package com.example.photoboothstudio.features.camera;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.photoboothstudio.R;
import com.example.photoboothstudio.data.local.SharedPrefManager;
import com.example.photoboothstudio.dialogs.CustomAlertDialog;
import com.example.photoboothstudio.features.auth.WelcomeActivity;
import com.example.photoboothstudio.features.editor.EditorActivity;
import com.example.photoboothstudio.models.LayoutConfig;
import com.example.photoboothstudio.utils.CameraUtils;
import com.example.photoboothstudio.utils.PermissionHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * CameraActivity - màn hình chụp ảnh dùng chung cho Guest và Member.
 * Cho phép chọn bố cục, đếm ngược 3 giây và chụp đủ số ảnh theo bố cục.
 */
public class CameraActivity extends AppCompatActivity {

    private static final long COUNTDOWN_MILLIS = 3000L;
    private static final long COUNTDOWN_INTERVAL = 1000L;

    private PreviewView previewView;
    private ImageButton btnBack;
    private FloatingActionButton btnCapture;
    private TextView tvCountdown;
    private RecyclerView rvLayouts;
    private LinearLayout layoutPanel;

    private ProcessCameraProvider cameraProvider;
    private ImageCapture imageCapture;
    private ExecutorService cameraExecutor;
    private CountdownTimer countdownTimer;

    private final List<Bitmap> capturedShots = new ArrayList<>();
    private LayoutConfig selectedLayout;
    private boolean capturing;

    private final ActivityResultLauncher<String[]> permissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                Boolean granted = result.get(Manifest.permission.CAMERA);
                if (Boolean.TRUE.equals(granted) || PermissionHelper.hasCameraPermission(this)) {
                    startCamera();
                } else {
                    CustomAlertDialog.showWarning(this, "Thiếu quyền Camera",
                            "Ứng dụng cần quyền Camera để chụp ảnh. Vui lòng cấp quyền trong phần Cài đặt.");
                    finish();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_camera);

        previewView = findViewById(R.id.previewView);
        btnBack = findViewById(R.id.btnBack);
        btnCapture = findViewById(R.id.btnCapture);
        tvCountdown = findViewById(R.id.tvCountdown);
        rvLayouts = findViewById(R.id.rvLayouts);
        layoutPanel = findViewById(R.id.layoutPanel);

        applyWindowInsets();

        cameraExecutor = Executors.newSingleThreadExecutor();

        setupLayoutList();
        setupListeners();

        if (PermissionHelper.hasCameraPermission(this)) {
            startCamera();
        } else {
            permissionLauncher.launch(PermissionHelper.CAMERA_PERMISSIONS);
        }
    }

    private void applyWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

            ViewGroup.MarginLayoutParams backParams =
                    (ViewGroup.MarginLayoutParams) btnBack.getLayoutParams();
            backParams.leftMargin = dp(12) + systemBars.left;
            backParams.topMargin = dp(12) + systemBars.top;
            btnBack.setLayoutParams(backParams);

            layoutPanel.setPadding(
                    layoutPanel.getPaddingLeft(),
                    layoutPanel.getPaddingTop(),
                    layoutPanel.getPaddingRight(),
                    dp(16) + systemBars.bottom);
            return insets;
        });
    }

    private void setupLayoutList() {
        List<LayoutConfig> layouts = buildPresetLayouts();
        selectedLayout = layouts.get(0);

        LayoutAdapter adapter = new LayoutAdapter(layouts, layout ->
                selectedLayout = layout);
        rvLayouts.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvLayouts.setAdapter(adapter);
    }

    /**
     * Các bố cục cố định dùng trong giai đoạn hiện tại (chưa lấy từ server).
     */
    private List<LayoutConfig> buildPresetLayouts() {
        List<LayoutConfig> layouts = new ArrayList<>();
        layouts.add(new LayoutConfig("layout_1x1", "1 ô", 1, 1, 1,
                "1:1", R.drawable.ic_layout_1x1));
        layouts.add(new LayoutConfig("layout_2x2", "2x2", 4, 2, 2,
                "1:1", R.drawable.ic_layout_2x2));
        layouts.add(new LayoutConfig("layout_strip3", "Strip 3", 3, 3, 1,
                "1:3", R.drawable.ic_layout_strip3));
        return layouts;
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> navigateBack());
        btnCapture.setOnClickListener(v -> startCaptureSequence());

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (!capturing) {
                    navigateBack();
                }
            }
        });
    }

    // ================= CameraX =================

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> providerFuture =
                ProcessCameraProvider.getInstance(this);
        providerFuture.addListener(() -> {
            try {
                cameraProvider = providerFuture.get();
                bindCameraUseCases();
            } catch (Exception e) {
                Toast.makeText(this, "Không thể khởi động Camera: " + e.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void bindCameraUseCases() {
        if (cameraProvider == null) {
            return;
        }

        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        imageCapture = new ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .setTargetRotation(getWindowManager().getDefaultDisplay().getRotation())
                .build();

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        try {
            cameraProvider.unbindAll();
            cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);
        } catch (Exception e) {
            Toast.makeText(this, "Không thể mở Camera: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    // ================= Luồng chụp ảnh =================

    private void startCaptureSequence() {
        if (capturing || imageCapture == null || selectedLayout == null) {
            return;
        }
        capturing = true;
        setCaptureUiEnabled(false);
        capturedShots.clear();
        startCountdownForNextShot();
    }

    private void startCountdownForNextShot() {
        tvCountdown.setVisibility(View.VISIBLE);
        tvCountdown.setText("3");

        if (countdownTimer != null) {
            countdownTimer.cancel();
        }
        countdownTimer = new CountdownTimer(COUNTDOWN_MILLIS, COUNTDOWN_INTERVAL,
                new CountdownTimer.Callback() {
                    @Override
                    public void onTick(int secondsRemaining) {
                        tvCountdown.setText(String.valueOf(Math.max(1, secondsRemaining)));
                    }

                    @Override
                    public void onFinishCountdown() {
                        tvCountdown.setText("Chụp!");
                        captureOneShot();
                    }
                });
        countdownTimer.start();
    }

    private void captureOneShot() {
        final File outputFile;
        try {
            outputFile = CameraUtils.createTempImageFile(this);
        } catch (IOException e) {
            onCaptureFailed("Không thể tạo file ảnh tạm.");
            return;
        }

        ImageCapture.OutputFileOptions outputOptions =
                new ImageCapture.OutputFileOptions.Builder(outputFile).build();

        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(this),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        Bitmap shot = CameraUtils.decodeAndOrient(outputFile);
                        if (shot != null) {
                            capturedShots.add(shot);
                        }

                        if (capturedShots.size() < selectedLayout.getFrameCount()) {
                            tvCountdown.postDelayed(
                                    CameraActivity.this::startCountdownForNextShot, 600L);
                        } else {
                            finishCaptureSequence();
                        }
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        onCaptureFailed("Chụp ảnh thất bại: " + exception.getMessage());
                    }
                });
    }

    private void finishCaptureSequence() {
        Bitmap collage = CameraUtils.composeCollage(capturedShots, selectedLayout);
        String path = CameraUtils.saveBitmapToCache(
                this, collage, "capture_" + System.currentTimeMillis());

        for (Bitmap shot : capturedShots) {
            if (shot != null && !shot.isRecycled()) {
                shot.recycle();
            }
        }
        capturedShots.clear();
        if (collage != null && !collage.isRecycled()) {
            collage.recycle();
        }

        capturing = false;
        setCaptureUiEnabled(true);
        tvCountdown.setVisibility(View.GONE);

        if (path == null) {
            Toast.makeText(this, "Không thể lưu ảnh vừa chụp.", Toast.LENGTH_LONG).show();
            return;
        }

        Intent intent = new Intent(this, EditorActivity.class);
        intent.putExtra(EditorActivity.EXTRA_IMAGE_PATH, path);
        startActivity(intent);
    }

    private void onCaptureFailed(String message) {
        capturing = false;
        setCaptureUiEnabled(true);
        tvCountdown.setVisibility(View.GONE);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void setCaptureUiEnabled(boolean enabled) {
        btnCapture.setEnabled(enabled);
        btnBack.setEnabled(enabled);
        btnCapture.setAlpha(enabled ? 1f : 0.5f);
        rvLayouts.setAlpha(enabled ? 1f : 0.5f);
    }

    private void navigateBack() {
        if ("GUEST".equalsIgnoreCase(SharedPrefManager.getInstance(this).getUserRole())) {
            Intent intent = new Intent(this, WelcomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
        finish();
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countdownTimer != null) {
            countdownTimer.cancel();
        }
        if (cameraProvider != null) {
            cameraProvider.unbindAll();
        }
        if (cameraExecutor != null) {
            cameraExecutor.shutdown();
        }
    }
}
