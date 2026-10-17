package com.example.photoboothstudio.data.mapper;

import com.example.photoboothstudio.data.local.entities.StickerEntity;
import com.example.photoboothstudio.models.Sticker;

import java.util.ArrayList;
import java.util.List;

/**
 * StickerMapper chuyển đổi giữa StickerEntity (Room) và Sticker (model dùng ở UI/Firebase).
 *
 * Quy ước giống FrameMapper:
 * - Sao chép nguyên giá trị từng trường; isVIP và priceInCoins độc lập với nhau.
 * - Đối tượng đơn null → trả về null; danh sách null → danh sách rỗng.
 * - Sticker.id có thể null còn StickerEntity.id bắt buộc khác null, nên toEntity ném
 *   NullPointerException nếu model chưa có ID.
 */
public final class StickerMapper {

    private StickerMapper() {
        // Lớp tiện ích, không tạo instance
    }

    /** StickerEntity → Sticker. Trả về null nếu entity là null. */
    public static Sticker toModel(StickerEntity entity) {
        if (entity == null) {
            return null;
        }
        return new Sticker(
                entity.getId(),
                entity.getName(),
                entity.getImageUrl(),
                entity.isVIP(),
                entity.getPriceInCoins()
        );
    }

    /**
     * Sticker → StickerEntity. Trả về null nếu model là null.
     *
     * @throws NullPointerException nếu model.getId() là null
     */
    public static StickerEntity toEntity(Sticker model) {
        if (model == null) {
            return null;
        }
        return new StickerEntity(
                model.getId(),
                model.getName(),
                model.getImageUrl(),
                model.isVIP(),
                model.getPriceInCoins()
        );
    }

    /** Danh sách StickerEntity → danh sách Sticker. Bỏ qua phần tử null. */
    public static List<Sticker> toModelList(List<StickerEntity> entities) {
        if (entities == null) {
            return new ArrayList<>();
        }
        List<Sticker> result = new ArrayList<>(entities.size());
        for (StickerEntity entity : entities) {
            Sticker model = toModel(entity);
            if (model != null) {
                result.add(model);
            }
        }
        return result;
    }

    /**
     * Danh sách Sticker → danh sách StickerEntity. Bỏ qua phần tử null.
     * Dùng trực tiếp cho StickerDao.insertStickers(List).
     *
     * @throws NullPointerException nếu có Sticker nào chưa có ID
     */
    public static List<StickerEntity> toEntityList(List<Sticker> models) {
        if (models == null) {
            return new ArrayList<>();
        }
        List<StickerEntity> result = new ArrayList<>(models.size());
        for (Sticker model : models) {
            StickerEntity entity = toEntity(model);
            if (entity != null) {
                result.add(entity);
            }
        }
        return result;
    }
}
