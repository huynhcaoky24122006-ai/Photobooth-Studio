package com.example.photoboothstudio.features.editor;

import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.widget.ImageView;

/**
 * FilterProcessor - tập hợp các bộ lọc màu cơ bản áp dụng cho ảnh trong Editor.
 */
public final class FilterProcessor {

    public enum FilterType {
        NONE("Gốc"),
        GRAYSCALE("Đen trắng"),
        SEPIA("Sepia"),
        WARM("Ấm"),
        COOL("Lạnh"),
        VINTAGE("Cổ điển");

        private final String displayName;

        FilterType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    private FilterProcessor() {
    }

    public static void applyTo(ImageView imageView, FilterType type) {
        if (imageView == null) {
            return;
        }
        if (type == null || type == FilterType.NONE) {
            imageView.clearColorFilter();
            return;
        }
        imageView.setColorFilter(new ColorMatrixColorFilter(buildMatrix(type)));
    }

    public static ColorMatrix buildMatrix(FilterType type) {
        ColorMatrix matrix = new ColorMatrix();
        switch (type) {
            case GRAYSCALE:
                matrix.setSaturation(0f);
                break;
            case SEPIA:
                matrix.setSaturation(0.35f);
                matrix.postConcat(new ColorMatrix(new float[]{
                        1.15f, 0.05f, 0f, 0f, 12f,
                        0.05f, 1.0f, 0f, 0f, 4f,
                        0f, 0.05f, 0.82f, 0f, -8f,
                        0f, 0f, 0f, 1f, 0f
                }));
                break;
            case WARM:
                matrix.postConcat(new ColorMatrix(new float[]{
                        1.12f, 0f, 0f, 0f, 12f,
                        0f, 1.02f, 0f, 0f, 2f,
                        0f, 0f, 0.88f, 0f, -10f,
                        0f, 0f, 0f, 1f, 0f
                }));
                break;
            case COOL:
                matrix.postConcat(new ColorMatrix(new float[]{
                        0.88f, 0f, 0f, 0f, -8f,
                        0f, 1.0f, 0f, 0f, 2f,
                        0f, 0f, 1.14f, 0f, 12f,
                        0f, 0f, 0f, 1f, 0f
                }));
                break;
            case VINTAGE:
                matrix.setSaturation(0.55f);
                matrix.postConcat(new ColorMatrix(new float[]{
                        0.95f, 0.08f, 0f, 0f, 8f,
                        0.04f, 0.92f, 0f, 0f, 4f,
                        0f, 0.06f, 0.85f, 0f, -6f,
                        0f, 0f, 0f, 1f, 0f
                }));
                break;
            case NONE:
            default:
                break;
        }
        return matrix;
    }
}
