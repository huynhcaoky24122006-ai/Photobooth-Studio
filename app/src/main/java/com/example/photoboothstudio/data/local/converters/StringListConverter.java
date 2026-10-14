package com.example.photoboothstudio.data.local.converters;

import androidx.room.TypeConverter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONTokener;

import java.util.ArrayList;
import java.util.List;

/**
 * Chuyển List<String> thành chuỗi JSON để Room lưu trong một cột TEXT.
 * Dùng cho unlockedFrameIds và unlockedStickerIds trong UserEntity.
 * Danh sách null được xem là danh sách rỗng, nhất quán với UserEntity.
 */
public class StringListConverter {

    /**
     * Giữ nguyên thứ tự, phần tử trùng và ký tự đặc biệt trong mỗi chuỗi.
     * Không chấp nhận phần tử null vì danh sách này chứa ID tài nguyên.
     */
    @TypeConverter
    public static String fromList(List<String> values) {
        JSONArray array = new JSONArray();
        if (values != null) {
            for (String value : values) {
                if (value == null) {
                    throw new IllegalArgumentException("Resource ID must not be null");
                }
                array.put(value);
            }
        }
        return array.toString();
    }

    /**
     * SQL NULL hoặc chuỗi trắng trả về danh sách rỗng có thể chỉnh sửa.
     * JSON sai cấu trúc hoặc có phần tử không phải chuỗi sẽ báo lỗi,
     * không âm thầm biến dữ liệu tài nguyên đã sở hữu thành danh sách rỗng.
     */
    @TypeConverter
    public static List<String> fromString(String value) {
        List<String> result = new ArrayList<>();
        if (value == null || value.trim().isEmpty()) {
            return result;
        }

        try {
            JSONTokener tokener = new JSONTokener(value);
            Object parsed = tokener.nextValue();
            if (!(parsed instanceof JSONArray) || tokener.nextClean() != 0) {
                throw new IllegalArgumentException("Expected a JSON array of resource IDs");
            }

            JSONArray array = (JSONArray) parsed;
            for (int i = 0; i < array.length(); i++) {
                Object item = array.get(i);
                // Không dùng getString vì nó có thể ép số/boolean thành chuỗi.
                if (!(item instanceof String)) {
                    throw new IllegalArgumentException(
                            "Resource ID at index " + i + " must be a string");
                }
                result.add((String) item);
            }
            return result;
        } catch (JSONException e) {
            throw new IllegalArgumentException("Invalid resource ID list JSON", e);
        }
    }
}
