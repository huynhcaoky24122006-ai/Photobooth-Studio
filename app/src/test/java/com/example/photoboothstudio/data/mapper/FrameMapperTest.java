package com.example.photoboothstudio.data.mapper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.example.photoboothstudio.data.local.entities.FrameEntity;
import com.example.photoboothstudio.models.Frame;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class FrameMapperTest {

    @Test
    public void toModel_validEntity_returnsValidModel() {
        FrameEntity entity = new FrameEntity("frame_1", "Korean Style", "http://example.com/frame1.png", true, 100, "VINTAGE");
        Frame model = FrameMapper.toModel(entity);

        assertNotNull(model);
        assertEquals("frame_1", model.getId());
        assertEquals("Korean Style", model.getName());
        assertEquals("http://example.com/frame1.png", model.getImageUrl());
        assertTrue(model.isVIP());
        assertEquals(100, model.getPriceInCoins());
        assertEquals("VINTAGE", model.getCategory());
    }

    @Test
    public void toModel_nullEntity_returnsNull() {
        assertNull(FrameMapper.toModel(null));
    }

    @Test
    public void toEntity_validModel_returnsValidEntity() {
        Frame model = new Frame("frame_2", "Retro 90s", "http://example.com/frame2.png", false, 0, "RETRO");
        FrameEntity entity = FrameMapper.toEntity(model);

        assertNotNull(entity);
        assertEquals("frame_2", entity.getId());
        assertEquals("Retro 90s", entity.getName());
        assertEquals("http://example.com/frame2.png", entity.getImageUrl());
        assertEquals(false, entity.isVIP());
        assertEquals(0, entity.getPriceInCoins());
        assertEquals("RETRO", entity.getCategory());
    }

    @Test
    public void toEntity_nullModel_returnsNull() {
        assertNull(FrameMapper.toEntity(null));
    }

    @Test
    public void toModelList_validList_returnsMappedList() {
        List<FrameEntity> entities = Arrays.asList(
                new FrameEntity("f1", "F1", "url1", false, 0, "cat1"),
                new FrameEntity("f2", "F2", "url2", true, 50, "cat2")
        );

        List<Frame> models = FrameMapper.toModelList(entities);

        assertNotNull(models);
        assertEquals(2, models.size());
        assertEquals("f1", models.get(0).getId());
        assertEquals("f2", models.get(1).getId());
    }

    @Test
    public void toModelList_nullList_returnsEmptyList() {
        List<Frame> result = FrameMapper.toModelList(null);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void toEntityList_validList_returnsMappedList() {
        List<Frame> models = Arrays.asList(
                new Frame("f1", "F1", "url1", false, 0, "cat1"),
                new Frame("f2", "F2", "url2", true, 50, "cat2")
        );

        List<FrameEntity> entities = FrameMapper.toEntityList(models);

        assertNotNull(entities);
        assertEquals(2, entities.size());
        assertEquals("f1", entities.get(0).getId());
        assertEquals("f2", entities.get(1).getId());
    }

    @Test
    public void toEntityList_nullList_returnsEmptyList() {
        List<FrameEntity> result = FrameMapper.toEntityList(null);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
