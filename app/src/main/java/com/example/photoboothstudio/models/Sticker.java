package com.example.photoboothstudio.models;

import java.io.Serializable;

/**
 * Sticker đại diện cho đối tượng Sticker/Hình dán
 *  ID, tên, link ảnh PNG, cờ VIP và giá Xu
 */
public class Sticker implements Serializable {
    private String id;           // ID duy nhất của sticker (vd: "sticker_cat_ears")
    private String name;         // Tên sticker (vd: "Tai mèo xinh")
    private String imageUrl;     // URL ảnh PNG sticker (lưu trên Firebase Storage hoặc Drawable res)
    private boolean isVIP;       // Trạng thái dành riêng cho tài khoản VIP
    private int priceInCoins;    // Giá mua bằng Xu (0 nếu là miễn phí)

    // Đừng xóa constructor rỗng
    public Sticker() {
    }

    // Constructor đầy đủ tham số
    public Sticker(String id, String name, String imageUrl, boolean isVIP, int priceInCoins) {
        this.id = id;
        this.name = name;
        this.imageUrl = imageUrl;
        this.isVIP = isVIP;
        this.priceInCoins = priceInCoins;
    }


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
}