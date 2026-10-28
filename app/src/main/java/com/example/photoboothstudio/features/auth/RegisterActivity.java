package com.example.photoboothstudio.features.auth;

import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.photoboothstudio.R;
import com.example.photoboothstudio.data.repository.AuthRepository;
import com.example.photoboothstudio.dialogs.CustomAlertDialog;
import com.example.photoboothstudio.dialogs.LoadingDialog;
import com.example.photoboothstudio.models.User;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

/**
 * RegisterActivity quản lý đăng ký tài khoản Member mới lên Firebase Auth & Firestore.
 * Sau khi đăng ký thành công, tự động đăng xuất và quay về màn hình Đăng nhập (MemberLoginActivity).
 */
public class RegisterActivity extends AppCompatActivity {

    private MaterialButton btnBack, btnRegister;
    private TextInputEditText etName, etEmail, etPassword, etConfirmPassword;
    private TextView tvLogin;

    private AuthRepository authRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

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
        btnRegister = findViewById(R.id.btnRegister);
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        tvLogin = findViewById(R.id.tvLogin);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        tvLogin.setOnClickListener(v -> finish());

        btnRegister.setOnClickListener(v -> handleRegister());
    }

    private void handleRegister() {
        String name = etName.getText() != null ? etName.getText().toString().trim() : "";
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";
        String confirmPassword = etConfirmPassword.getText() != null ? etConfirmPassword.getText().toString().trim() : "";

        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            CustomAlertDialog.showWarning(this, "Thiếu thông tin", "Vui lòng nhập đầy đủ thông tin!");
            return;
        }

        if (!password.equals(confirmPassword)) {
            CustomAlertDialog.showWarning(this, "Mật khẩu không khớp", "Mật khẩu nhập lại không khớp!");
            return;
        }

        if (password.length() < 6) {
            CustomAlertDialog.showWarning(this, "Mật khẩu quá ngắn", "Mật khẩu phải có ít nhất 6 ký tự!");
            return;
        }

        btnRegister.setEnabled(false);
        LoadingDialog.show(this, "Đang đăng ký...");

        authRepository.registerMember(email, password, name, new AuthRepository.RepoCallback<User>() {
            @Override
            public void onSuccess(User user) {
                LoadingDialog.dismiss();
                btnRegister.setEnabled(true);

                // Đăng xuất ngay sau khi đăng ký thành công
                authRepository.logout();

                CustomAlertDialog.showSuccess(RegisterActivity.this, "Đăng ký thành công",
                        "Tài khoản đã tạo thành công! Chào mừng bạn đến với Photobooth Studio. Vui lòng đăng nhập để bắt đầu.",
                        () -> finish());
            }

            @Override
            public void onError(@NonNull String message) {
                LoadingDialog.dismiss();
                btnRegister.setEnabled(true);
                CustomAlertDialog.showError(RegisterActivity.this, "Đăng ký thất bại", message);
            }
        });
    }
}
