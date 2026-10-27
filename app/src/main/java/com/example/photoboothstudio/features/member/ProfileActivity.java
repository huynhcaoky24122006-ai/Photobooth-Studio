package com.example.photoboothstudio.features.member;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.example.photoboothstudio.data.local.SharedPrefManager;
import com.example.photoboothstudio.data.repository.AuthRepository;
import com.example.photoboothstudio.features.auth.WelcomeActivity;
import com.example.photoboothstudio.features.camera.CameraActivity;
import com.example.photoboothstudio.features.rewards.DailyCheckinActivity;
import com.example.photoboothstudio.features.shop.FrameShopActivity;
import com.example.photoboothstudio.models.User;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * ProfileActivity quản lý thông tin tài khoản cá nhân,
 * hiển thị số xu hiện có, điểm danh nhận xu, đăng xuất và thanh điều hướng chính.
 */
public class ProfileActivity extends AppCompatActivity {

    private ImageView imgAvatar;
    private TextView tvUserName, tvUserEmail, tvUserRole, tvCoinBalance;
    private LinearLayout btnDailyCheckin, btnLogout;
    private LinearLayout btnNavProfile, btnNavCamera, btnNavGallery, btnNavShop;

    private AuthRepository authRepository;
    private SharedPrefManager prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);

        // Xử lý Edge-to-Edge: Kéo dài chiều cao + nền trắng của BottomNavigation xuống tận đáy màn hình
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);

            LinearLayout bottomNav = findViewById(R.id.bottomNavigation);
            if (bottomNav != null) {
                bottomNav.setPadding(0, 0, 0, systemBars.bottom);
            }
            return insets;
        });

        authRepository = AuthRepository.getInstance(this);
        prefs = SharedPrefManager.getInstance(this);

        initViews();
        setupListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserData();
    }

    private void initViews() {
        imgAvatar = findViewById(R.id.imgAvatar);
        tvUserName = findViewById(R.id.tvUserName);
        tvUserEmail = findViewById(R.id.tvUserEmail);
        tvUserRole = findViewById(R.id.tvUserRole);
        tvCoinBalance = findViewById(R.id.tvCoinBalance);

        btnDailyCheckin = findViewById(R.id.btnDailyCheckin);
        btnLogout = findViewById(R.id.btnLogout);

        btnNavProfile = findViewById(R.id.btnNavProfile);
        btnNavCamera = findViewById(R.id.btnNavCamera);
        btnNavGallery = findViewById(R.id.btnNavGallery);
        btnNavShop = findViewById(R.id.btnNavShop);
    }

    private void setupListeners() {
        // Mở màn hình điểm danh hàng ngày
        btnDailyCheckin.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, DailyCheckinActivity.class);
            startActivity(intent);
        });

        // Đăng xuất tài khoản
        btnLogout.setOnClickListener(v -> showLogoutConfirmationDialog());

        // Thanh điều hướng (Bottom Navigation)
        btnNavProfile.setOnClickListener(v -> {
            // Đang ở màn Profile
            Toast.makeText(this, "Bạn đang ở trang Profile", Toast.LENGTH_SHORT).show();
        });

        btnNavCamera.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, CameraActivity.class);
            startActivity(intent);
        });

        btnNavGallery.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, GalleryActivity.class);
            startActivity(intent);
        });

        btnNavShop.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, FrameShopActivity.class);
            startActivity(intent);
        });
    }

    /**
     * Tải dữ liệu người dùng từ SharedPrefManager tạm thời và đồng bộ bản mới nhất từ AuthRepository/Firestore.
     */
    private void loadUserData() {
        // Tải trước thông tin trong Cache để UI không bị giật
        String cachedName = prefs.getUserName();
        String cachedEmail = prefs.getUserEmail();
        String cachedRole = prefs.getUserRole();

        tvUserName.setText(cachedName != null && !cachedName.trim().isEmpty() ? cachedName : "Tên Tài Khoản");
        tvUserEmail.setText(cachedEmail != null && !cachedEmail.trim().isEmpty() ? cachedEmail : "Email Tài Khoản");
        tvUserRole.setText(cachedRole != null ? cachedRole : "Member");

        // Đồng bộ dữ liệu mới nhất từ Firestore / Local Database
        authRepository.refreshCurrentUser(new AuthRepository.RepoCallback<User>() {
            @Override
            public void onSuccess(User user) {
                if (user != null) {
                    displayUser(user);
                }
            }

            @Override
            public void onError(@NonNull String message) {
                // Nếu mất mạng, khôi phục từ Room local
                authRepository.getCachedUser(new AuthRepository.RepoCallback<User>() {
                    @Override
                    public void onSuccess(User cachedUser) {
                        if (cachedUser != null) {
                            displayUser(cachedUser);
                        }
                    }

                    @Override
                    public void onError(@NonNull String localError) {
                        // Giữ nguyên dữ liệu cache cơ bản từ SharedPreferences
                    }
                });
            }
        });
    }

    private void displayUser(@NonNull User user) {
        if (user.getName() != null && !user.getName().trim().isEmpty()) {
            tvUserName.setText(user.getName());
        }
        if (user.getEmail() != null && !user.getEmail().trim().isEmpty()) {
            tvUserEmail.setText(user.getEmail());
        }

        if (user.isVIP()) {
            tvUserRole.setText("VIP Member");
        } else if (user.getRole() != null) {
            tvUserRole.setText(user.getRole());
        } else {
            tvUserRole.setText("Member");
        }

        tvCoinBalance.setText(NumberFormat.getNumberInstance(Locale.US).format(user.getCoins()));
    }

    private void showLogoutConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Đăng Xuất")
                .setMessage("Bạn có chắc chắn muốn đăng xuất khỏi tài khoản không?")
                .setPositiveButton("Đăng Xuất", (dialog, which) -> handleLogout())
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void handleLogout() {
        authRepository.logout();
        Toast.makeText(this, "Đã đăng xuất thành công!", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(ProfileActivity.this, WelcomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
