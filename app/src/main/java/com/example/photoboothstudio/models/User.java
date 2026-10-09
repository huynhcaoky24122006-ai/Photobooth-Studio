package com.example.photoboothstudio.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * User đại diện cho đối tượng Người dùng / Thành viên
 * ID, Email, Vai trò, Số xu, Trạng thái VIP và danh sách tài nguyên đã sở hữu
 */
public class User implements Serializable {
    private String id;                       // ID duy nhất của User (Uid từ Firebase Auth)
    private String email;                    // Email đăng nhập
    private String name;            // Tên hiển thị người dùng
    private String role;          // Vai trò: "GUEST", "MEMBER", "ADMIN"
    private int coins;             // Số lượng Xu hiện có
    private boolean isVIP;                   // Trạng thái tài khoản VIP
    private List<String> unlockedFrameIds;   // Danh sách ID các Frame đã mua/mở khóa
    private List<String> unlockedStickerIds; // Danh sách ID các Sticker đã mua/mở khóa

    // Đừng xóa constructor rỗng
    public User() {
        this.unlockedFrameIds = new ArrayList<>();
        this.unlockedStickerIds = new ArrayList<>();
    }

    // Constructor cơ bản khi khởi tạo tài khoản Member mới
    public User(String id, String email, String name) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.role = "MEMBER";
        this.coins = 0;
        this.isVIP = false;
        this.unlockedFrameIds = new ArrayList<>();
        this.unlockedStickerIds = new ArrayList<>();
    }

    // Constructor đầy đủ tham số
    public User(String id, String email, String name, String role, int coins, boolean isVIP, List<String> unlockedFrameIds, List<String> unlockedStickerIds) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.role = role;
        this.coins = coins;
        this.isVIP = isVIP;
        this.unlockedFrameIds = unlockedFrameIds != null ? unlockedFrameIds : new ArrayList<>();
        this.unlockedStickerIds = unlockedStickerIds != null ? unlockedStickerIds : new ArrayList<>();
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public int getCoins() {
        return coins;
    }

    public void setCoins(int coins) {
        this.coins = coins;
    }

    public boolean isVIP() {
        return isVIP;
    }

    public void setVIP(boolean isVIP) {
        this.isVIP = isVIP;
    }

    public List<String> getUnlockedFrameIds() {
        return unlockedFrameIds;
    }

    public void setUnlockedFrameIds(List<String> unlockedFrameIds) {
        this.unlockedFrameIds = unlockedFrameIds;
    }

    public List<String> getUnlockedStickerIds() {
        return unlockedStickerIds;
    }

    public void setUnlockedStickerIds(List<String> unlockedStickerIds) {
        this.unlockedStickerIds = unlockedStickerIds;
    }
}