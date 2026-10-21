package com.example.photoboothstudio.data.local;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.example.photoboothstudio.data.local.converters.StringListConverter;
import com.example.photoboothstudio.data.local.dao.FrameDao;
import com.example.photoboothstudio.data.local.dao.PhotoDao;
import com.example.photoboothstudio.data.local.dao.StickerDao;
import com.example.photoboothstudio.data.local.dao.UserDao;
import com.example.photoboothstudio.data.local.entities.FrameEntity;
import com.example.photoboothstudio.data.local.entities.PhotoEntity;
import com.example.photoboothstudio.data.local.entities.StickerEntity;
import com.example.photoboothstudio.data.local.entities.UserEntity;

/**
 * AppDatabase khởi tạo Room Database cục bộ (Level 0/P0).
 * Quản lý các Entity: UserEntity, PhotoEntity, FrameEntity, StickerEntity.
 * Cung cấp các phương thức truy cập DAO.
 */
@Database(
        entities = {
                UserEntity.class,
                PhotoEntity.class,
                FrameEntity.class,
                StickerEntity.class
        },
        version = 2,
        exportSchema = false
)
@TypeConverters({StringListConverter.class})
public abstract class AppDatabase extends RoomDatabase {

    private static final String DATABASE_NAME = "photobooth_studio_db";
    private static volatile AppDatabase instance;

    public static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE users ADD COLUMN isLocked INTEGER NOT NULL DEFAULT 0");
        }
    };

    // Các phương thức trừu tượng để lấy DAO
    public abstract UserDao userDao();
    public abstract PhotoDao photoDao();
    public abstract FrameDao frameDao();
    public abstract StickerDao stickerDao();

    // Singleton Pattern khởi tạo Database
    public static AppDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    DATABASE_NAME
                            )
                            .addMigrations(MIGRATION_1_2)
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return instance;
    }
}