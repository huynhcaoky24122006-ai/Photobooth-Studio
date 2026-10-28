package com.example.photoboothstudio.features.auth;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.photoboothstudio.R;
import com.example.photoboothstudio.dialogs.CustomAlertDialog;
import com.example.photoboothstudio.dialogs.LoadingDialog;
import com.example.photoboothstudio.utils.CloudFunctionHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

/**
 * ForgotPasswordActivity quản lý gửi yêu cầu tạo mã OTP qua Cloud Function,
 * tích hợp đồng hồ đếm ngược chống spam nút gửi, và chuyển sang ResetPasswordActivity.
 */
public class ForgotPasswordActivity extends AppCompatActivity {

    private MaterialButton btnBack, btnSendCode, btnVerifyCode;
    private TextInputEditText etEmail, etVerificationCode;
    private TextView tvLogin;

    private CountDownTimer countDownTimer;
    private boolean isCountingDown = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forgot_password);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        setupListeners();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnSendCode = findViewById(R.id.btnSendCode);
        btnVerifyCode = findViewById(R.id.btnVerifyCode);
        etEmail = findViewById(R.id.etEmail);
        etVerificationCode = findViewById(R.id.etVerificationCode);
        tvLogin = findViewById(R.id.tvLogin);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        tvLogin.setOnClickListener(v -> finish());

        // Gửi yêu cầu sinh mã OTP về Gmail qua Cloud Function
        btnSendCode.setOnClickListener(v -> handleSendOtp());

        // Xác nhận mã OTP người dùng nhập và chuyển sang ResetPasswordActivity
        btnVerifyCode.setOnClickListener(v -> handleVerifyOtp());
    }

    private void handleSendOtp() {
        if (isCountingDown) return;

        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        if (email.isEmpty()) {
            CustomAlertDialog.showWarning(this, "Thiếu Email", "Vui lòng nhập Email đã đăng ký để nhận mã OTP!");
            return;
        }

        LoadingDialog.show(this, "Đang gửi mã OTP qua Email...");

        CloudFunctionHelper.sendOtpEmail(email, new CloudFunctionHelper.ApiCallback() {
            @Override
            public void onSuccess() {
                LoadingDialog.dismiss();
                CustomAlertDialog.showSuccess(ForgotPasswordActivity.this, "Đã gửi mã OTP",
                        "Mã xác nhận 6 số đã được gửi tới email " + email + ". Vui lòng kiểm tra hộp thư!");
                startCountdown();
            }

            @Override
            public void onError(@NonNull String error) {
                LoadingDialog.dismiss();
                CustomAlertDialog.showError(ForgotPasswordActivity.this, "Gửi mã thất bại", error);
            }
        });
    }

    private void startCountdown() {
        isCountingDown = true;
        btnSendCode.setEnabled(false);

        countDownTimer = new CountDownTimer(60000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long seconds = millisUntilFinished / 1000;
                btnSendCode.setText("Gửi lại (" + seconds + "s)");
            }

            @Override
            public void onFinish() {
                isCountingDown = false;
                btnSendCode.setEnabled(true);
                btnSendCode.setText(R.string.forgot_password_send_code);
            }
        }.start();
    }

    private void handleVerifyOtp() {
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String inputCode = etVerificationCode.getText() != null ? etVerificationCode.getText().toString().trim() : "";

        if (email.isEmpty()) {
            CustomAlertDialog.showWarning(this, "Thiếu Email", "Vui lòng nhập Email của bạn!");
            return;
        }

        if (inputCode.isEmpty() || inputCode.length() < 6) {
            CustomAlertDialog.showWarning(this, "Thiếu mã OTP", "Vui lòng nhập đầy đủ mã OTP 6 chữ số!");
            return;
        }

        // Chuyển sang ResetPasswordActivity và truyền kèm email và mã otp
        Intent intent = new Intent(ForgotPasswordActivity.this, ResetPasswordActivity.class);
        intent.putExtra("EXTRA_EMAIL", email);
        intent.putExtra("EXTRA_OTP", inputCode);
        startActivity(intent);
        finish();
    }
}
