package com.example.photoboothstudio.features.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.photoboothstudio.R;
import com.example.photoboothstudio.data.repository.AuthRepository;
import com.example.photoboothstudio.features.admin.DashboardActivity;
import com.example.photoboothstudio.models.User;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

/**
 * AdminLoginActivity quản lý cổng đăng nhập riêng dành cho Quản trị viên (Admin)
 * Xác thực qua Firebase Auth + kiểm tra cờ role "ADMIN" trên Firestore.
 */
public class AdminLoginActivity extends AppCompatActivity {

    private MaterialButton btnBack, btnAdminLogin;
    private TextInputEditText etAdminEmail, etAdminPassword;

    private AuthRepository authRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_login);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        authRepository = AuthRepository.getInstance(this);

        initViews();
        setupListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnAdminLogin = findViewById(R.id.btnAdminLogin);
        etAdminEmail = findViewById(R.id.etAdminEmail);
        etAdminPassword = findViewById(R.id.etAdminPassword);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnAdminLogin.setOnClickListener(v -> handleAdminLogin());
    }

    private void handleAdminLogin() {
        String email = etAdminEmail.getText() != null ? etAdminEmail.getText().toString().trim() : "";
        String password = etAdminPassword.getText() != null ? etAdminPassword.getText().toString().trim() : "";

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập tài khoản và mật khẩu Admin!", Toast.LENGTH_SHORT).show();
            return;
        }

        btnAdminLogin.setEnabled(false);
        Toast.makeText(this, "Đang xác thực tài khoản Admin...", Toast.LENGTH_SHORT).show();

        authRepository.loginAdmin(email, password, new AuthRepository.RepoCallback<User>() {
            @Override
            public void onSuccess(User user) {
                btnAdminLogin.setEnabled(true);
                Toast.makeText(AdminLoginActivity.this, "Đăng nhập Admin thành công!", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(AdminLoginActivity.this, DashboardActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }

            @Override
            public void onError(@NonNull String message) {
                btnAdminLogin.setEnabled(true);
                Toast.makeText(AdminLoginActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }
}
