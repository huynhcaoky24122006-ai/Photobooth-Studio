package com.example.photoboothstudio.data.mapper;

import com.example.photoboothstudio.data.local.entities.FrameEntity;
import com.example.photoboothstudio.models.Frame;

import java.util.ArrayList;
import java.util.List;

/**
 * FrameMapper chuyển đổi giữa FrameEntity (Room) và Frame (model dùng ở UI/Firebase).
 *
 * Quy ước chung của các Mapper:
 * - Sao chép nguyên giá trị từng trường, không tự suy diễn. Đặc biệt isVIP và
 *   priceInCoins độc lập với nhau: giá 0 không có nghĩa là khung miễn phí/không VIP.
 * - Đầu vào null cho đối tượng đơn trả về null (DAO trả null khi không tìm thấy bản ghi).
 * - Đầu vào null cho danh sách trả về danh sách rỗng có thể chỉnh sửa.
 * - Model.id có thể null còn FrameEntity.id bắt buộc khác null, nên toEntity ném
 *   NullPointerException nếu model chưa có ID. Repository phải gán ID trước khi lưu vào Room.
 */
public final class FrameMapper {

    private FrameMapper() {
        // Lớp tiện ích, không tạo instance
    }

    /** FrameEntity → Frame. Trả về null nếu entity là null. */
    public static Frame toModel(FrameEntity entity) {
        if (entity == null) {
            return null;
        }
        return new Frame(
                entity.getId(),
                entity.getName(),
                entity.getImageUrl(),
                entity.isVIP(),
                entity.getPriceInCoins(),
                entity.getCategory()
        );
    }

    /**
     * Frame → FrameEntity. Trả về null nếu model là null.
     *
     * @throws NullPointerException nếu model.getId() là null
     */
    public static FrameEntity toEntity(Frame model) {
        if (model == null) {
            return null;
        }
        return new FrameEntity(
                model.getId(),
                model.getName(),
                model.getImageUrl(),
                model.isVIP(),
                model.getPriceInCoins(),
                model.getCategory()
        );
    }

    /** Danh sách FrameEntity → danh sách Frame. Bỏ qua phần tử null. */
    public static List<Frame> toModelList(List<FrameEntity> entities) {
        if (entities == null) {
            return new ArrayList<>();
        }
        List<Frame> result = new ArrayList<>(entities.size());
        for (FrameEntity entity : entities) {
            Frame model = toModel(entity);
            if (model != null) {
                result.add(model);
            }
        }
        return result;
    }

    /**
     * Danh sách Frame → danh sách FrameEntity. Bỏ qua phần tử null.
     * Dùng trực tiếp cho FrameDao.insertFrames(List).
     *
     * @throws NullPointerException nếu có Frame nào chưa có ID
     */
    public static List<FrameEntity> toEntityList(List<Frame> models) {
        if (models == null) {
            return new ArrayList<>();
        }
        List<FrameEntity> result = new ArrayList<>(models.size());
        for (Frame model : models) {
            FrameEntity entity = toEntity(model);
            if (entity != null) {
                result.add(entity);
            }
        }
        return result;
    }
}
