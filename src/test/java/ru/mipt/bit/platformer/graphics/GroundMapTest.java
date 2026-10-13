package ru.mipt.bit.platformer.graphics;

import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import org.junit.Test;
import static org.junit.Assert.*;

public class GroundMapTest {
    @Test public void groundMatchesLevelDimensionsAndRepeatsPattern() {
        TiledMap map = new TiledMap();
        TiledMapTileLayer template = new TiledMapTileLayer(2, 1, 128, 128);
        TiledMapTileLayer.Cell first = new TiledMapTileLayer.Cell();
        TiledMapTileLayer.Cell second = new TiledMapTileLayer.Cell();
        template.setCell(0, 0, first);
        template.setCell(1, 0, second);
        map.getLayers().add(template);

        GroundMap.resize(map, 5, 3);
        TiledMapTileLayer ground = (TiledMapTileLayer) map.getLayers().get(0);
        assertEquals(1, map.getLayers().getCount());
        assertEquals(5, ground.getWidth());
        assertEquals(3, ground.getHeight());
        assertEquals(128f, ground.getTileWidth(), 0f);
        assertSame(first, ground.getCell(4, 2));
        assertSame(second, ground.getCell(3, 2));

        GroundMap.resize(map, 1, 1);
        ground = (TiledMapTileLayer) map.getLayers().get(0);
        assertEquals(1, ground.getWidth());
        assertEquals(1, ground.getHeight());
        map.dispose();
    }
}
