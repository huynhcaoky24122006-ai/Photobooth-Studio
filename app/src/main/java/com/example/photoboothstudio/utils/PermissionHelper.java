package com.example.photoboothstudio.utils;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import androidx.core.content.ContextCompat;

/**
 * PermissionHelper - kiểm tra và xử lý quyền Camera / bộ nhớ dùng chung.
 */
public final class PermissionHelper {

    public static final String[] CAMERA_PERMISSIONS = {
            Manifest.permission.CAMERA
    };

    private PermissionHelper() {
    }

    public static boolean hasCameraPermission(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Từ Android 10 (Q) trở lên, lưu ảnh qua MediaStore không cần quyền.
     * Các phiên bản cũ cần WRITE_EXTERNAL_STORAGE.
     */
    public static boolean hasStoragePermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return true;
        }
        return ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;
    }

    public static String[] storagePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return new String[0];
        }
        return new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
    }

    /**
     * Mở màn hình cài đặt của ứng dụng để người dùng tự cấp quyền khi đã từ chối vĩnh viễn.
     */
    public static void openAppSettings(Context context) {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.fromParts("package", context.getPackageName(), null));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
}
