package com.example.photoboothstudio.data.mapper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.example.photoboothstudio.data.local.entities.StickerEntity;
import com.example.photoboothstudio.models.Sticker;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class StickerMapperTest {

    @Test
    public void toModel_validEntity_returnsValidModel() {
        StickerEntity entity = new StickerEntity("sticker_1", "Heart Eyes", "http://example.com/sticker1.png", true, 30);
        Sticker model = StickerMapper.toModel(entity);

        assertNotNull(model);
        assertEquals("sticker_1", model.getId());
        assertEquals("Heart Eyes", model.getName());
        assertEquals("http://example.com/sticker1.png", model.getImageUrl());
        assertTrue(model.isVIP());
        assertEquals(30, model.getPriceInCoins());
    }

    @Test
    public void toModel_nullEntity_returnsNull() {
        assertNull(StickerMapper.toModel(null));
    }

    @Test
    public void toEntity_validModel_returnsValidEntity() {
        Sticker model = new Sticker("sticker_2", "Cat Ears", "http://example.com/sticker2.png", false, 0);
        StickerEntity entity = StickerMapper.toEntity(model);

        assertNotNull(entity);
        assertEquals("sticker_2", entity.getId());
        assertEquals("Cat Ears", entity.getName());
        assertEquals("http://example.com/sticker2.png", entity.getImageUrl());
        assertEquals(false, entity.isVIP());
        assertEquals(0, entity.getPriceInCoins());
    }

    @Test
    public void toEntity_nullModel_returnsNull() {
        assertNull(StickerMapper.toEntity(null));
    }

    @Test
    public void toModelList_validList_returnsMappedList() {
        List<StickerEntity> entities = Arrays.asList(
                new StickerEntity("s1", "S1", "url1", false, 0),
                new StickerEntity("s2", "S2", "url2", true, 20)
        );

        List<Sticker> models = StickerMapper.toModelList(entities);

        assertNotNull(models);
        assertEquals(2, models.size());
        assertEquals("s1", models.get(0).getId());
        assertEquals("s2", models.get(1).getId());
    }

    @Test
    public void toModelList_nullList_returnsEmptyList() {
        List<Sticker> result = StickerMapper.toModelList(null);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void toEntityList_validList_returnsMappedList() {
        List<Sticker> models = Arrays.asList(
                new Sticker("s1", "S1", "url1", false, 0),
                new Sticker("s2", "S2", "url2", true, 20)
        );

        List<StickerEntity> entities = StickerMapper.toEntityList(models);

        assertNotNull(entities);
        assertEquals(2, entities.size());
        assertEquals("s1", entities.get(0).getId());
        assertEquals("s2", entities.get(1).getId());
    }

    @Test
    public void toEntityList_nullList_returnsEmptyList() {
        List<StickerEntity> result = StickerMapper.toEntityList(null);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
