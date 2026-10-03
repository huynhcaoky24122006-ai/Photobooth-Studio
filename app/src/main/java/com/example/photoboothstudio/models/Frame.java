package com.example.photoboothstudio.models;

import java.io.Serializable;

/**
 * Frame đại diện cho đối tượng Khung ảnh (Level 0)
 * Chứa thông tin cấu hình khung: ID, link ảnh PNG, cờ VIP, giá Xu và loại danh mục
 */
public class Frame implements Serializable {
    private String id;           // ID duy nhất (vd: "frame_001")
    private String name;         // Tên khung (vd: "Khung Chào Xuân")
    private String imageUrl;     // URL ảnh PNG viền khung (lưu trên Firebase Storage)
    private boolean isVIP;       // Kiểm tra VIP
    private int priceInCoins;    // Giá mua bằng Xu (0 nếu là miễn phí)
    private String category;     // Danh mục/Chủ đề (vd: "Vintage", "Cute", "Event")

    // Đừng có xóa constructor rỗng
    public Frame() {
    }

    // Constructor đầy đủ tham số
    public Frame(String id, String name, String imageUrl, boolean isVIP, int priceInCoins, String category) {
        this.id = id;
        this.name = name;
        this.imageUrl = imageUrl;
        this.isVIP = isVIP;
        this.priceInCoins = priceInCoins;
        this.category = category;
    }

    // --- Getters and Setters ---

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public void setVIP(boolean VIP) {
        isVIP = VIP;
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