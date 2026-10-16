package com.example.photoboothstudio.data.mapper;

import com.example.photoboothstudio.data.local.entities.UserEntity;
import com.example.photoboothstudio.models.User;

import java.util.ArrayList;
import java.util.List;

/**
 * UserMapper chuyển đổi giữa UserEntity (Room) và User (model dùng ở UI/Firebase).
 *
 * Quy ước giống FrameMapper, cộng thêm các lưu ý riêng của User:
 * - unlockedFrameIds / unlockedStickerIds được SAO CHÉP sang danh sách mới ở cả hai chiều.
 *   Constructor và setter của User không tự copy, nên nếu chuyển thẳng tham chiếu thì
 *   User và UserEntity sẽ dùng chung một List: sửa danh sách trên model (ví dụ khi mua
 *   Frame trong Shop) sẽ vô tình sửa luôn entity đang cache.
 * - Danh sách null phía model được hiểu là danh sách rỗng (UserEntity tự xử lý),
 *   nên User lấy từ entity luôn có danh sách khác null.
 * - Phần tử null trong hai danh sách này không bị mapper âm thầm lọc bỏ; StringListConverter
 *   sẽ ném IllegalArgumentException khi ghi xuống Room.
 * - role (GUEST/MEMBER/ADMIN), coins và isVIP được giữ nguyên, không chuẩn hóa lại.
 * - User.id có thể null còn UserEntity.id bắt buộc khác null, nên toEntity ném
 *   NullPointerException nếu model chưa có ID (ID chính là UID từ Firebase Auth).
 */
public final class UserMapper {

    private UserMapper() {
        // Lớp tiện ích, không tạo instance
    }

    /** UserEntity → User. Trả về null nếu entity là null (không có user trong Room). */
    public static User toModel(UserEntity entity) {
        if (entity == null) {
            return null;
        }
        return new User(
                entity.getId(),
                entity.getEmail(),
                entity.getName(),
                entity.getRole(),
                entity.getCoins(),
                entity.isVIP(),
                new ArrayList<>(entity.getUnlockedFrameIds()),
                new ArrayList<>(entity.getUnlockedStickerIds())
        );
    }

    /**
     * User → UserEntity. Trả về null nếu model là null.
     * UserEntity tự tạo bản sao của hai danh sách nên không cần copy lại ở đây.
     *
     * @throws NullPointerException nếu model.getId() là null
     */
    public static UserEntity toEntity(User model) {
        if (model == null) {
            return null;
        }
        return new UserEntity(
                model.getId(),
                model.getEmail(),
                model.getName(),
                model.getRole(),
                model.getCoins(),
                model.isVIP(),
                model.getUnlockedFrameIds(),
                model.getUnlockedStickerIds()
        );
    }

    /** Danh sách UserEntity → danh sách User. Bỏ qua phần tử null. */
    public static List<User> toModelList(List<UserEntity> entities) {
        if (entities == null) {
            return new ArrayList<>();
        }
        List<User> result = new ArrayList<>(entities.size());
        for (UserEntity entity : entities) {
            User model = toModel(entity);
            if (model != null) {
                result.add(model);
            }
        }
        return result;
    }

    /**
     * Danh sách User → danh sách UserEntity. Bỏ qua phần tử null.
     *
     * @throws NullPointerException nếu có User nào chưa có ID
     */
    public static List<UserEntity> toEntityList(List<User> models) {
        if (models == null) {
            return new ArrayList<>();
        }
        List<UserEntity> result = new ArrayList<>(models.size());
        for (User model : models) {
            UserEntity entity = toEntity(model);
            if (entity != null) {
                result.add(entity);
            }
        }
        return result;
    }
}
