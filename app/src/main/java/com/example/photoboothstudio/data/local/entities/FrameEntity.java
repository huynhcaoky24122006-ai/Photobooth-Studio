package com.example.photoboothstudio.data.local.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Objects;

/**
 * Bản ghi khung ảnh trong bảng frames của Room.
 * Các trường tương ứng với model Frame để FrameMapper chuyển đổi dữ liệu.
 * Lưu URL ảnh PNG, không lưu dữ liệu Bitmap trong database.
 */
@Entity(tableName = "frames")
public class FrameEntity {

    // Giữ cùng ID khung ảnh giữa dữ liệu cục bộ và cloud.
    @PrimaryKey
    @NonNull
    private String id;

    private String name;
    private String imageUrl;
    private boolean isVIP;

    // Giá mua bằng Xu; giá 0 không thay thế việc kiểm tra quyền VIP.
    private int priceInCoins;

    // Danh mục/chủ đề của khung ảnh, ví dụ Vintage, Cute hoặc Event.
    private String category;

    // Room sử dụng constructor đầy đủ để khôi phục bản ghi từ database.
    public FrameEntity(@NonNull String id, String name, String imageUrl,
                       boolean isVIP, int priceInCoins, String category) {
        this.id = Objects.requireNonNull(id, "Frame ID must not be null");
        this.name = name;
        this.imageUrl = imageUrl;
        this.isVIP = isVIP;
        this.priceInCoins = priceInCoins;
        this.category = category;
    }

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = Objects.requireNonNull(id, "Frame ID must not be null");
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public boolean isVIP() {
        return isVIP;
    }

    public void setVIP(boolean isVIP) {
        this.isVIP = isVIP;
    }

    public int getPriceInCoins() {
        return priceInCoins;
    }

    public void setPriceInCoins(int priceInCoins) {
        this.priceInCoins = priceInCoins;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
