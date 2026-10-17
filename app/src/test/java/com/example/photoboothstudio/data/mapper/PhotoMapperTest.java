package com.example.photoboothstudio.data.mapper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.example.photoboothstudio.data.local.entities.PhotoEntity;
import com.example.photoboothstudio.models.Photo;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class PhotoMapperTest {

    @Test
    public void toModel_validEntity_returnsValidModel() {
        PhotoEntity entity = new PhotoEntity("photo_1", "http://example.com/photo1.png", 1700000000000L, "user_123", true);
        Photo model = PhotoMapper.toModel(entity);

        assertNotNull(model);
        assertEquals("photo_1", model.getId());
        assertEquals("http://example.com/photo1.png", model.getPhotoUrl());
        assertEquals(1700000000000L, model.getCreatedAt());
        assertEquals("user_123", model.getOwnerId());
        assertTrue(model.isPrivate());
    }

    @Test
    public void toModel_nullEntity_returnsNull() {
        assertNull(PhotoMapper.toModel(null));
    }

    @Test
    public void toEntity_validModel_returnsValidEntity() {
        Photo model = new Photo("photo_2", "http://example.com/photo2.png", 1700000001000L, "GUEST", false);
        PhotoEntity entity = PhotoMapper.toEntity(model);

        assertNotNull(entity);
        assertEquals("photo_2", entity.getId());
        assertEquals("http://example.com/photo2.png", entity.getPhotoUrl());
        assertEquals(1700000001000L, entity.getCreatedAt());
        assertEquals("GUEST", entity.getOwnerId());
        assertEquals(false, entity.isPrivate());
    }

    @Test
    public void toEntity_nullModel_returnsNull() {
        assertNull(PhotoMapper.toEntity(null));
    }

    @Test
    public void toModelList_validList_returnsMappedList() {
        List<PhotoEntity> entities = Arrays.asList(
                new PhotoEntity("p1", "url1", 1000L, "u1", false),
                new PhotoEntity("p2", "url2", 2000L, "u2", true)
        );

        List<Photo> models = PhotoMapper.toModelList(entities);

        assertNotNull(models);
        assertEquals(2, models.size());
        assertEquals("p1", models.get(0).getId());
        assertEquals("p2", models.get(1).getId());
    }

    @Test
    public void toModelList_nullList_returnsEmptyList() {
        List<Photo> result = PhotoMapper.toModelList(null);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void toEntityList_validList_returnsMappedList() {
        List<Photo> models = Arrays.asList(
                new Photo("p1", "url1", 1000L, "u1", false),
                new Photo("p2", "url2", 2000L, "u2", true)
        );

        List<PhotoEntity> entities = PhotoMapper.toEntityList(models);

        assertNotNull(entities);
        assertEquals(2, entities.size());
        assertEquals("p1", entities.get(0).getId());
        assertEquals("p2", entities.get(1).getId());
    }

    @Test
    public void toEntityList_nullList_returnsEmptyList() {
        List<PhotoEntity> result = PhotoMapper.toEntityList(null);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
