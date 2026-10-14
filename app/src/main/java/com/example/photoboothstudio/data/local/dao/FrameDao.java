package com.example.photoboothstudio.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.photoboothstudio.data.local.entities.FrameEntity;

import java.util.List;

@Dao
public interface FrameDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertFrames(List<FrameEntity> frames);

    @Query("SELECT * FROM frames")
    List<FrameEntity> getAllFrames();

    @Query("SELECT * FROM frames WHERE id = :frameId LIMIT 1")
    FrameEntity getFrameById(String frameId);

    @Query("DELETE FROM frames")
    void clearAllFrames();
}