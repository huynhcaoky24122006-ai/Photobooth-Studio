package com.example.photoboothstudio.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Window;
import android.widget.TextView;

import com.example.photoboothstudio.R;

import java.lang.ref.WeakReference;

/**
 * Khối Loading dùng chung toàn app:
 *
 *   LoadingDialog.show(this, "Đang đăng nhập...");   // hiện
 *   LoadingDialog.dismiss();                         // tắt khi xong
 */
public final class LoadingDialog {

    private static final String DEFAULT_MESSAGE = "Đang xử lý...";

    private static WeakReference<Dialog> dialogRef = new WeakReference<>(null);
    private static WeakReference<Context> ownerRef = new WeakReference<>(null);

    private LoadingDialog() {}

    public static void show(Context context) {
        show(context, DEFAULT_MESSAGE);
    }

    public static void show(Context context, String message) {
        if (!isAlive(context)) return;

        Dialog current = dialogRef.get();
        if (current != null && current.isShowing() && ownerRef.get() == context) {
            setMessage(current, message);
            return;
        }
        dismiss();

        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_loading);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setDimAmount(0.5f);
        }
        setMessage(dialog, message);

        dialogRef = new WeakReference<>(dialog);
        ownerRef = new WeakReference<>(context);
        dialog.show();
    }

    public static void updateMessage(String message) {
        Dialog d = dialogRef.get();
        if (d != null && d.isShowing()) setMessage(d, message);
    }

    public static void dismiss() {
        Dialog d = dialogRef.get();
        if (d != null && d.isShowing()) {
            try {
                d.dismiss();
            } catch (IllegalArgumentException ignored) {
            }
        }
        dialogRef = new WeakReference<>(null);
        ownerRef = new WeakReference<>(null);
    }

    public static boolean isShowing() {
        Dialog d = dialogRef.get();
        return d != null && d.isShowing();
    }

    private static void setMessage(Dialog dialog, String message) {
        TextView tv = dialog.findViewById(R.id.tvLoadingMessage);
        if (tv != null) tv.setText(message != null ? message : DEFAULT_MESSAGE);
    }

    private static boolean isAlive(Context context) {
        if (context instanceof Activity) {
            Activity a = (Activity) context;
            return !a.isFinishing() && !a.isDestroyed();
        }
        return context != null;
    }
}
