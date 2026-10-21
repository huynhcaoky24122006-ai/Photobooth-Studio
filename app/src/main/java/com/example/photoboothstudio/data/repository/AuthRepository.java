package com.example.photoboothstudio.data.repository;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.photoboothstudio.data.local.AppDatabase;
import com.example.photoboothstudio.data.local.SharedPrefManager;
import com.example.photoboothstudio.data.local.dao.UserDao;
import com.example.photoboothstudio.data.mapper.UserMapper;
import com.example.photoboothstudio.data.remote.FirebaseAuthHelper;
import com.example.photoboothstudio.data.remote.FirestoreHelper;
import com.example.photoboothstudio.models.User;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * AuthRepository là trung gian xử lý đăng nhập / đăng ký / phiên đăng nhập.
 *
 * Phối hợp ba nguồn dữ liệu:
 * - FirebaseAuthHelper: xác thực email + mật khẩu.
 * - FirestoreHelper: hồ sơ User (role, isLocked, Xu...) là nguồn dữ liệu chuẩn.
 * - Room (UserDao) + SharedPrefManager: bản lưu cục bộ để mở app nhanh và dùng khi mất mạng.
 *
 * Quy ước:
 * - Mọi callback trả về trên main thread, Activity cập nhật UI trực tiếp được.
 * - Room chỉ truy cập trên một luồng nền riêng (không chạy trên main thread).
 * - Thông báo lỗi trong onError là tiếng Việt, hiển thị thẳng cho người dùng.
 * - Đăng nhập chỉ được coi là thành công khi đã qua kiểm tra role và isLocked;
 *   nếu không qua, Firebase Auth bị đăng xuất lại và KHÔNG lưu session.
 * - Tài khoản Admin KHÔNG đăng ký trong app, tạo thủ công trên Firebase Console
 *   (users/{uid} với role = "ADMIN").
 */
public class AuthRepository {

    /** Callback chung của Repository. */
    public interface RepoCallback<T> {
        void onSuccess(T result);

        void onError(@NonNull String message);
    }

    private static final String TAG = "AuthRepository";
    private static final String ROLE_MEMBER = "MEMBER";
    private static final String ROLE_ADMIN = "ADMIN";

    private static final String MSG_LOCKED =
            "Tài khoản của bạn đã bị khóa. Vui lòng liên hệ Admin.";
    private static final String MSG_NOT_ADMIN = "Tài khoản này không phải Admin.";
    private static final String MSG_ADMIN_USE_ADMIN_GATE =
            "Tài khoản Admin vui lòng đăng nhập ở cổng Admin.";

    private static volatile AuthRepository instance;

    private final FirebaseAuthHelper authHelper;
    private final FirestoreHelper firestoreHelper;
    private final UserDao userDao;
    private final SharedPrefManager prefs;
    private final ExecutorService executor;
    private final Handler mainHandler;

    private AuthRepository(Context context) {
        Context appContext = context.getApplicationContext();
        this.authHelper = FirebaseAuthHelper.getInstance();
        this.firestoreHelper = FirestoreHelper.getInstance();
        this.userDao = AppDatabase.getInstance(appContext).userDao();
        this.prefs = SharedPrefManager.getInstance(appContext);
        this.executor = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    public static AuthRepository getInstance(Context context) {
        if (instance == null) {
            synchronized (AuthRepository.class) {
                if (instance == null) {
                    instance = new AuthRepository(context);
                }
            }
        }
        return instance;
    }

    // ==================== ĐĂNG KÝ ====================

    /**
     * Đăng ký Member mới: tạo tài khoản Firebase Auth, ghi hồ sơ users/{uid} lên Firestore
     * (role MEMBER, 0 Xu, không VIP, không khóa), rồi lưu Room + session.
     * Thành công nghĩa là người dùng đã ở trạng thái đăng nhập.
     * Nếu ghi hồ sơ thất bại, tài khoản Auth đã được tạo; lần đăng nhập sau sẽ tự tạo lại hồ sơ.
     */
    public void registerMember(String email, String password, String name,
                               @NonNull RepoCallback<User> callback) {
        authHelper.register(email, password, name, new FirebaseAuthHelper.AuthCallback<FirebaseUser>() {
            @Override
            public void onSuccess(FirebaseUser firebaseUser) {
                createMemberProfile(firebaseUser, name, callback);
            }

            @Override
            public void onError(@NonNull String message) {
                callback.onError(message);
            }
        });
    }

    // ==================== ĐĂNG NHẬP ====================

    /** Đăng nhập Member. Tài khoản Admin bị từ chối ở cổng này. */
    public void loginMember(String email, String password, @NonNull RepoCallback<User> callback) {
        login(email, password, false, callback);
    }

    /** Đăng nhập Admin. Chỉ chấp nhận tài khoản có role ADMIN trong Firestore. */
    public void loginAdmin(String email, String password, @NonNull RepoCallback<User> callback) {
        login(email, password, true, callback);
    }

    private void login(String email, String password, boolean adminOnly,
                       @NonNull RepoCallback<User> callback) {
        authHelper.login(email, password, new FirebaseAuthHelper.AuthCallback<FirebaseUser>() {
            @Override
            public void onSuccess(FirebaseUser firebaseUser) {
                loadAndVerify(firebaseUser, adminOnly, callback);
            }

            @Override
            public void onError(@NonNull String message) {
                callback.onError(message);
            }
        });
    }

    /** Sau khi Firebase Auth xác thực xong: đọc hồ sơ, kiểm tra khóa + role, rồi lưu session. */
    private void loadAndVerify(@NonNull FirebaseUser firebaseUser, boolean adminOnly,
                               @NonNull RepoCallback<User> callback) {
        String uid = firebaseUser.getUid();
        firestoreHelper.getUser(uid, new FirestoreHelper.FirestoreCallback<User>() {
            @Override
            public void onSuccess(@Nullable User remote) {
                if (remote == null) {
                    if (adminOnly) {
                        rejectLogin(MSG_NOT_ADMIN, callback);
                    } else {
                        // Có tài khoản Auth nhưng chưa có hồ sơ (ví dụ đăng ký bị gián đoạn): tạo lại.
                        createMemberProfile(firebaseUser, null, callback);
                    }
                    return;
                }

                normalize(remote, uid, firebaseUser.getEmail());

                String accessError = checkAccess(remote, adminOnly);
                if (accessError != null) {
                    rejectLogin(accessError, callback);
                    return;
                }
                persistSession(remote, callback);
            }

            @Override
            public void onError(@NonNull String message) {
                rejectLogin(message, callback);
            }
        });
    }

    /** Trả về thông báo lỗi nếu tài khoản không được phép đăng nhập vào cổng này, ngược lại null. */
    @Nullable
    private String checkAccess(@NonNull User user, boolean adminOnly) {
        if (user.isLocked()) {
            return MSG_LOCKED;
        }
        boolean isAdmin = ROLE_ADMIN.equals(user.getRole());
        if (adminOnly && !isAdmin) {
            return MSG_NOT_ADMIN;
        }
        if (!adminOnly && isAdmin) {
            return MSG_ADMIN_USE_ADMIN_GATE;
        }
        return null;
    }

    /** Hủy lần đăng nhập: đăng xuất Firebase Auth, không lưu session, báo lỗi. */
    private void rejectLogin(@NonNull String message, @NonNull RepoCallback<User> callback) {
        authHelper.logout();
        callback.onError(message);
    }

    /** Tạo hồ sơ Member mặc định trên Firestore cho một tài khoản Auth, rồi lưu session. */
    private void createMemberProfile(@NonNull FirebaseUser firebaseUser, @Nullable String name,
                                     @NonNull RepoCallback<User> callback) {
        String email = firebaseUser.getEmail() != null ? firebaseUser.getEmail() : "";
        String displayName = !isBlank(name) ? name.trim()
                : (!isBlank(firebaseUser.getDisplayName()) ? firebaseUser.getDisplayName()
                   : nameFromEmail(email));
        User user = new User(firebaseUser.getUid(), email, displayName);

        firestoreHelper.saveUser(user, new FirestoreHelper.FirestoreCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                persistSession(user, callback);
            }

            @Override
            public void onError(@NonNull String message) {
                authHelper.logout();
                callback.onError("Không thể lưu hồ sơ tài khoản: " + message
                        + " Vui lòng đăng nhập lại để hoàn tất.");
            }
        });
    }

    // ==================== QUÊN MẬT KHẨU ====================

    public void sendPasswordReset(String email, @NonNull RepoCallback<Void> callback) {
        authHelper.sendPasswordReset(email, new FirebaseAuthHelper.AuthCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                callback.onSuccess(null);
            }

            @Override
            public void onError(@NonNull String message) {
                callback.onError(message);
            }
        });
    }

    // ==================== PHIÊN ĐĂNG NHẬP ====================

    /**
     * Khôi phục phiên khi mở app (dùng ở WelcomeActivity).
     * - onSuccess(null): chưa đăng nhập (hoặc phiên đã hết), ở lại màn Welcome.
     * - onSuccess(user): đã đăng nhập hợp lệ, user là bản mới nhất; dựa vào user.getRole()
     *   để chuyển sang màn Member hoặc Admin.
     * - onError: tài khoản bị khóa/không còn tồn tại, hoặc không xác minh được (mất mạng và
     *   chưa có bản lưu cục bộ). Phiên cục bộ đã bị xóa nếu là khóa/không tồn tại.
     * Khi mất mạng nhưng đã có bản lưu trong Room (và chưa khóa) thì dùng bản lưu đó.
     */
    public void restoreSession(@NonNull RepoCallback<User> callback) {
        String uid = authHelper.getCurrentUid();

        if (!prefs.isLoggedIn()) {
            callback.onSuccess(null);
            return;
        }
        if (uid == null || !uid.equals(prefs.getUserId())) {
            // Firebase Auth đã mất phiên hoặc lệch tài khoản: dọn sạch phiên cục bộ.
            forceLogout();
            callback.onSuccess(null);
            return;
        }
        syncFromRemote(uid, true, callback);
    }

    /**
     * Tải lại hồ sơ mới nhất từ Firestore và cập nhật Room + session
     * (dùng sau khi mua Frame, điểm danh, hoặc khi mở Profile để thấy Xu/VIP mới).
     * Nếu tài khoản vừa bị khóa, tự đăng xuất và trả onError.
     */
    public void refreshCurrentUser(@NonNull RepoCallback<User> callback) {
        String uid = authHelper.getCurrentUid();
        if (uid == null) {
            callback.onError("Bạn chưa đăng nhập.");
            return;
        }
        syncFromRemote(uid, false, callback);
    }

    private void syncFromRemote(@NonNull String uid, boolean fallbackToCache,
                                @NonNull RepoCallback<User> callback) {
        firestoreHelper.getUser(uid, new FirestoreHelper.FirestoreCallback<User>() {
            @Override
            public void onSuccess(@Nullable User remote) {
                if (remote == null) {
                    forceLogout();
                    callback.onError("Tài khoản không còn tồn tại.");
                    return;
                }
                normalize(remote, uid, null);
                if (remote.isLocked()) {
                    forceLogout();
                    callback.onError(MSG_LOCKED);
                    return;
                }
                persistSession(remote, callback);
            }

            @Override
            public void onError(@NonNull String message) {
                if (fallbackToCache) {
                    useCachedUser(uid, message, callback);
                } else {
                    callback.onError(message);
                }
            }
        });
    }

    /** Dùng bản lưu trong Room khi không đọc được Firestore (mất mạng). */
    private void useCachedUser(@NonNull String uid, @NonNull String remoteError,
                               @NonNull RepoCallback<User> callback) {
        executor.execute(() -> {
            User cached = null;
            try {
                cached = UserMapper.toModel(userDao.getUserById(uid));
            } catch (Exception e) {
                Log.w(TAG, "Không đọc được User từ Room", e);
            }
            User result = cached;
            mainHandler.post(() -> {
                if (result != null && !result.isLocked()) {
                    callback.onSuccess(result);
                } else {
                    callback.onError(remoteError);
                }
            });
        });
    }

    /** Đọc User đã lưu trong Room (không cần mạng). onSuccess(null) nếu chưa có. */
    public void getCachedUser(@NonNull RepoCallback<User> callback) {
        String uid = prefs.getUserId();
        if (uid == null) {
            callback.onSuccess(null);
            return;
        }
        executor.execute(() -> {
            User cached = null;
            try {
                cached = UserMapper.toModel(userDao.getUserById(uid));
            } catch (Exception e) {
                Log.w(TAG, "Không đọc được User từ Room", e);
            }
            User result = cached;
            mainHandler.post(() -> callback.onSuccess(result));
        });
    }

    /**
     * Ghi một User vào Room (không đụng Firestore, không đổi session).
     * Dùng khi Repository/CoinManager đã cập nhật Firestore và muốn đồng bộ bản cục bộ.
     */
    public void saveUserLocally(@NonNull User user) {
        if (user.getId() == null) {
            return;
        }
        executor.execute(() -> {
            try {
                userDao.insertUser(UserMapper.toEntity(user));
            } catch (Exception e) {
                Log.w(TAG, "Không ghi được User vào Room", e);
            }
        });
    }

    // ==================== ĐĂNG XUẤT ====================

    /** Đăng xuất: Firebase Auth, session SharedPreferences và bản lưu User trong Room. */
    public void logout() {
        forceLogout();
    }

    private void forceLogout() {
        String uid = prefs.getUserId();
        authHelper.logout();
        prefs.clearSession();
        if (uid != null) {
            executor.execute(() -> {
                try {
                    userDao.deleteUserById(uid);
                } catch (Exception e) {
                    Log.w(TAG, "Không xóa được User khỏi Room", e);
                }
            });
        }
    }

    // ==================== HỖ TRỢ ====================

    /** Lưu User vào Room + SharedPreferences (luồng nền), rồi trả kết quả về main thread. */
    private void persistSession(@NonNull User user, @NonNull RepoCallback<User> callback) {
        executor.execute(() -> {
            try {
                userDao.insertUser(UserMapper.toEntity(user));
            } catch (Exception e) {
                // Lỗi cache cục bộ không được làm hỏng đăng nhập.
                Log.w(TAG, "Không ghi được User vào Room", e);
            }
            prefs.saveUserSession(user.getId(), user.getEmail(), user.getName(), user.getRole());
            mainHandler.post(() -> callback.onSuccess(user));
        });
    }

    /**
     * Chuẩn hóa User đọc từ Firestore:
     * - id luôn lấy theo UID (document id), phòng khi field id trong document bị nhập sai.
     * - role thiếu thì coi là MEMBER; email thiếu thì lấy từ tài khoản Auth.
     * - hai danh sách không bao giờ null.
     */
    private void normalize(@NonNull User user, @NonNull String uid, @Nullable String authEmail) {
        user.setId(uid);
        if (isBlank(user.getRole())) {
            user.setRole(ROLE_MEMBER);
        }
        if (isBlank(user.getEmail()) && authEmail != null) {
            user.setEmail(authEmail);
        }
        if (user.getUnlockedFrameIds() == null) {
            user.setUnlockedFrameIds(new ArrayList<>());
        }
        if (user.getUnlockedStickerIds() == null) {
            user.setUnlockedStickerIds(new ArrayList<>());
        }
    }

    private static boolean isBlank(@Nullable String s) {
        return s == null || s.trim().isEmpty();
    }

    /** Lấy phần trước dấu @ làm tên hiển thị mặc định. */
    @NonNull
    private static String nameFromEmail(@NonNull String email) {
        int at = email.indexOf('@');
        return at > 0 ? email.substring(0, at) : "Member";
    }
}