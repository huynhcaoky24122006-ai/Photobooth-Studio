package com.example.photoboothstudio.features.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.photoboothstudio.R;
import com.google.android.material.button.MaterialButton;

public class WelcomeActivity extends AppCompatActivity {

    private MaterialButton btnMember, btnGuest;
    private TextView tvAdminLogin;

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

        initViews();
        setupListeners();
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
            // Guest quick entry flow
        });
    }
}
