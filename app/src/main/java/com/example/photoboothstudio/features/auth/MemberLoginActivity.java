package com.example.photoboothstudio.features.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.photoboothstudio.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class MemberLoginActivity extends AppCompatActivity {

    private MaterialButton btnBack, btnLogin;
    private TextInputEditText etEmail, etPassword;
    private TextView tvForgotPassword, tvRegister;

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

        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
            String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ Email và Mật khẩu!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
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
            if (!recoveryEmail.isEmpty()) {
                Toast.makeText(this, "Đã gửi hướng dẫn khôi phục mật khẩu tới " + recoveryEmail, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Vui lòng nhập email hợp lệ!", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.cancel());

        builder.show();
    }
}
