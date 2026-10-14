package com.example.photoboothstudio.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.photoboothstudio.data.local.entities.PhotoEntity;

import java.util.List;

@Dao
public interface PhotoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertPhoto(PhotoEntity photo);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertPhotos(List<PhotoEntity> photos);

    @Query("SELECT * FROM photos WHERE ownerId = :ownerId ORDER BY createdAt DESC")
    List<PhotoEntity> getPhotosByOwner(String ownerId);

    @Query("DELETE FROM photos WHERE id = :photoId")
    void deletePhotoById(String photoId);

    @Query("DELETE FROM photos")
    void clearAllPhotos();
}