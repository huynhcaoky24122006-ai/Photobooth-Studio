package com.example.photoboothstudio.features.editor;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.photoboothstudio.R;
import com.example.photoboothstudio.features.export.ResultActivity;
import com.example.photoboothstudio.models.Frame;
import com.example.photoboothstudio.utils.CameraUtils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.List;

/**
 * EditorActivity - chỉnh sửa ảnh sau khi chụp: chọn khung, dán sticker,
 * kéo/xoay/phóng to sticker và áp dụng bộ lọc màu.
 */
public class EditorActivity extends AppCompatActivity {

    public static final String EXTRA_IMAGE_PATH = "extra_image_path";

    private static final String[] STICKER_NAMES = {
            "sticker_star",
            "sticker_heart",
            "sticker_crown",
            "sticker_glasses",
            "sticker_cat_ears",
            "sticker_mustache"
    };

    private FrameLayout canvasArea;
    private FrameLayout canvasContainer;
    private FrameLayout stickerLayer;
    private ImageView ivPhoto;
    private ImageView ivFrame;

    private RecyclerView rvFrames;
    private HorizontalScrollView scrollStickers;
    private HorizontalScrollView scrollFilters;
    private LinearLayout stickerList;
    private LinearLayout filterList;

    private TextView tabFrame;
    private TextView tabSticker;
    private TextView tabFilter;

    private Bitmap sourceBitmap;
    private FilterProcessor.FilterType currentFilter = FilterProcessor.FilterType.NONE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_editor);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();

        String imagePath = getIntent().getStringExtra(EXTRA_IMAGE_PATH);
        sourceBitmap = CameraUtils.decodeFile(imagePath);
        if (sourceBitmap == null) {
            Toast.makeText(this, "Không tìm thấy ảnh để chỉnh sửa.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        ivPhoto.setImageBitmap(sourceBitmap);
        setupCanvasSize();
        setupToolbar();
        setupTabs();
        setupFrameList();
        setupStickerList();
        setupFilterList();
        selectTab(0);
    }

    private void initViews() {
        canvasArea = findViewById(R.id.canvasArea);
        canvasContainer = findViewById(R.id.canvasContainer);
        stickerLayer = findViewById(R.id.stickerLayer);
        ivPhoto = findViewById(R.id.ivPhoto);
        ivFrame = findViewById(R.id.ivFrame);

        rvFrames = findViewById(R.id.rvFrames);
        scrollStickers = findViewById(R.id.scrollStickers);
        scrollFilters = findViewById(R.id.scrollFilters);
        stickerList = findViewById(R.id.stickerList);
        filterList = findViewById(R.id.filterList);

        tabFrame = findViewById(R.id.tabFrame);
        tabSticker = findViewById(R.id.tabSticker);
        tabFilter = findViewById(R.id.tabFilter);
    }

    private void setupCanvasSize() {
        canvasArea.addOnLayoutChangeListener(
                (v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) ->
                        layoutCanvas());
        canvasArea.post(this::layoutCanvas);
    }

    private void layoutCanvas() {
        if (sourceBitmap == null) {
            return;
        }
        int maxWidth = canvasArea.getWidth()
                - canvasArea.getPaddingLeft() - canvasArea.getPaddingRight();
        int maxHeight = canvasArea.getHeight()
                - canvasArea.getPaddingTop() - canvasArea.getPaddingBottom();
        if (maxWidth <= 0 || maxHeight <= 0) {
            return;
        }

        float ratio = sourceBitmap.getWidth() / (float) sourceBitmap.getHeight();
        int width = maxWidth;
        int height = Math.round(width / ratio);
        if (height > maxHeight) {
            height = maxHeight;
            width = Math.round(height * ratio);
        }

        ViewGroup.LayoutParams params = canvasContainer.getLayoutParams();
        params.width = Math.max(1, width);
        params.height = Math.max(1, height);
        canvasContainer.setLayoutParams(params);
    }

    private void setupToolbar() {
        ImageButton btnBack = findViewById(R.id.btnBack);
        MaterialButton btnDone = findViewById(R.id.btnDone);
        btnBack.setOnClickListener(v -> finish());
        btnDone.setOnClickListener(v -> exportImage());
    }

    private void setupTabs() {
        tabFrame.setOnClickListener(v -> selectTab(0));
        tabSticker.setOnClickListener(v -> selectTab(1));
        tabFilter.setOnClickListener(v -> selectTab(2));
    }

    private void selectTab(int index) {
        rvFrames.setVisibility(index == 0 ? View.VISIBLE : View.GONE);
        scrollStickers.setVisibility(index == 1 ? View.VISIBLE : View.GONE);
        scrollFilters.setVisibility(index == 2 ? View.VISIBLE : View.GONE);

        updateTabStyle(tabFrame, index == 0);
        updateTabStyle(tabSticker, index == 1);
        updateTabStyle(tabFilter, index == 2);
    }

    private void updateTabStyle(TextView tab, boolean selected) {
        tab.setTextColor(ContextCompat.getColor(this,
                selected ? R.color.coral_pink : R.color.gray_dark));
        tab.setTypeface(null, selected ? Typeface.BOLD : Typeface.NORMAL);
    }

    // ================= Khung ảnh =================

    private void setupFrameList() {
        List<Frame> frames = new ArrayList<>();
        frames.add(new Frame("frame_none", "Bỏ khung", "", false, 0, ""));
        frames.add(new Frame("frame_classic", "Cổ điển", "frame_classic", false, 0, "Basic"));
        frames.add(new Frame("frame_floral", "Hoa", "frame_floral", false, 0, "Cute"));
        frames.add(new Frame("frame_polaroid", "Polaroid", "frame_polaroid", false, 0, "Basic"));

        FrameAdapter adapter = new FrameAdapter(frames, frame -> {
            if (TextUtils.isEmpty(frame.getImageUrl())) {
                ivFrame.setImageDrawable(null);
                ivFrame.setVisibility(View.GONE);
                return;
            }
            int resId = resolveDrawable(frame.getImageUrl());
            if (resId != 0) {
                ivFrame.setImageResource(resId);
                ivFrame.setVisibility(View.VISIBLE);
            }
        });

        rvFrames.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvFrames.setAdapter(adapter);
    }

    // ================= Sticker =================

    private void setupStickerList() {
        for (String stickerName : STICKER_NAMES) {
            int resId = resolveDrawable(stickerName);
            if (resId == 0) {
                continue;
            }

            ImageView thumbnail = new ImageView(this);
            int size = dp(62);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
            params.setMargins(dp(6), 0, dp(6), 0);
            thumbnail.setLayoutParams(params);
            thumbnail.setPadding(dp(8), dp(8), dp(8), dp(8));
            thumbnail.setBackgroundColor(Color.parseColor("#F2F2F2"));
            thumbnail.setImageResource(resId);
            thumbnail.setContentDescription(stickerName);
            thumbnail.setOnClickListener(v -> addSticker(resId));

            stickerList.addView(thumbnail);
        }
    }

    private void addSticker(int drawableRes) {
        ImageView sticker = new ImageView(this);
        int size = dp(110);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(size, size);
        params.gravity = Gravity.CENTER;
        sticker.setLayoutParams(params);
        sticker.setImageResource(drawableRes);
        sticker.setElevation(dp(4));
        sticker.setContentDescription("Sticker");
        sticker.setOnTouchListener(new StickerTouchListener(sticker));

        stickerLayer.addView(sticker);
    }

    // ================= Bộ lọc =================

    private void setupFilterList() {
        for (FilterProcessor.FilterType type : FilterProcessor.FilterType.values()) {
            Chip chip = new Chip(this);
            chip.setText(type.getDisplayName());
            chip.setCheckable(true);
            chip.setChecked(type == currentFilter);
            chip.setTag(type);
            chip.setOnClickListener(v -> {
                currentFilter = type;
                FilterProcessor.applyTo(ivPhoto, type);
                updateFilterChips();
            });

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins(dp(4), 0, dp(4), 0);
            chip.setLayoutParams(params);
            filterList.addView(chip);
        }
    }

    private void updateFilterChips() {
        for (int i = 0; i < filterList.getChildCount(); i++) {
            View child = filterList.getChildAt(i);
            if (child instanceof Chip) {
                Chip chip = (Chip) child;
                chip.setChecked(currentFilter.equals(chip.getTag()));
            }
        }
    }

    // ================= Xuất ảnh =================

    private void exportImage() {
        if (sourceBitmap == null) {
            return;
        }

        int viewWidth = canvasContainer.getWidth();
        int viewHeight = canvasContainer.getHeight();
        if (viewWidth <= 0 || viewHeight <= 0) {
            Toast.makeText(this, "Vùng ảnh chưa sẵn sàng, vui lòng thử lại.", Toast.LENGTH_SHORT).show();
            return;
        }

        Bitmap output = Bitmap.createBitmap(
                sourceBitmap.getWidth(), sourceBitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        canvas.drawColor(Color.WHITE);
        canvas.scale(
                output.getWidth() / (float) viewWidth,
                output.getHeight() / (float) viewHeight);
        canvasContainer.draw(canvas);

        String path = CameraUtils.saveBitmapToCache(
                this, output, "edited_" + System.currentTimeMillis());
        output.recycle();

        if (path == null) {
            Toast.makeText(this, "Không thể lưu ảnh đã chỉnh sửa.", Toast.LENGTH_LONG).show();
            return;
        }

        Intent intent = new Intent(this, ResultActivity.class);
        intent.putExtra(ResultActivity.EXTRA_IMAGE_PATH, path);
        startActivity(intent);
        finish();
    }

    // ================= Tiện ích =================

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private int resolveDrawable(String name) {
        if (TextUtils.isEmpty(name)) {
            return 0;
        }
        return getResources().getIdentifier(name, "drawable", getPackageName());
    }
}
