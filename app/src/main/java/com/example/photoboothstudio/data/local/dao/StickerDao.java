package com.example.photoboothstudio.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.photoboothstudio.data.local.entities.StickerEntity;

import java.util.List;

@Dao
public interface StickerDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertStickers(List<StickerEntity> stickers);

    @Query("SELECT * FROM stickers")
    List<StickerEntity> getAllStickers();

    @Query("SELECT * FROM stickers WHERE id = :stickerId LIMIT 1")
    StickerEntity getStickerById(String stickerId);

    @Query("DELETE FROM stickers")
    void clearAllStickers();
}