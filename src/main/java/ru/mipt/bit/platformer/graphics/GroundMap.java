package ru.mipt.bit.platformer.graphics;

import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import static ru.mipt.bit.platformer.util.GdxGameUtils.getSingleLayer;

public final class GroundMap {
    private GroundMap() {}

    // Repeat the existing grass pattern. Textures remain owned by the same map.
    public static void resize(TiledMap map, int width, int height) {
        TiledMapTileLayer template = getSingleLayer(map);
        TiledMapTileLayer ground = new TiledMapTileLayer(width, height,
                (int) template.getTileWidth(), (int) template.getTileHeight());
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                ground.setCell(x, y, template.getCell(
                        x % template.getWidth(), y % template.getHeight()));
            }
        }
        map.getLayers().remove(template);
        map.getLayers().add(ground);
    }
}
