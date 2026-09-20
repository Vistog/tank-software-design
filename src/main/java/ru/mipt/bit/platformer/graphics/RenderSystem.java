package ru.mipt.bit.platformer.graphics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Disposable;
import ru.mipt.bit.platformer.model.EntityState;
import ru.mipt.bit.platformer.model.Field;
import ru.mipt.bit.platformer.model.GameWorld;
import ru.mipt.bit.platformer.model.RectangleField;
import ru.mipt.bit.platformer.util.TileMovement;

import java.util.LinkedHashMap;
import java.util.Map;

import static com.badlogic.gdx.graphics.GL20.GL_COLOR_BUFFER_BIT;
import static ru.mipt.bit.platformer.util.GdxGameUtils.*;

public final class RenderSystem implements Disposable {
    private final SpriteBatch batch = new SpriteBatch();
    private final TiledMap level = new TmxMapLoader().load("level.tmx");
    private final TiledMapTileLayer ground = getSingleLayer(level);
    private final OrthogonalTiledMapRenderer levelRenderer =
            new OrthogonalTiledMapRenderer(level, batch);
    private final TileMovement tileMovement = new TileMovement(ground, Interpolation.smooth);
    private final Map<Integer, TextureRegion> renderables = new LinkedHashMap<>();

    public RenderSystem() {
        levelRenderer.getViewBounds().set(0, 0,
                ground.getWidth() * ground.getTileWidth(),
                ground.getHeight() * ground.getTileHeight());
    }

    public Field createField() {
        return new RectangleField(ground.getWidth(), ground.getHeight());
    }

    public void register(int entityId, String texturePath) {
        TextureRegion graphics = new TextureRegion(new Texture(texturePath));
        TextureRegion previous = renderables.put(entityId, graphics);
        if (previous != null) previous.getTexture().dispose();
    }

    public void render(GameWorld world) {
        Gdx.gl.glClearColor(0f, 0f, 0.2f, 1f);
        Gdx.gl.glClear(GL_COLOR_BUFFER_BIT);
        levelRenderer.render();
        batch.begin();
        for (Map.Entry<Integer, TextureRegion> entry : renderables.entrySet()) {
            EntityState state = world.getState(entry.getKey());
            TextureRegion graphics = entry.getValue();
            Rectangle rectangle = createBoundingRectangle(graphics);
            tileMovement.moveRectangleBetweenTileCenters(rectangle,
                    new GridPoint2(state.cell.x, state.cell.y),
                    new GridPoint2(state.destination.x, state.destination.y), state.progress);
            drawTextureRegionUnscaled(batch, graphics, rectangle, state.facing.rotationDegrees);
        }
        batch.end();
    }

    @Override
    public void dispose() {
        for (TextureRegion graphics : renderables.values()) graphics.getTexture().dispose();
        renderables.clear();
        levelRenderer.dispose();
        level.dispose();
        batch.dispose();
    }
}
