package com.example.photoboothstudio.data.mapper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.example.photoboothstudio.data.local.entities.UserEntity;
import com.example.photoboothstudio.models.User;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UserMapperTest {

    @Test
    public void existingConstructors_defaultToUnlocked() {
        assertFalse(new User().isLocked());
        assertFalse(new User("u", "email", "Name").isLocked());
        assertFalse(new User("u", "email", "Name", "MEMBER", 0, false, null, null).isLocked());
        assertFalse(new UserEntity("u", "email", "Name").isLocked());
        assertFalse(new UserEntity("u", "email", "Name", "MEMBER", 0, false, null, null).isLocked());
    }

    @Test
    public void lockedUser_roundTripPreservesLockAndCanUnlock() {
        User model = new User("u", "email", "Name", "MEMBER", 0, false, true, null, null);
        UserEntity entity = UserMapper.toEntity(model);
        assertTrue(entity.isLocked());
        assertTrue(UserMapper.toModel(entity).isLocked());
        model.setLocked(false);
        assertFalse(UserMapper.toEntity(model).isLocked());
    }

    @Test
    public void lockedEntity_roundTripPreservesLockAndCanUnlock() {
        UserEntity entity = new UserEntity("u", "email", "Name", "MEMBER", 0, false, true, null, null);
        User model = UserMapper.toModel(entity);
        assertTrue(model.isLocked());
        assertTrue(UserMapper.toEntity(model).isLocked());
        entity.setLocked(false);
        assertFalse(UserMapper.toModel(entity).isLocked());
    }


    @Test
    public void toModel_validEntity_returnsValidModelWithCopiedLists() {
        List<String> frames = new ArrayList<>(Arrays.asList("f1", "f2"));
        List<String> stickers = new ArrayList<>(Arrays.asList("s1", "s2"));

        UserEntity entity = new UserEntity("usr_1", "user@example.com", "John Doe", "MEMBER", 150, true, frames, stickers);
        User model = UserMapper.toModel(entity);

        assertNotNull(model);
        assertEquals("usr_1", model.getId());
        assertEquals("user@example.com", model.getEmail());
        assertEquals("John Doe", model.getName());
        assertEquals("MEMBER", model.getRole());
        assertEquals(150, model.getCoins());
        assertTrue(model.isVIP());
        assertEquals(2, model.getUnlockedFrameIds().size());
        assertEquals(2, model.getUnlockedStickerIds().size());

        // Verify defensive list copy (not same reference)
        assertNotSame(entity.getUnlockedFrameIds(), model.getUnlockedFrameIds());
        assertNotSame(entity.getUnlockedStickerIds(), model.getUnlockedStickerIds());
    }

    @Test
    public void toModel_nullEntity_returnsNull() {
        assertNull(UserMapper.toModel(null));
    }

    @Test
    public void toEntity_validModel_returnsValidEntity() {
        User model = new User("usr_2", "admin@example.com", "Admin User", "ADMIN", 500, true, Arrays.asList("f1"), Arrays.asList("s1"));
        UserEntity entity = UserMapper.toEntity(model);

        assertNotNull(entity);
        assertEquals("usr_2", entity.getId());
        assertEquals("admin@example.com", entity.getEmail());
        assertEquals("Admin User", entity.getName());
        assertEquals("ADMIN", entity.getRole());
        assertEquals(500, entity.getCoins());
        assertTrue(entity.isVIP());
        assertEquals(1, entity.getUnlockedFrameIds().size());
        assertEquals(1, entity.getUnlockedStickerIds().size());
    }

    @Test
    public void toEntity_nullModel_returnsNull() {
        assertNull(UserMapper.toEntity(null));
    }

    @Test
    public void toModelList_validList_returnsMappedList() {
        List<UserEntity> entities = Arrays.asList(
                new UserEntity("u1", "email1", "Name1", "GUEST", 0, false, new ArrayList<>(), new ArrayList<>()),
                new UserEntity("u2", "email2", "Name2", "MEMBER", 100, true, new ArrayList<>(), new ArrayList<>())
        );

        List<User> models = UserMapper.toModelList(entities);

        assertNotNull(models);
        assertEquals(2, models.size());
        assertEquals("u1", models.get(0).getId());
        assertEquals("u2", models.get(1).getId());
    }

    @Test
    public void toModelList_nullList_returnsEmptyList() {
        List<User> result = UserMapper.toModelList(null);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void toEntityList_validList_returnsMappedList() {
        List<User> models = Arrays.asList(
                new User("u1", "email1", "Name1", "GUEST", 0, false, new ArrayList<>(), new ArrayList<>()),
                new User("u2", "email2", "Name2", "MEMBER", 100, true, new ArrayList<>(), new ArrayList<>())
        );

        List<UserEntity> entities = UserMapper.toEntityList(models);

        assertNotNull(entities);
        assertEquals(2, entities.size());
        assertEquals("u1", entities.get(0).getId());
        assertEquals("u2", entities.get(1).getId());
    }

    @Test
    public void toEntityList_nullList_returnsEmptyList() {
        List<UserEntity> result = UserMapper.toEntityList(null);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
