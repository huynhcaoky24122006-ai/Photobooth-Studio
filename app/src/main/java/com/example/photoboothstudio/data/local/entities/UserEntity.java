package com.example.photoboothstudio.data.local.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.example.photoboothstudio.data.local.converters.StringListConverter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Bản ghi người dùng trong bảng users của Room.
 * Các trường tương ứng với model User để UserMapper chuyển đổi dữ liệu.
 * StringListConverter cần triển khai chuyển đổi List<String> và String
 * trước khi sử dụng Entity này trong AppDatabase.
 */
@Entity(tableName = "users")
@TypeConverters(StringListConverter.class)
public class UserEntity {

    // Dùng UID từ Firebase Auth, không tự sinh ID trong Room.
    @PrimaryKey
    @NonNull
    private String id;

    private String email;
    private String name;
    private String role;
    private int coins;
    private boolean isVIP;

    @NonNull
    private List<String> unlockedFrameIds;

    @NonNull
    private List<String> unlockedStickerIds;

    // Room sử dụng constructor đầy đủ để khôi phục bản ghi từ database.
    public UserEntity(@NonNull String id, String email, String name, String role,
                      int coins, boolean isVIP, List<String> unlockedFrameIds,
                      List<String> unlockedStickerIds) {
        this.id = Objects.requireNonNull(id, "User ID must not be null");
        this.email = email;
        this.name = name;
        this.role = role;
        this.coins = coins;
        this.isVIP = isVIP;
        setUnlockedFrameIds(unlockedFrameIds);
        setUnlockedStickerIds(unlockedStickerIds);
    }

    // Constructor tiện dụng cho Member mới; Room bỏ qua constructor này.
    @Ignore
    public UserEntity(@NonNull String id, String email, String name) {
        this(id, email, name, "MEMBER", 0, false, null, null);
    }

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = Objects.requireNonNull(id, "User ID must not be null");
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

    @NonNull
    public List<String> getUnlockedFrameIds() {
        return unlockedFrameIds;
    }

    public void setUnlockedFrameIds(List<String> unlockedFrameIds) {
        this.unlockedFrameIds = unlockedFrameIds != null
                ? new ArrayList<>(unlockedFrameIds) : new ArrayList<>();
    }

    @NonNull
    public List<String> getUnlockedStickerIds() {
        return unlockedStickerIds;
    }

    public void setUnlockedStickerIds(List<String> unlockedStickerIds) {
        this.unlockedStickerIds = unlockedStickerIds != null
                ? new ArrayList<>(unlockedStickerIds) : new ArrayList<>();
    }
}
