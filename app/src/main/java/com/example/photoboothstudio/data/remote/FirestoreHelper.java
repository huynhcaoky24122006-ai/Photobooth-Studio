package com.example.photoboothstudio.data.remote;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.photoboothstudio.models.Frame;
import com.example.photoboothstudio.models.Photo;
import com.example.photoboothstudio.models.RewardClaim;
import com.example.photoboothstudio.models.Sticker;
import com.example.photoboothstudio.models.User;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.Transaction;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * FirestoreHelper bọc FirebaseFirestore: đọc/ghi User, Frame, Sticker, Photo, RewardClaim.
 *
 * Quy ước:
 * - Lớp này CHỈ làm việc với Firestore. Không đụng Room, SharedPrefManager hay Firebase Auth;
 *   việc đồng bộ Room và kiểm tra isLocked khi đăng nhập thuộc về Repository.
 * - Kết quả trả về qua FirestoreCallback, chạy trên main thread. Thông báo lỗi đã được dịch
 *   sang tiếng Việt để hiển thị thẳng cho người dùng.
 * - Các thao tác động đến Xu (mua Frame/Sticker, điểm danh, tặng Xu) dùng Transaction để
 *   kiểm tra và trừ/cộng Xu trong một bước, tránh mua hai lần hoặc điểm danh hai lần.
 * - Tên field trên Firestore do Firestore tự suy ra từ getter của model:
 *   isVIP() -> "vip", isLocked() -> "locked", isPrivate() -> "private".
 *   Các hằng FIELD_* bên dưới phải khớp với quy tắc này.
 * - Model phải có constructor rỗng để toObject()/toObjects() hoạt động (đã có sẵn).
 */
public class FirestoreHelper {

    /** Callback chung cho các thao tác bất đồng bộ của Firestore. */
    public interface FirestoreCallback<T> {
        void onSuccess(T result);

        void onError(@NonNull String message);
    }

    // ==================== TÊN COLLECTION / FIELD ====================

    public static final String COL_USERS = "users";
    public static final String COL_FRAMES = "frames";
    public static final String COL_STICKERS = "stickers";
    public static final String COL_PHOTOS = "photos";
    public static final String COL_REWARD_CLAIMS = "reward_claims";
    public static final String COL_OTP_CODES = "otp_codes";

    public static final String FIELD_ROLE = "role";
    public static final String FIELD_COINS = "coins";
    public static final String FIELD_VIP = "vip";
    public static final String FIELD_LOCKED = "locked";
    public static final String FIELD_UNLOCKED_FRAMES = "unlockedFrameIds";
    public static final String FIELD_UNLOCKED_STICKERS = "unlockedStickerIds";
    public static final String FIELD_PRICE = "priceInCoins";
    public static final String FIELD_OWNER_ID = "ownerId";
    public static final String FIELD_PRIVATE = "private";
    public static final String FIELD_USER_ID = "userId";

    private static final String ROLE_MEMBER = "MEMBER";
    private static final String ROLE_ADMIN = "ADMIN";

    /** Kết quả nội bộ của Transaction: thành công kèm số Xu mới, hoặc thất bại kèm lý do. */
    private static final class TxResult {
        final String error; // null nếu thành công
        final int coins;

        private TxResult(String error, int coins) {
            this.error = error;
            this.coins = coins;
        }

        static TxResult ok(long coins) {
            return new TxResult(null, (int) coins);
        }

        static TxResult fail(String message) {
            return new TxResult(message, 0);
        }
    }

    private static volatile FirestoreHelper instance;

    private final FirebaseFirestore db;

    private FirestoreHelper() {
        this.db = FirebaseFirestore.getInstance();
    }

    public static FirestoreHelper getInstance() {
        if (instance == null) {
            synchronized (FirestoreHelper.class) {
                if (instance == null) {
                    instance = new FirestoreHelper();
                }
            }
        }
        return instance;
    }

    // ==================== USER ====================

    /** Ghi toàn bộ User (dùng khi đăng ký). Document id = User.id = UID Firebase Auth. */
    public void saveUser(@NonNull User user, @NonNull FirestoreCallback<Void> callback) {
        if (isEmpty(user.getId())) {
            callback.onError("Thiếu ID người dùng.");
            return;
        }
        db.collection(COL_USERS).document(user.getId()).set(user)
                .addOnSuccessListener(unused -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onError(toMessage(e)));
    }

    /** Đọc User theo UID. Trả về null (onSuccess(null)) nếu document không tồn tại. */
    public void getUser(String uid, @NonNull FirestoreCallback<User> callback) {
        if (isEmpty(uid)) {
            callback.onError("Thiếu ID người dùng.");
            return;
        }
        db.collection(COL_USERS).document(uid).get()
                .addOnSuccessListener(snapshot ->
                        callback.onSuccess(snapshot.exists() ? snapshot.toObject(User.class) : null))
                .addOnFailureListener(e -> callback.onError(toMessage(e)));
    }

    /** Danh sách toàn bộ Member (cho màn Admin quản lý người dùng). */
    public void getAllMembers(@NonNull FirestoreCallback<List<User>> callback) {
        fetchList(db.collection(COL_USERS).whereEqualTo(FIELD_ROLE, ROLE_MEMBER),
                User.class, callback);
    }

    /**
     * Khóa / mở khóa tài khoản. Không cho khóa tài khoản ADMIN.
     * Việc chặn Admin tự khóa chính mình do Activity kiểm tra (so với UID đang đăng nhập).
     */
    public void setUserLocked(String uid, boolean locked, @NonNull FirestoreCallback<Void> callback) {
        if (isEmpty(uid)) {
            callback.onError("Thiếu ID người dùng.");
            return;
        }
        DocumentReference userRef = db.collection(COL_USERS).document(uid);
        deliverVoid(db.runTransaction(tx -> {
            DocumentSnapshot snap = tx.get(userRef);
            if (!snap.exists()) {
                return TxResult.fail("Không tìm thấy tài khoản.");
            }
            if (ROLE_ADMIN.equals(snap.getString(FIELD_ROLE))) {
                return TxResult.fail("Không thể khóa tài khoản Admin.");
            }
            tx.update(userRef, FIELD_LOCKED, locked);
            return TxResult.ok(0);
        }), callback);
    }

    /** Cấp / thu hồi quyền VIP (Admin). */
    public void setUserVip(String uid, boolean vip, @NonNull FirestoreCallback<Void> callback) {
        if (isEmpty(uid)) {
            callback.onError("Thiếu ID người dùng.");
            return;
        }
        db.collection(COL_USERS).document(uid).update(FIELD_VIP, vip)
                .addOnSuccessListener(unused -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onError(toMessage(e)));
    }

    /** Tặng Xu cho một tài khoản (Admin). Trả về số Xu mới. */
    public void addCoins(String uid, int amount, @NonNull FirestoreCallback<Integer> callback) {
        if (isEmpty(uid)) {
            callback.onError("Thiếu ID người dùng.");
            return;
        }
        if (amount <= 0) {
            callback.onError("Số Xu phải lớn hơn 0.");
            return;
        }
        DocumentReference userRef = db.collection(COL_USERS).document(uid);
        deliverCoins(db.runTransaction(tx -> {
            DocumentSnapshot snap = tx.get(userRef);
            if (!snap.exists()) {
                return TxResult.fail("Không tìm thấy tài khoản.");
            }
            long newCoins = longOf(snap, FIELD_COINS) + amount;
            tx.update(userRef, FIELD_COINS, newCoins);
            return TxResult.ok(newCoins);
        }), callback);
    }

    // ==================== MUA FRAME / STICKER ====================

    /**
     * Mua Frame bằng Xu. Giá lấy từ document Frame trên Firestore (không tin giá từ client).
     * Trong một Transaction: kiểm tra tài khoản, đã sở hữu chưa, VIP, đủ Xu; rồi trừ Xu và
     * thêm frameId vào unlockedFrameIds. Trả về số Xu còn lại.
     */
    public void purchaseFrame(String uid, String frameId, @NonNull FirestoreCallback<Integer> callback) {
        purchaseItem(uid, frameId, COL_FRAMES, FIELD_UNLOCKED_FRAMES,
                "Không tìm thấy Frame này.", callback);
    }

    /** Mua Sticker bằng Xu. Quy tắc giống purchaseFrame, thêm vào unlockedStickerIds. */
    public void purchaseSticker(String uid, String stickerId, @NonNull FirestoreCallback<Integer> callback) {
        purchaseItem(uid, stickerId, COL_STICKERS, FIELD_UNLOCKED_STICKERS,
                "Không tìm thấy Sticker này.", callback);
    }

    @SuppressWarnings("unchecked")
    private void purchaseItem(String uid, String itemId, String itemCollection,
                              String unlockedField, String itemNotFoundMessage,
                              FirestoreCallback<Integer> callback) {
        if (isEmpty(uid) || isEmpty(itemId)) {
            callback.onError("Thiếu thông tin mua hàng.");
            return;
        }
        DocumentReference userRef = db.collection(COL_USERS).document(uid);
        DocumentReference itemRef = db.collection(itemCollection).document(itemId);

        deliverCoins(db.runTransaction(tx -> {
            // Firestore yêu cầu đọc xong hết rồi mới ghi.
            DocumentSnapshot userSnap = tx.get(userRef);
            DocumentSnapshot itemSnap = tx.get(itemRef);

            if (!userSnap.exists()) {
                return TxResult.fail("Không tìm thấy tài khoản.");
            }
            if (!itemSnap.exists()) {
                return TxResult.fail(itemNotFoundMessage);
            }
            if (Boolean.TRUE.equals(userSnap.getBoolean(FIELD_LOCKED))) {
                return TxResult.fail("Tài khoản đã bị khóa.");
            }

            List<String> owned = (List<String>) userSnap.get(unlockedField);
            if (owned != null && owned.contains(itemId)) {
                return TxResult.fail("Bạn đã sở hữu vật phẩm này.");
            }

            boolean itemIsVip = Boolean.TRUE.equals(itemSnap.getBoolean(FIELD_VIP));
            boolean userIsVip = Boolean.TRUE.equals(userSnap.getBoolean(FIELD_VIP));
            if (itemIsVip && !userIsVip) {
                return TxResult.fail("Vật phẩm này chỉ dành cho thành viên VIP.");
            }

            long price = longOf(itemSnap, FIELD_PRICE);
            long coins = longOf(userSnap, FIELD_COINS);
            if (coins < price) {
                return TxResult.fail("Bạn không đủ Xu để mua.");
            }

            long newCoins = coins - price;
            tx.update(userRef,
                    FIELD_COINS, newCoins,
                    unlockedField, FieldValue.arrayUnion(itemId));
            return TxResult.ok(newCoins);
        }), callback);
    }

    // ==================== ĐIỂM DANH ====================

    /** Ngày hiện tại theo giờ của thiết bị, định dạng "yyyy-MM-dd" (khớp RewardClaim.claimDate). */
    public static String getTodayDate() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
    }

    /** Id của lượt điểm danh, ví dụ "claim_uid123_20261021". Mỗi user chỉ có một id mỗi ngày. */
    public static String buildClaimId(String uid, String claimDate) {
        return "claim_" + uid + "_" + claimDate.replace("-", "");
    }

    /**
     * Điểm danh nhận Xu cho ngày hôm nay. Trong một Transaction: nếu hôm nay đã có lượt điểm danh
     * thì báo lỗi, nếu chưa thì tạo RewardClaim và cộng Xu. Trả về số Xu mới.
     * Số Xu của từng ngày trong bảng 7 ngày do nơi gọi quyết định.
     */
    public void claimDailyReward(String uid, int rewardCoins, @NonNull FirestoreCallback<Integer> callback) {
        if (isEmpty(uid)) {
            callback.onError("Thiếu ID người dùng.");
            return;
        }
        if (rewardCoins <= 0) {
            callback.onError("Số Xu thưởng không hợp lệ.");
            return;
        }
        String today = getTodayDate();
        String claimId = buildClaimId(uid, today);
        DocumentReference userRef = db.collection(COL_USERS).document(uid);
        DocumentReference claimRef = db.collection(COL_REWARD_CLAIMS).document(claimId);

        deliverCoins(db.runTransaction(tx -> {
            DocumentSnapshot userSnap = tx.get(userRef);
            DocumentSnapshot claimSnap = tx.get(claimRef);

            if (!userSnap.exists()) {
                return TxResult.fail("Không tìm thấy tài khoản.");
            }
            if (Boolean.TRUE.equals(userSnap.getBoolean(FIELD_LOCKED))) {
                return TxResult.fail("Tài khoản đã bị khóa.");
            }
            if (claimSnap.exists()) {
                return TxResult.fail("Hôm nay bạn đã điểm danh rồi.");
            }

            long newCoins = longOf(userSnap, FIELD_COINS) + rewardCoins;
            tx.update(userRef, FIELD_COINS, newCoins);
            tx.set(claimRef, new RewardClaim(claimId, uid, rewardCoins, today,
                    System.currentTimeMillis()));
            return TxResult.ok(newCoins);
        }), callback);
    }

    /** Hôm nay user đã điểm danh chưa. */
    public void hasClaimedToday(String uid, @NonNull FirestoreCallback<Boolean> callback) {
        if (isEmpty(uid)) {
            callback.onError("Thiếu ID người dùng.");
            return;
        }
        String claimId = buildClaimId(uid, getTodayDate());
        db.collection(COL_REWARD_CLAIMS).document(claimId).get()
                .addOnSuccessListener(snapshot -> callback.onSuccess(snapshot.exists()))
                .addOnFailureListener(e -> callback.onError(toMessage(e)));
    }

    /** Lịch sử điểm danh của user, mới nhất trước (cho bảng 7 ngày). */
    public void getClaimsOfUser(String uid, @NonNull FirestoreCallback<List<RewardClaim>> callback) {
        if (isEmpty(uid)) {
            callback.onError("Thiếu ID người dùng.");
            return;
        }
        // Sắp xếp phía client để không phải tạo composite index trên Firestore.
        db.collection(COL_REWARD_CLAIMS).whereEqualTo(FIELD_USER_ID, uid).get()
                .addOnSuccessListener(snapshot -> {
                    List<RewardClaim> claims = snapshot.toObjects(RewardClaim.class);
                    Collections.sort(claims,
                            (a, b) -> Long.compare(b.getClaimedAt(), a.getClaimedAt()));
                    callback.onSuccess(claims);
                })
                .addOnFailureListener(e -> callback.onError(toMessage(e)));
    }

    // ==================== FRAME / STICKER ====================

    /** Ghi Frame (Admin upload). Document id = Frame.id. */
    public void saveFrame(@NonNull Frame frame, @NonNull FirestoreCallback<Void> callback) {
        if (isEmpty(frame.getId())) {
            callback.onError("Thiếu ID Frame.");
            return;
        }
        db.collection(COL_FRAMES).document(frame.getId()).set(frame)
                .addOnSuccessListener(unused -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onError(toMessage(e)));
    }

    public void getAllFrames(@NonNull FirestoreCallback<List<Frame>> callback) {
        fetchList(db.collection(COL_FRAMES), Frame.class, callback);
    }

    /** Ghi Sticker (Admin upload). Document id = Sticker.id. */
    public void saveSticker(@NonNull Sticker sticker, @NonNull FirestoreCallback<Void> callback) {
        if (isEmpty(sticker.getId())) {
            callback.onError("Thiếu ID Sticker.");
            return;
        }
        db.collection(COL_STICKERS).document(sticker.getId()).set(sticker)
                .addOnSuccessListener(unused -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onError(toMessage(e)));
    }

    public void getAllStickers(@NonNull FirestoreCallback<List<Sticker>> callback) {
        fetchList(db.collection(COL_STICKERS), Sticker.class, callback);
    }

    // ==================== PHOTO ====================

    /** Ghi Photo (metadata; file ảnh nằm trên Firebase Storage). Document id = Photo.id. */
    public void savePhoto(@NonNull Photo photo, @NonNull FirestoreCallback<Void> callback) {
        if (isEmpty(photo.getId())) {
            callback.onError("Thiếu ID ảnh.");
            return;
        }
        db.collection(COL_PHOTOS).document(photo.getId()).set(photo)
                .addOnSuccessListener(unused -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onError(toMessage(e)));
    }

    /** Ảnh của một chủ sở hữu, mới nhất trước (cho Gallery). */
    public void getPhotosByOwner(String ownerId, @NonNull FirestoreCallback<List<Photo>> callback) {
        if (isEmpty(ownerId)) {
            callback.onError("Thiếu ID người dùng.");
            return;
        }
        // Sắp xếp phía client để không phải tạo composite index trên Firestore.
        db.collection(COL_PHOTOS).whereEqualTo(FIELD_OWNER_ID, ownerId).get()
                .addOnSuccessListener(snapshot -> {
                    List<Photo> photos = snapshot.toObjects(Photo.class);
                    Collections.sort(photos,
                            (a, b) -> Long.compare(b.getCreatedAt(), a.getCreatedAt()));
                    callback.onSuccess(photos);
                })
                .addOnFailureListener(e -> callback.onError(toMessage(e)));
    }

    /** Đổi trạng thái riêng tư của ảnh. */
    public void setPhotoPrivate(String photoId, boolean isPrivate, @NonNull FirestoreCallback<Void> callback) {
        if (isEmpty(photoId)) {
            callback.onError("Thiếu ID ảnh.");
            return;
        }
        db.collection(COL_PHOTOS).document(photoId).update(FIELD_PRIVATE, isPrivate)
                .addOnSuccessListener(unused -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onError(toMessage(e)));
    }

    /** Xóa metadata của ảnh. Việc xóa file trên Storage do FirebaseStorageHelper làm. */
    public void deletePhoto(String photoId, @NonNull FirestoreCallback<Void> callback) {
        if (isEmpty(photoId)) {
            callback.onError("Thiếu ID ảnh.");
            return;
        }
        db.collection(COL_PHOTOS).document(photoId).delete()
                .addOnSuccessListener(unused -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onError(toMessage(e)));
    }

    // ==================== OTP CODES ====================

    /**
     * Lưu mã OTP 6 số lên Firestore kèm thời gian hết hạn (5 phút).
     */
    public void saveOtpCode(String email, String code, @NonNull FirestoreCallback<Void> callback) {
        if (isEmpty(email) || isEmpty(code)) {
            callback.onError("Thiếu Email hoặc mã OTP.");
            return;
        }
        long now = System.currentTimeMillis();
        long expiresAt = now + (5 * 60 * 1000); // Hết hạn sau 5 phút

        java.util.Map<String, Object> data = new java.util.HashMap<>();
        data.put("email", email.trim().toLowerCase());
        data.put("code", code.trim());
        data.put("createdAt", now);
        data.put("expiresAt", expiresAt);

        db.collection(COL_OTP_CODES).document(email.trim().toLowerCase()).set(data)
                .addOnSuccessListener(unused -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onError(toMessage(e)));
    }

    /**
     * Xác thực mã OTP 6 số người dùng nhập so với mã trên Firestore.
     */
    public void verifyOtpCode(String email, String inputCode, @NonNull FirestoreCallback<Boolean> callback) {
        if (isEmpty(email) || isEmpty(inputCode)) {
            callback.onError("Vui lòng nhập đầy đủ Email và mã OTP.");
            return;
        }
        String docId = email.trim().toLowerCase();
        db.collection(COL_OTP_CODES).document(docId).get()
                .addOnSuccessListener(snapshot -> {
                    if (!snapshot.exists()) {
                        callback.onError("Chưa có mã OTP được tạo cho Email này. Vui lòng bấm 'Gửi mã qua Email'!");
                        return;
                    }

                    Long expiresAt = snapshot.getLong("expiresAt");
                    String savedCode = snapshot.getString("code");

                    if (expiresAt == null || System.currentTimeMillis() > expiresAt) {
                        callback.onError("Mã OTP đã hết hạn (quá 5 phút). Vui lòng bấm gửi lại mã mới!");
                        return;
                    }

                    if (savedCode != null && savedCode.equals(inputCode.trim())) {
                        // Xóa mã sau khi xác thực thành công
                        db.collection(COL_OTP_CODES).document(docId).delete();
                        callback.onSuccess(true);
                    } else {
                        callback.onError("Mã OTP không chính xác. Vui lòng kiểm tra lại!");
                    }
                })
                .addOnFailureListener(e -> callback.onError(toMessage(e)));
    }

    // ==================== HỖ TRỢ ====================

    private <T> void fetchList(Query query, Class<T> clazz, FirestoreCallback<List<T>> callback) {
        query.get()
                .addOnSuccessListener(snapshot -> callback.onSuccess(snapshot.toObjects(clazz)))
                .addOnFailureListener(e -> callback.onError(toMessage(e)));
    }

    private void deliverCoins(Task<TxResult> task, FirestoreCallback<Integer> callback) {
        task.addOnSuccessListener(result -> {
            if (result.error != null) {
                callback.onError(result.error);
            } else {
                callback.onSuccess(result.coins);
            }
        }).addOnFailureListener(e -> callback.onError(toMessage(e)));
    }

    private void deliverVoid(Task<TxResult> task, FirestoreCallback<Void> callback) {
        task.addOnSuccessListener(result -> {
            if (result.error != null) {
                callback.onError(result.error);
            } else {
                callback.onSuccess(null);
            }
        }).addOnFailureListener(e -> callback.onError(toMessage(e)));
    }

    /** Đọc field số nguyên, thiếu field thì coi là 0. */
    private static long longOf(DocumentSnapshot snapshot, String field) {
        Long value = snapshot.getLong(field);
        return value != null ? value : 0L;
    }

    private static boolean isEmpty(@Nullable String s) {
        return s == null || s.trim().isEmpty();
    }

    /** Dịch exception của Firestore sang thông báo tiếng Việt. */
    @NonNull
    private String toMessage(@NonNull Exception e) {
        if (e instanceof FirebaseFirestoreException) {
            switch (((FirebaseFirestoreException) e).getCode()) {
                case PERMISSION_DENIED:
                    return "Bạn không có quyền thực hiện thao tác này.";
                case UNAVAILABLE:
                    return "Không có kết nối mạng. Vui lòng kiểm tra lại.";
                case NOT_FOUND:
                    return "Không tìm thấy dữ liệu.";
                case DEADLINE_EXCEEDED:
                    return "Kết nối quá chậm. Vui lòng thử lại.";
                default:
                    break;
            }
        }
        return "Đã xảy ra lỗi. Vui lòng thử lại.";
    }
}