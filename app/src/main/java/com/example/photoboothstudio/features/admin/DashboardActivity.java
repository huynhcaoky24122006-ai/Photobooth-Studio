package com.example.photoboothstudio.features.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.example.photoboothstudio.data.repository.AuthRepository;
import com.example.photoboothstudio.dialogs.CustomAlertDialog;
import com.example.photoboothstudio.features.auth.WelcomeActivity;

public class DashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Giao diện tạm, sẽ thay bằng layout Dashboard thật sau
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setPadding(48, 48, 48, 48);

        TextView tv = new TextView(this);
        tv.setText("Admin Dashboard (đang xây dựng)");
        tv.setTextSize(20);
        tv.setGravity(Gravity.CENTER);

        Button btnLogout = new Button(this);
        btnLogout.setText("Đăng xuất");
        btnLogout.setOnClickListener(v ->
                CustomAlertDialog.showConfirm(this, "Đăng xuất",
                        "Bạn có chắc muốn đăng xuất không?", "Đăng xuất", this::logout));

        root.addView(tv);
        root.addView(btnLogout);
        setContentView(root, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));
    }

    private void logout() {
        AuthRepository.getInstance(this).logout();
        Intent intent = new Intent(this, WelcomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}