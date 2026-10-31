package com.example.photoboothstudio.features.auth;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.photoboothstudio.R;
import com.example.photoboothstudio.dialogs.CustomAlertDialog;
import com.example.photoboothstudio.dialogs.LoadingDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONObject;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * ResetPasswordActivity gọi Cloud Function verifyAndResetPassword để xác thực OTP
 * và cập nhật mật khẩu trực tiếp trên Firebase Auth (Admin SDK).
 */
public class ResetPasswordActivity extends AppCompatActivity {

    private static final String RESET_URL = "https://us-central1-photobooth-studio-5bea1.cloudfunctions.net/verifyAndResetPassword";

    private MaterialButton btnBack, btnResetPassword;
    private TextInputEditText etNewPassword, etConfirmNewPassword;
    private TextView tvLogin;
    private String userEmail;
    private String userOtp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_reset_password);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        if (getIntent() != null) {
            userEmail = getIntent().getStringExtra("EXTRA_EMAIL");
            userOtp = getIntent().getStringExtra("EXTRA_OTP");
        }

        initViews();
        setupListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnResetPassword = findViewById(R.id.btnResetPassword);
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmNewPassword = findViewById(R.id.etConfirmNewPassword);
        tvLogin = findViewById(R.id.tvLogin);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        tvLogin.setOnClickListener(v -> finish());

        btnResetPassword.setOnClickListener(v -> handleResetPassword());
    }

    private void handleResetPassword() {
        String newPassword = etNewPassword.getText() != null ? etNewPassword.getText().toString().trim() : "";
        String confirmPassword = etConfirmNewPassword.getText() != null ? etConfirmNewPassword.getText().toString().trim() : "";

        if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
            CustomAlertDialog.showWarning(this, "Thiếu thông tin", "Vui lòng nhập đầy đủ mật khẩu mới!");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            CustomAlertDialog.showWarning(this, "Mật khẩu không khớp", "Mật khẩu nhập lại không khớp nhau!");
            return;
        }

        if (newPassword.length() < 6) {
            CustomAlertDialog.showWarning(this, "Mật khẩu quá ngắn", "Mật khẩu phải chứa ít nhất 6 ký tự!");
            return;
        }

        if (userEmail == null || userOtp == null) {
            CustomAlertDialog.showError(this, "Lỗi", "Thông tin xác thực không hợp lệ. Vui lòng thử lại từ đầu!");
            return;
        }

        btnResetPassword.setEnabled(false);
        LoadingDialog.show(this, "Đang cập nhật mật khẩu mới...");

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler mainHandler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            try {
                URL url = new URL(RESET_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);
                conn.setConnectTimeout(12000);
                conn.setReadTimeout(12000);

                JSONObject payload = new JSONObject();
                payload.put("email", userEmail.trim());
                payload.put("otpCode", userOtp.trim());
                payload.put("newPassword", newPassword);

                byte[] body = payload.toString().getBytes(StandardCharsets.UTF_8);
                try (OutputStream os = conn.getOutputStream()) {
                    os.write(body);
                    os.flush();
                }

                int responseCode = conn.getResponseCode();
                if (responseCode == 200 || responseCode == 201) {
                    mainHandler.post(() -> {
                        LoadingDialog.dismiss();
                        btnResetPassword.setEnabled(true);
                        CustomAlertDialog.showSuccess(ResetPasswordActivity.this, "Đổi mật khẩu thành công!",
                                "Mật khẩu của bạn đã được cập nhật thành công trên hệ thống. Vui lòng đăng nhập lại bằng mật khẩu mới.",
                                () -> {
                                    Intent intent = new Intent(ResetPasswordActivity.this, MemberLoginActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                    finish();
                                });
                    });
                } else {
                    InputStream errStream = conn.getErrorStream();
                    String errText = "";
                    if (errStream != null) {
                        Scanner s = new Scanner(errStream).useDelimiter("\\A");
                        errText = s.hasNext() ? s.next() : "";
                    }
                    final String detailError = errText;
                    mainHandler.post(() -> {
                        LoadingDialog.dismiss();
                        btnResetPassword.setEnabled(true);
                        CustomAlertDialog.showError(ResetPasswordActivity.this, "Đổi mật khẩu thất bại",
                                detailError.isEmpty() ? "Mã OTP không chính xác hoặc đã hết hạn!" : detailError);
                    });
                }
                conn.disconnect();
            } catch (Exception e) {
                mainHandler.post(() -> {
                    LoadingDialog.dismiss();
                    btnResetPassword.setEnabled(true);
                    CustomAlertDialog.showError(ResetPasswordActivity.this, "Lỗi kết nối", "Không thể kết nối máy chủ. Vui lòng kiểm tra lại mạng!");
                });
            }
        });
    }
}
