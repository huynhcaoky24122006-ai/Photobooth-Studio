package com.example.photoboothstudio.models;

import java.io.Serializable;

/**
 * Photo đại diện cho đối tượng Bức ảnh đã hoàn thành
 * ID, link đếm/tải trên Cloud Storage, thời gian tạo, ID chủ sở hữu và quyền riêng tư
 */
public class Photo implements Serializable {
    private String id;          // ID duy nhất của bức ảnh
    private String photoUrl;    // Link URL tải ảnh từ Firebase Storage
    private long createdAt;     // Timestamp thời điểm tạo/chụp ảnh (milliseconds)
    private String ownerId;     // ID người dùng sở hữu (Uid từ Firebase Auth hoặc "GUEST")
    private boolean isPrivate;  // Trạng thái riêng tư (true: chỉ chủ sở hữu xem được, false: công khai)

    // Đừng xóa constructor rỗng
    public Photo() {
    }

    // Constructor đầy đủ tham số
    public Photo(String id, String photoUrl, long createdAt, String ownerId, boolean isPrivate) {
        this.id = id;
        this.photoUrl = photoUrl;
        this.createdAt = createdAt;
        this.ownerId = ownerId;
        this.isPrivate = isPrivate;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public void setPrivate(boolean aPrivate) {
        isPrivate = aPrivate;
    }
}