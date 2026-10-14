package com.example.photoboothstudio.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.photoboothstudio.data.local.entities.UserEntity;

@Dao
public interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertUser(UserEntity user);

    @Update
    void updateUser(UserEntity user);

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    UserEntity getUserById(String userId);

    @Query("DELETE FROM users WHERE id = :userId")
    void deleteUserById(String userId);

    @Query("DELETE FROM users")
    void clearAllUsers();
}