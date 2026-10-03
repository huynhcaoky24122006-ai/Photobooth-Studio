package com.example.photoboothstudio.data.local;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * SharedPrefManager quản lý lưu trữ Session & Cache cục bộ (Level 0)
 * Lưu thông tin User ID, Role, Token đăng nhập, Ngày điểm danh gần nhất...
 */
public class SharedPrefManager {
    private static final String PREF_NAME = "photobooth_studio_pref";

    // Keys
    private static final String KEY_USER_ID = "key_user_id";
    private static final String KEY_USER_EMAIL = "key_user_email";
    private static final String KEY_USER_NAME = "key_user_name";
    private static final String KEY_USER_ROLE = "key_user_role"; // "GUEST", "MEMBER", "ADMIN"
    private static final String KEY_IS_LOGGED_IN = "key_is_logged_in";
    private static final String KEY_LAST_CHECKIN_DATE = "key_last_checkin_date";

    private static SharedPrefManager instance;
    private final SharedPreferences sharedPreferences;

    private SharedPrefManager(Context context) {
        sharedPreferences = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    // Singleton Pattern để đảm bảo chỉ tạo 1 instance duy nhất
    public static synchronized SharedPrefManager getInstance(Context context) {
        if (instance == null) {
            instance = new SharedPrefManager(context);
        }
        return instance;
    }

    // --- Luồng Quản lý Session Đăng Nhập ---

    public void saveUserSession(String userId, String email, String name, String role) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_USER_ID, userId);
        editor.putString(KEY_USER_EMAIL, email);
        editor.putString(KEY_USER_NAME, name);
        editor.putString(KEY_USER_ROLE, role);
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.apply();
    }

    public boolean isLoggedIn() {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public String getUserId() {
        return sharedPreferences.getString(KEY_USER_ID, null);
    }

    public String getUserEmail() {
        return sharedPreferences.getString(KEY_USER_EMAIL, "");
    }

    public String getUserName() {
        return sharedPreferences.getString(KEY_USER_NAME, "Khách");
    }

    public String getUserRole() {
        return sharedPreferences.getString(KEY_USER_ROLE, "GUEST");
    }

    public void setUserRole(String role) {
        sharedPreferences.edit().putString(KEY_USER_ROLE, role).apply();
    }

    public void clearSession() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }

    // --- Quản lý Điểm danh Hàng ngày (Daily Check-in) ---

    public void setLastCheckinDate(String dateStr) {
        sharedPreferences.edit().putString(KEY_LAST_CHECKIN_DATE, dateStr).apply();
    }

    public String getLastCheckinDate() {
        return sharedPreferences.getString(KEY_LAST_CHECKIN_DATE, "");
    }
}