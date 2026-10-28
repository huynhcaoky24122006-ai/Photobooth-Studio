package com.example.photoboothstudio.features.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.photoboothstudio.R;
import com.example.photoboothstudio.data.repository.AuthRepository;
import com.example.photoboothstudio.dialogs.CustomAlertDialog;
import com.example.photoboothstudio.dialogs.LoadingDialog;
import com.example.photoboothstudio.features.member.ProfileActivity;
import com.example.photoboothstudio.models.User;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

/**
 * MemberLoginActivity quản lý đăng nhập cho Thành viên bằng Firebase Auth + Firestore.
 */
public class MemberLoginActivity extends AppCompatActivity {

    private MaterialButton btnBack, btnLogin;
    private TextInputEditText etEmail, etPassword;
    private TextView tvForgotPassword, tvRegister;

    private AuthRepository authRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_member_login);

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
        btnLogin = findViewById(R.id.btnLogin);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        tvRegister = findViewById(R.id.tvRegister);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        tvRegister.setOnClickListener(v -> {
            Intent intent = new Intent(MemberLoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        tvForgotPassword.setOnClickListener(v -> showForgotPasswordDialog());

        btnLogin.setOnClickListener(v -> handleLogin());
    }

    private void handleLogin() {
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";

        if (email.isEmpty() || password.isEmpty()) {
            CustomAlertDialog.showWarning(this, "Thiếu thông tin", "Vui lòng nhập đầy đủ Email và Mật khẩu!");
            return;
        }

        btnLogin.setEnabled(false);
        LoadingDialog.show(this, "Đang đăng nhập...");

        authRepository.loginMember(email, password, new AuthRepository.RepoCallback<User>() {
            @Override
            public void onSuccess(User user) {
                LoadingDialog.dismiss();
                btnLogin.setEnabled(true);

                Intent intent = new Intent(MemberLoginActivity.this, ProfileActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }

            @Override
            public void onError(@NonNull String message) {
                LoadingDialog.dismiss();
                btnLogin.setEnabled(true);
                CustomAlertDialog.showError(MemberLoginActivity.this, "Đăng nhập thất bại", message);
            }
        });
    }

    private void showForgotPasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Khôi phục mật khẩu");

        final android.widget.EditText input = new android.widget.EditText(this);
        input.setHint("Nhập email đăng ký của bạn");
        input.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        builder.setView(input);

        builder.setPositiveButton("Gửi yêu cầu", (dialog, which) -> {
            String recoveryEmail = input.getText().toString().trim();
            if (recoveryEmail.isEmpty()) {
                CustomAlertDialog.showWarning(this, "Thiếu thông tin", "Vui lòng nhập email hợp lệ!");
                return;
            }

            LoadingDialog.show(this, "Đang gửi yêu cầu...");
            authRepository.sendPasswordReset(recoveryEmail, new AuthRepository.RepoCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    LoadingDialog.dismiss();
                    CustomAlertDialog.showSuccess(MemberLoginActivity.this, "Thành công",
                            "Đã gửi email khôi phục mật khẩu tới " + recoveryEmail);
                }

                @Override
                public void onError(@NonNull String message) {
                    LoadingDialog.dismiss();
                    CustomAlertDialog.showError(MemberLoginActivity.this, "Gửi thất bại", message);
                }
            });
        });
        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.cancel());

        builder.show();
    }
}
