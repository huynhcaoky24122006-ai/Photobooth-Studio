package com.example.photoboothstudio.utils;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.media.ExifInterface;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import com.example.photoboothstudio.models.LayoutConfig;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 * CameraUtils - các hàm hỗ trợ CameraX: tạo file, xoay ảnh theo EXIF,
 * ghép ảnh theo LayoutConfig và lưu ảnh ra bộ nhớ.
 */
public final class CameraUtils {

    private static final String CACHE_FOLDER = "photobooth";
    private static final int MAX_DECODE_SIZE = 2048;

    private CameraUtils() {
    }

    // ================= File tạm =================

    public static File createTempImageFile(Context context) throws IOException {
        File dir = new File(context.getCacheDir(), CACHE_FOLDER);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IOException("Không thể tạo thư mục cache: " + dir.getAbsolutePath());
        }
        return File.createTempFile("capture_", ".jpg", dir);
    }

    // ================= Đọc ảnh =================

    public static Bitmap decodeFile(String path) {
        return decodeFile(path, MAX_DECODE_SIZE);
    }

    public static Bitmap decodeFile(String path, int maxSize) {
        if (path == null) {
            return null;
        }
        File file = new File(path);
        if (!file.exists()) {
            return null;
        }

        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, bounds);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = calculateInSampleSize(bounds.outWidth, bounds.outHeight, maxSize);
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        return BitmapFactory.decodeFile(path, options);
    }

    /**
     * Đọc ảnh CameraX vừa chụp và xoay đúng chiều theo EXIF.
     */
    public static Bitmap decodeAndOrient(File file) {
        Bitmap bitmap = decodeFile(file.getAbsolutePath());
        if (bitmap == null) {
            return null;
        }

        int rotation = 0;
        try {
            ExifInterface exif = new ExifInterface(file.getAbsolutePath());
            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotation = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotation = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotation = 270;
                    break;
                default:
                    rotation = 0;
                    break;
            }
        } catch (IOException ignored) {
            // Không đọc được EXIF thì giữ nguyên ảnh gốc.
        }

        return rotation == 0 ? bitmap : rotateBitmap(bitmap, rotation);
    }

    private static int calculateInSampleSize(int width, int height, int maxSize) {
        int sampleSize = 1;
        int longestSide = Math.max(width, height);
        while (longestSide / sampleSize > maxSize) {
            sampleSize *= 2;
        }
        return sampleSize;
    }

    public static Bitmap rotateBitmap(Bitmap source, float degrees) {
        if (source == null || degrees == 0) {
            return source;
        }
        Matrix matrix = new Matrix();
        matrix.postRotate(degrees);
        Bitmap rotated = Bitmap.createBitmap(
                source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
        if (rotated != source) {
            source.recycle();
        }
        return rotated;
    }

    // ================= Ghép ảnh theo layout =================

    /**
     * Ghép các tấm ảnh đã chụp thành một bức ảnh hoàn chỉnh theo bố cục.
     */
    public static Bitmap composeCollage(List<Bitmap> shots, LayoutConfig layout) {
        int rows = Math.max(1, layout.getRows());
        int columns = Math.max(1, layout.getColumns());
        int frameCount = Math.max(1, layout.getFrameCount());

        int cellSize = 900;
        int gap = 24;
        int padding = 24;

        int outputWidth = columns * cellSize + (columns + 1) * gap;
        int outputHeight = rows * cellSize + (rows + 1) * gap;

        Bitmap output = Bitmap.createBitmap(outputWidth, outputHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        canvas.drawColor(Color.parseColor("#F3EAD5"));

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        Path clipPath = new Path();
        RectF cellRect = new RectF();

        for (int i = 0; i < frameCount; i++) {
            int row = i / columns;
            int column = i % columns;
            if (row >= rows) {
                break;
            }

            int left = gap + column * (cellSize + gap);
            int top = padding + row * (cellSize + gap);

            cellRect.set(left, top, left + cellSize, top + cellSize);
            clipPath.reset();
            clipPath.addRoundRect(cellRect, 28f, 28f, Path.Direction.CW);

            canvas.save();
            canvas.clipPath(clipPath);
            canvas.drawColor(Color.WHITE);
            if (i < shots.size()) {
                Bitmap cell = centerCrop(shots.get(i), cellSize, cellSize);
                canvas.drawBitmap(cell, left, top, paint);
                if (cell != shots.get(i)) {
                    cell.recycle();
                }
            }
            canvas.restore();
        }

        return output;
    }

    private static Bitmap centerCrop(Bitmap source, int targetWidth, int targetHeight) {
        if (source == null) {
            return null;
        }
        float scale = Math.max(
                targetWidth / (float) source.getWidth(),
                targetHeight / (float) source.getHeight());
        int scaledWidth = Math.max(1, Math.round(source.getWidth() * scale));
        int scaledHeight = Math.max(1, Math.round(source.getHeight() * scale));

        Bitmap scaled = Bitmap.createScaledBitmap(source, scaledWidth, scaledHeight, true);
        int x = Math.max(0, (scaledWidth - targetWidth) / 2);
        int y = Math.max(0, (scaledHeight - targetHeight) / 2);
        Bitmap cropped = Bitmap.createBitmap(scaled, x, y, targetWidth, targetHeight);
        if (cropped != scaled) {
            scaled.recycle();
        }
        return cropped;
    }

    // ================= Lưu ảnh =================

    /**
     * Lưu bitmap vào cache nội bộ và trả về đường dẫn tuyệt đối để truyền qua Intent.
     */
    public static String saveBitmapToCache(Context context, Bitmap bitmap, String name) {
        File dir = new File(context.getCacheDir(), CACHE_FOLDER);
        if (!dir.exists() && !dir.mkdirs()) {
            return null;
        }
        File file = new File(dir, name + ".jpg");
        try (OutputStream out = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 95, out);
            return file.getAbsolutePath();
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Lưu ảnh vào thư viện ảnh của máy (Pictures/PhotoboothStudio).
     */
    public static boolean saveBitmapToGallery(Context context, Bitmap bitmap, String displayName) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return saveToMediaStore(context, bitmap, displayName);
        }
        return saveToLegacyStorage(context, bitmap, displayName);
    }

    private static boolean saveToMediaStore(Context context, Bitmap bitmap, String displayName) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, displayName + ".jpg");
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        values.put(MediaStore.Images.Media.RELATIVE_PATH,
                Environment.DIRECTORY_PICTURES + "/PhotoboothStudio");

        Uri uri = context.getContentResolver()
                .insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        if (uri == null) {
            return false;
        }

        try (OutputStream out = context.getContentResolver().openOutputStream(uri)) {
            if (out == null) {
                return false;
            }
            return bitmap.compress(Bitmap.CompressFormat.JPEG, 95, out);
        } catch (IOException e) {
            return false;
        }
    }

    @SuppressWarnings("deprecation")
    private static boolean saveToLegacyStorage(Context context, Bitmap bitmap, String displayName) {
        File dir = new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                "PhotoboothStudio");
        if (!dir.exists() && !dir.mkdirs()) {
            return false;
        }

        File file = new File(dir, displayName + ".jpg");
        try (OutputStream out = new FileOutputStream(file)) {
            if (!bitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)) {
                return false;
            }
        } catch (IOException e) {
            return false;
        }

        MediaScannerConnection.scanFile(
                context,
                new String[]{file.getAbsolutePath()},
                new String[]{"image/jpeg"},
                null);
        return true;
    }
}
