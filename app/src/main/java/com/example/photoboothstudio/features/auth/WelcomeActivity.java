package com.example.photoboothstudio.features.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.photoboothstudio.R;
import com.example.photoboothstudio.data.local.SharedPrefManager;
import com.example.photoboothstudio.data.repository.AuthRepository;
import com.example.photoboothstudio.features.admin.DashboardActivity;
import com.example.photoboothstudio.features.member.ProfileActivity;
import com.example.photoboothstudio.models.User;
import com.google.android.material.button.MaterialButton;

/**
 * WelcomeActivity là màn hình khởi đầu ứng dụng.
 * Tự động kiểm tra khôi phục phiên đăng nhập (Firebase Auth + Room Cache).
 */
public class WelcomeActivity extends AppCompatActivity {

    private MaterialButton btnMember, btnGuest;
    private TextView tvAdminLogin;
    private AuthRepository authRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.welcome_activity);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        authRepository = AuthRepository.getInstance(this);

        initViews();
        setupListeners();
        checkActiveSession();
    }

    private void initViews() {
        btnMember = findViewById(R.id.btnMember);
        btnGuest = findViewById(R.id.btnGuest);
        tvAdminLogin = findViewById(R.id.tvAdminLogin);
    }

    private void setupListeners() {
        btnMember.setOnClickListener(v -> {
            Intent intent = new Intent(WelcomeActivity.this, MemberLoginActivity.class);
            startActivity(intent);
        });

        tvAdminLogin.setOnClickListener(v -> {
            Intent intent = new Intent(WelcomeActivity.this, AdminLoginActivity.class);
            startActivity(intent);
        });

        btnGuest.setOnClickListener(v -> {
            // Đăng nhập nhanh dưới dạng Khách
            SharedPrefManager.getInstance(this).saveUserSession("GUEST_TEMP", "", "Khách Hàng", "GUEST");
            Intent intent = new Intent(WelcomeActivity.this, ProfileActivity.class);
            startActivity(intent);
            finish();
        });
    }

    /**
     * Tự động kiểm tra phiên đăng nhập còn hiệu lực hay không.
     */
    private void checkActiveSession() {
        authRepository.restoreSession(new AuthRepository.RepoCallback<User>() {
            @Override
            public void onSuccess(User user) {
                if (user != null) {
                    navigateToHome(user);
                }
            }

            @Override
            public void onError(String message) {
                // Phiên hết hạn hoặc không khả dụng -> Người dùng ở lại màn hình Welcome
            }
        });
    }

    private void navigateToHome(User user) {
        Intent intent;
        if ("ADMIN".equalsIgnoreCase(user.getRole())) {
            intent = new Intent(WelcomeActivity.this, DashboardActivity.class);
        } else {
            intent = new Intent(WelcomeActivity.this, ProfileActivity.class);
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
