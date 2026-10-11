package com.example.photoboothstudio.data.local.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Objects;

/**
 * Bản ghi ảnh trong bảng photos của Room.
 * Các trường tương ứng với model Photo để PhotoMapper chuyển đổi dữ liệu.
 * Chỉ lưu thông tin ảnh và URL, không lưu dữ liệu Bitmap trong database.
 */
@Entity(tableName = "photos")
public class PhotoEntity {

    // Giữ cùng ID ảnh giữa dữ liệu cục bộ và cloud, không tự sinh ID trong Room.
    @PrimaryKey
    @NonNull
    private String id;

    private String photoUrl;

    // Thời điểm tạo ảnh, tính bằng milliseconds từ Unix epoch.
    private long createdAt;

    // UID của chủ sở hữu hoặc "GUEST", theo quy ước của model Photo.
    private String ownerId;

    // Cờ dữ liệu riêng tư; quyền truy cập trên cloud cần được kiểm soát riêng.
    private boolean isPrivate;

    // Room sử dụng constructor đầy đủ để khôi phục bản ghi từ database.
    public PhotoEntity(@NonNull String id, String photoUrl, long createdAt,
                       String ownerId, boolean isPrivate) {
        this.id = Objects.requireNonNull(id, "Photo ID must not be null");
        this.photoUrl = photoUrl;
        this.createdAt = createdAt;
        this.ownerId = ownerId;
        this.isPrivate = isPrivate;
    }

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = Objects.requireNonNull(id, "Photo ID must not be null");
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public boolean isPrivate() {
        return isPrivate;
    }

    public void setPrivate(boolean isPrivate) {
        this.isPrivate = isPrivate;
    }
}
