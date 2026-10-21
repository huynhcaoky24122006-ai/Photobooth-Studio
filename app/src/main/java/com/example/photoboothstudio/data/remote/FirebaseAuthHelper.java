package com.example.photoboothstudio.data.remote;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

/**
 * FirebaseAuthHelper bọc FirebaseAuth: đăng ký, đăng nhập, quên mật khẩu, đăng xuất.
 *
 * Quy ước:
 * - Lớp này CHỈ làm việc với Firebase Auth. Không đọc/ghi Firestore, không đụng Room,
 *   không kiểm tra role hay isLocked. Các việc đó thuộc về AuthRepository.
 * - Kết quả trả về qua AuthCallback. Firebase gọi listener trên main thread nên callback
 *   có thể cập nhật UI trực tiếp.
 * - Thông báo lỗi trong onError đã được dịch sang tiếng Việt để hiển thị thẳng cho người dùng.
 * - Dùng Singleton giống AppDatabase: FirebaseAuthHelper.getInstance().
 */
public class FirebaseAuthHelper {

    /** Callback chung cho các thao tác bất đồng bộ của Firebase Auth. */
    public interface AuthCallback<T> {
        void onSuccess(T result);

        void onError(@NonNull String message);
    }

    private static volatile FirebaseAuthHelper instance;

    private final FirebaseAuth auth;

    private FirebaseAuthHelper() {
        this.auth = FirebaseAuth.getInstance();
    }

    public static FirebaseAuthHelper getInstance() {
        if (instance == null) {
            synchronized (FirebaseAuthHelper.class) {
                if (instance == null) {
                    instance = new FirebaseAuthHelper();
                }
            }
        }
        return instance;
    }

    // ==================== TRẠNG THÁI HIỆN TẠI ====================

    /** User Firebase đang đăng nhập, null nếu chưa đăng nhập. */
    @Nullable
    public FirebaseUser getCurrentUser() {
        return auth.getCurrentUser();
    }

    /** UID của user đang đăng nhập, null nếu chưa đăng nhập. UID chính là User.id / UserEntity.id. */
    @Nullable
    public String getCurrentUid() {
        FirebaseUser user = auth.getCurrentUser();
        return user != null ? user.getUid() : null;
    }

    public boolean isSignedIn() {
        return auth.getCurrentUser() != null;
    }

    // ==================== ĐĂNG KÝ ====================

    /**
     * Tạo tài khoản mới bằng email + mật khẩu.
     * Nếu truyền displayName hợp lệ, tên sẽ được gán vào hồ sơ Firebase Auth.
     * Việc gán tên thất bại không làm đăng ký thất bại (tài khoản đã được tạo).
     * Lưu ý: chưa tạo document trong Firestore, bước đó do AuthRepository gọi FirestoreHelper.
     */
    public void register(String email, String password, @Nullable String displayName,
                         @NonNull AuthCallback<FirebaseUser> callback) {
        String error = validateCredentials(email, password);
        if (error != null) {
            callback.onError(error);
            return;
        }

        auth.createUserWithEmailAndPassword(email.trim(), password)
                .addOnSuccessListener(result -> {
                    FirebaseUser user = result.getUser();
                    if (user == null) {
                        callback.onError("Không thể tạo tài khoản. Vui lòng thử lại.");
                        return;
                    }
                    if (displayName == null || displayName.trim().isEmpty()) {
                        callback.onSuccess(user);
                        return;
                    }
                    UserProfileChangeRequest profile = new UserProfileChangeRequest.Builder()
                            .setDisplayName(displayName.trim())
                            .build();
                    user.updateProfile(profile)
                            .addOnCompleteListener(task -> callback.onSuccess(user));
                })
                .addOnFailureListener(e -> callback.onError(toMessage(e)));
    }

    // ==================== ĐĂNG NHẬP ====================

    /**
     * Đăng nhập bằng email + mật khẩu. Dùng chung cho Member và Admin;
     * việc phân biệt role và kiểm tra isLocked do AuthRepository đảm nhiệm
     * (nếu tài khoản bị khóa thì AuthRepository gọi logout()).
     */
    public void login(String email, String password,
                      @NonNull AuthCallback<FirebaseUser> callback) {
        String error = validateCredentials(email, password);
        if (error != null) {
            callback.onError(error);
            return;
        }

        auth.signInWithEmailAndPassword(email.trim(), password)
                .addOnSuccessListener(result -> {
                    FirebaseUser user = result.getUser();
                    if (user == null) {
                        callback.onError("Không thể đăng nhập. Vui lòng thử lại.");
                    } else {
                        callback.onSuccess(user);
                    }
                })
                .addOnFailureListener(e -> callback.onError(toMessage(e)));
    }

    // ==================== QUÊN MẬT KHẨU ====================

    /** Gửi email đặt lại mật khẩu. */
    public void sendPasswordReset(String email, @NonNull AuthCallback<Void> callback) {
        if (email == null || email.trim().isEmpty()) {
            callback.onError("Vui lòng nhập email.");
            return;
        }

        auth.sendPasswordResetEmail(email.trim())
                .addOnSuccessListener(unused -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onError(toMessage(e)));
    }

    // ==================== ĐĂNG XUẤT ====================

    /** Đăng xuất khỏi Firebase Auth. Việc xóa session cục bộ (SharedPrefManager) do AuthRepository làm. */
    public void logout() {
        auth.signOut();
    }

    // ==================== HỖ TRỢ ====================

    /** Kiểm tra nhanh đầu vào trước khi gọi Firebase. Trả về null nếu hợp lệ. */
    @Nullable
    private String validateCredentials(String email, String password) {
        if (email == null || email.trim().isEmpty()) {
            return "Vui lòng nhập email.";
        }
        if (password == null || password.isEmpty()) {
            return "Vui lòng nhập mật khẩu.";
        }
        return null;
    }

    /** Dịch exception của Firebase Auth sang thông báo tiếng Việt. */
    @NonNull
    private String toMessage(@NonNull Exception e) {
        if (e instanceof FirebaseAuthWeakPasswordException) {
            return "Mật khẩu quá yếu, vui lòng dùng ít nhất 6 ký tự.";
        }
        if (e instanceof FirebaseAuthUserCollisionException) {
            return "Email này đã được đăng ký.";
        }
        if (e instanceof FirebaseAuthInvalidUserException) {
            String code = ((FirebaseAuthInvalidUserException) e).getErrorCode();
            if ("ERROR_USER_DISABLED".equals(code)) {
                return "Tài khoản đã bị vô hiệu hóa.";
            }
            return "Tài khoản không tồn tại.";
        }
        if (e instanceof FirebaseAuthInvalidCredentialsException) {
            String code = ((FirebaseAuthInvalidCredentialsException) e).getErrorCode();
            if ("ERROR_INVALID_EMAIL".equals(code)) {
                return "Email không hợp lệ.";
            }
            // ERROR_WRONG_PASSWORD hoặc ERROR_INVALID_CREDENTIAL (SDK mới gộp chung hai trường hợp)
            return "Email hoặc mật khẩu không đúng.";
        }
        if (e instanceof FirebaseTooManyRequestsException) {
            return "Bạn thử quá nhiều lần. Vui lòng đợi một lúc rồi thử lại.";
        }
        if (e instanceof FirebaseNetworkException) {
            return "Không có kết nối mạng. Vui lòng kiểm tra lại.";
        }
        return "Đã xảy ra lỗi. Vui lòng thử lại.";
    }
}