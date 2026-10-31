package com.example.photoboothstudio.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.example.photoboothstudio.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

/**
 * Khối Alert dùng chung cho mọi thông báo và xác nhận.
 *
 *   CustomAlertDialog.showSuccess(this, "Đăng ký thành công", "Chào mừng bạn đến với Photobooth Studio!");
 *   CustomAlertDialog.showError(this, "Sai mật khẩu", "Mật khẩu không đúng, vui lòng thử lại.");
 *   CustomAlertDialog.showWarning(this, "Không đủ Xu", "Bạn cần thêm Xu để mua khung này.");
 *   CustomAlertDialog.showConfirm(this, "Đăng xuất", "Bạn có chắc muốn đăng xuất không?", () -> logout());
 */
public final class CustomAlertDialog {

    private enum Type { SUCCESS, ERROR, WARNING, CONFIRM }

    private CustomAlertDialog() {}

    // ================= Thành công =================

    public static void showSuccess(Context context, String title, String message) {
        show(context, Type.SUCCESS, title, message, null, null, true);
    }

    public static void showSuccess(Context context, String title, String message, Runnable onOk) {
        show(context, Type.SUCCESS, title, message, null, onOk, onOk == null);
    }

    // ================= Lỗi =================

    public static void showError(Context context, String title, String message) {
        show(context, Type.ERROR, title, message, null, null, true);
    }

    // ================= Cảnh báo =================

    public static void showWarning(Context context, String title, String message) {
        show(context, Type.WARNING, title, message, null, null, true);
    }

    // ================= Xác nhận (có nút Hủy) =================

    public static void showConfirm(Context context, String title, String message, Runnable onConfirm) {
        show(context, Type.CONFIRM, title, message, null, onConfirm, true);
    }

    public static void showConfirm(Context context, String title, String message,
                                   String confirmText, Runnable onConfirm) {
        show(context, Type.CONFIRM, title, message, confirmText, onConfirm, true);
    }

    // ================= Hàm dựng chung =================

    private static void show(Context context, Type type, String title, String message,
                             String positiveText, Runnable onPositive, boolean cancelable) {
        if (context instanceof Activity) {
            Activity a = (Activity) context;
            if (a.isFinishing() || a.isDestroyed()) return;
        }

        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_alert);
        dialog.setCancelable(cancelable);

        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setDimAmount(0.5f);
            DisplayMetrics dm = context.getResources().getDisplayMetrics();
            int width = Math.min((int) (dm.widthPixels * 0.86f), (int) (340 * dm.density));
            window.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        MaterialCardView cardIcon = dialog.findViewById(R.id.cardIcon);
        ImageView ivIcon = dialog.findViewById(R.id.ivIcon);
        TextView tvTitle = dialog.findViewById(R.id.tvTitle);
        TextView tvMessage = dialog.findViewById(R.id.tvMessage);
        MaterialButton btnPositive = dialog.findViewById(R.id.btnPositive);
        MaterialButton btnNegative = dialog.findViewById(R.id.btnNegative);

        int iconRes;
        int iconColor;
        int circleColor;
        String defaultPositive;
        switch (type) {
            case SUCCESS:
                iconRes = R.drawable.ic_dialog_success;
                iconColor = R.color.success_green;
                circleColor = R.color.success_bg;
                defaultPositive = "OK";
                break;
            case ERROR:
                iconRes = R.drawable.ic_dialog_error;
                iconColor = R.color.error_red;
                circleColor = R.color.error_bg;
                defaultPositive = "Đóng";
                break;
            case WARNING:
                iconRes = R.drawable.ic_dialog_warning;
                iconColor = R.color.dialog_warning_icon;
                circleColor = R.color.warning_bg;
                defaultPositive = "Đã hiểu";
                break;
            default: // CONFIRM
                iconRes = R.drawable.ic_dialog_question;
                iconColor = R.color.purple_primary;
                circleColor = R.color.avatar_bg;
                defaultPositive = "Xác nhận";
                break;
        }

        if (cardIcon != null) {
            cardIcon.setCardBackgroundColor(ContextCompat.getColor(context, circleColor));
        }
        if (ivIcon != null) {
            ivIcon.setImageResource(iconRes);
            ivIcon.setColorFilter(ContextCompat.getColor(context, iconColor));
        }

        if (tvTitle != null) tvTitle.setText(title);
        if (tvMessage != null) tvMessage.setText(message);

        if (btnPositive != null) {
            btnPositive.setText(positiveText != null ? positiveText : defaultPositive);
            btnPositive.setOnClickListener(v -> {
                dialog.dismiss();
                if (onPositive != null) onPositive.run();
            });
        }

        if (type == Type.CONFIRM && btnNegative != null) {
            btnNegative.setText("Hủy");
            btnNegative.setVisibility(View.VISIBLE);
            btnNegative.setOnClickListener(v -> dialog.dismiss());
        }

        dialog.show();
    }
}
