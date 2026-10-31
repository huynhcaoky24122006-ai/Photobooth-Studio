package com.example.photoboothstudio.features.export;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.photoboothstudio.R;
import com.example.photoboothstudio.data.local.SharedPrefManager;
import com.example.photoboothstudio.dialogs.CustomAlertDialog;
import com.example.photoboothstudio.features.admin.DashboardActivity;
import com.example.photoboothstudio.features.auth.WelcomeActivity;
import com.example.photoboothstudio.features.camera.CameraActivity;
import com.example.photoboothstudio.features.member.ProfileActivity;
import com.example.photoboothstudio.utils.CameraUtils;
import com.example.photoboothstudio.utils.PermissionHelper;
import com.google.android.material.button.MaterialButton;

/**
 * ResultActivity - hiển thị ảnh hoàn chỉnh, mã QR và cho phép lưu ảnh về máy.
 */
public class ResultActivity extends AppCompatActivity {

    public static final String EXTRA_IMAGE_PATH = "extra_image_path";

    private ImageView ivResult;
    private ImageView ivQrCode;
    private Bitmap resultBitmap;
    private boolean saving;

    private final ActivityResultLauncher<String[]> storagePermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                if (PermissionHelper.hasStoragePermission(this)) {
                    saveImageToGallery();
                } else {
                    CustomAlertDialog.showWarning(this, "Thiếu quyền bộ nhớ",
                            "Ứng dụng cần quyền lưu ảnh để ghi ảnh vào thư viện của máy.");
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_result);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ivResult = findViewById(R.id.ivResult);
        ivQrCode = findViewById(R.id.ivQrCode);

        String imagePath = getIntent().getStringExtra(EXTRA_IMAGE_PATH);
        resultBitmap = CameraUtils.decodeFile(imagePath);
        if (resultBitmap == null) {
            Toast.makeText(this, "Không tìm thấy ảnh kết quả.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        ivResult.setImageBitmap(resultBitmap);

        // QR demo: nội dung sẽ được thay bằng link tải ảnh thật ở giai đoạn sau.
        String qrContent = "https://photobooth.studio/p/" + System.currentTimeMillis();
        Bitmap qrBitmap = QRCodeGenerator.generate(qrContent, 640);
        if (qrBitmap != null) {
            ivQrCode.setImageBitmap(qrBitmap);
        }

        setupButtons();
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (!saving) {
                    navigateHome();
                }
            }
        });
    }

    private void setupButtons() {
        ImageButton btnBack = findViewById(R.id.btnBack);
        MaterialButton btnSave = findViewById(R.id.btnSave);
        MaterialButton btnNewShot = findViewById(R.id.btnNewShot);

        btnBack.setOnClickListener(v -> navigateHome());
        btnSave.setOnClickListener(v -> onSaveClicked());
        btnNewShot.setOnClickListener(v -> {
            Intent intent = new Intent(this, CameraActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void onSaveClicked() {
        if (saving) {
            return;
        }
        if (PermissionHelper.hasStoragePermission(this)) {
            saveImageToGallery();
        } else {
            storagePermissionLauncher.launch(PermissionHelper.storagePermissions());
        }
    }

    private void saveImageToGallery() {
        if (resultBitmap == null || saving) {
            return;
        }
        saving = true;
        final Bitmap bitmap = resultBitmap;
        final String displayName = "photobooth_" + System.currentTimeMillis();

        new Thread(() -> {
            boolean success = CameraUtils.saveBitmapToGallery(
                    getApplicationContext(), bitmap, displayName);
            runOnUiThread(() -> {
                saving = false;
                if (success) {
                    CustomAlertDialog.showSuccess(this, "Đã lưu ảnh",
                            "Ảnh đã được lưu vào thư viện: Pictures/PhotoboothStudio");
                } else {
                    CustomAlertDialog.showError(this, "Lưu ảnh thất bại",
                            "Không thể lưu ảnh vào bộ nhớ. Vui lòng thử lại.");
                }
            });
        }).start();
    }

    private void navigateHome() {
        String role = SharedPrefManager.getInstance(this).getUserRole();
        Intent intent;
        if ("GUEST".equalsIgnoreCase(role)) {
            intent = new Intent(this, WelcomeActivity.class);
        } else if ("ADMIN".equalsIgnoreCase(role)) {
            intent = new Intent(this, DashboardActivity.class);
        } else {
            intent = new Intent(this, ProfileActivity.class);
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
