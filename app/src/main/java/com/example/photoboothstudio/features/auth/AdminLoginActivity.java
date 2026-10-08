package com.example.photoboothstudio.features.auth;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.photoboothstudio.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class AdminLoginActivity extends AppCompatActivity {

    private MaterialButton btnBack, btnAdminLogin;
    private TextInputEditText etAdminEmail, etAdminPassword;

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

        btnAdminLogin.setOnClickListener(v -> {
            String email = etAdminEmail.getText() != null ? etAdminEmail.getText().toString().trim() : "";
            String password = etAdminPassword.getText() != null ? etAdminPassword.getText().toString().trim() : "";

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập tài khoản và mật khẩu Admin!", Toast.LENGTH_SHORT).show();
            } else if ("admin@photobooth.com".equals(email) && "admin123".equals(password)) {
                Toast.makeText(this, "Đăng nhập Admin thành công!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Tài khoản hoặc mật khẩu Admin không chính xác!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
