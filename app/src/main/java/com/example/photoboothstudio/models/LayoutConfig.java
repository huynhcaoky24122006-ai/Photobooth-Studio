package com.example.photoboothstudio.models;

import java.io.Serializable;

/**
 * LayoutConfig đại diện cho cấu trúc bố cục khung chụp ảnh (Level 0)
 * Ví dụ: 1 ô lẻ, lưới 2x2, dải dọc 3 ô (Photo Strip)...
 */
public class LayoutConfig implements Serializable {
    private String id;              // ID duy nhất của layout (vd: "layout_1x1", "layout_2x2")
    private String name;            // Tên hiển thị (vd: "Ảnh đơn", "Khung 4 ô")
    private int frameCount;         // Số lượng ảnh cần chụp cho layout này (vd: 1, 3, 4)
    private int rows;
    private int columns;
    private String aspectRatio;     // Tỉ lệ khung hình (vd: "1:1", "4:3", "16:9")
    private int previewDrawableRes; // Resource ID ảnh minh họa layout (vd: R.drawable.ic_layout_2x2)

    // Constructor rỗng bắt buộc cho Firebase Serialization / Deserialization
    public LayoutConfig() {
    }

    public LayoutConfig(String id, String name, int frameCount, int rows, int columns, String aspectRatio, int previewDrawableRes) {
        this.id = id;
        this.name = name;
        this.frameCount = frameCount;
        this.rows = rows;
        this.columns = columns;
        this.aspectRatio = aspectRatio;
        this.previewDrawableRes = previewDrawableRes;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getFrameCount() {
        return frameCount;
    }

    public void setFrameCount(int frameCount) {
        this.frameCount = frameCount;
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    public int getColumns() {
        return columns;
    }

    public void setColumns(int columns) {
        this.columns = columns;
    }

    public String getAspectRatio() {
        return aspectRatio;
    }

    public void setAspectRatio(String aspectRatio) {
        this.aspectRatio = aspectRatio;
    }

    public int getPreviewDrawableRes() {
        return previewDrawableRes;
    }

    public void setPreviewDrawableRes(int previewDrawableRes) {
        this.previewDrawableRes = previewDrawableRes;
    }
}