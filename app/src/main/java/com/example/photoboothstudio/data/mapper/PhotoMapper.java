package com.example.photoboothstudio.data.mapper;

import com.example.photoboothstudio.data.local.entities.PhotoEntity;
import com.example.photoboothstudio.models.Photo;

import java.util.ArrayList;
import java.util.List;

/**
 * PhotoMapper chuyển đổi giữa PhotoEntity (Room) và Photo (model dùng ở UI/Firebase).
 *
 * Quy ước giống FrameMapper:
 * - Sao chép nguyên giá trị từng trường. createdAt giữ nguyên đơn vị milliseconds,
 *   ownerId giữ nguyên (UID Firebase hoặc "GUEST"), isPrivate không bị suy diễn lại.
 * - Mapper không quyết định ảnh nào được phép lưu vào Room (ví dụ ảnh của GUEST);
 *   việc đó thuộc về PhotoRepository.
 * - Đối tượng đơn null → trả về null; danh sách null → danh sách rỗng.
 * - Photo.id có thể null còn PhotoEntity.id bắt buộc khác null, nên toEntity ném
 *   NullPointerException nếu model chưa có ID.
 */
public final class PhotoMapper {

    private PhotoMapper() {
        // Lớp tiện ích, không tạo instance
    }

    /** PhotoEntity → Photo. Trả về null nếu entity là null. */
    public static Photo toModel(PhotoEntity entity) {
        if (entity == null) {
            return null;
        }
        return new Photo(
                entity.getId(),
                entity.getPhotoUrl(),
                entity.getCreatedAt(),
                entity.getOwnerId(),
                entity.isPrivate()
        );
    }

    /**
     * Photo → PhotoEntity. Trả về null nếu model là null.
     *
     * @throws NullPointerException nếu model.getId() là null
     */
    public static PhotoEntity toEntity(Photo model) {
        if (model == null) {
            return null;
        }
        return new PhotoEntity(
                model.getId(),
                model.getPhotoUrl(),
                model.getCreatedAt(),
                model.getOwnerId(),
                model.isPrivate()
        );
    }

    /**
     * Danh sách PhotoEntity → danh sách Photo. Bỏ qua phần tử null.
     * Giữ nguyên thứ tự, nên kết quả của PhotoDao.getPhotosByOwner vẫn xếp mới nhất trước.
     */
    public static List<Photo> toModelList(List<PhotoEntity> entities) {
        if (entities == null) {
            return new ArrayList<>();
        }
        List<Photo> result = new ArrayList<>(entities.size());
        for (PhotoEntity entity : entities) {
            Photo model = toModel(entity);
            if (model != null) {
                result.add(model);
            }
        }
        return result;
    }

    /**
     * Danh sách Photo → danh sách PhotoEntity. Bỏ qua phần tử null.
     * Dùng trực tiếp cho PhotoDao.insertPhotos(List).
     *
     * @throws NullPointerException nếu có Photo nào chưa có ID
     */
    public static List<PhotoEntity> toEntityList(List<Photo> models) {
        if (models == null) {
            return new ArrayList<>();
        }
        List<PhotoEntity> result = new ArrayList<>(models.size());
        for (Photo model : models) {
            PhotoEntity entity = toEntity(model);
            if (entity != null) {
                result.add(entity);
            }
        }
        return result;
    }
}
