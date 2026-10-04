package ru.mipt.bit.platformer.graphics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Disposable;
import ru.mipt.bit.platformer.model.EntityState;
import ru.mipt.bit.platformer.util.TileMovement;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.IntFunction;

import static com.badlogic.gdx.graphics.GL20.GL_COLOR_BUFFER_BIT;
import static ru.mipt.bit.platformer.util.GdxGameUtils.*;

public final class RenderSystem implements Disposable {
    private final SpriteBatch batch = new SpriteBatch();
    private final OrthogonalTiledMapRenderer levelRenderer;
    private final TileMovement tileMovement;
    private final Map<Integer, TextureRegion> renderables = new LinkedHashMap<>();

    // The caller owns the map; this renderer owns its batch and registered textures.
    public RenderSystem(TiledMap level, Interpolation interpolation) {
        TiledMapTileLayer ground = getSingleLayer(level);
        levelRenderer = new OrthogonalTiledMapRenderer(level, batch);
        tileMovement = new TileMovement(ground, interpolation);
        levelRenderer.getViewBounds().set(0, 0,
                ground.getWidth() * ground.getTileWidth(),
                ground.getHeight() * ground.getTileHeight());
    }

    public void register(int entityId, String texturePath) {
        TextureRegion graphics = new TextureRegion(new Texture(texturePath));
        TextureRegion previous = renderables.put(entityId, graphics);
        if (previous != null) previous.getTexture().dispose();
    }

    public void render(IntFunction<EntityState> states) {
        Gdx.gl.glClearColor(0f, 0f, 0.2f, 1f);
        Gdx.gl.glClear(GL_COLOR_BUFFER_BIT);
        levelRenderer.render();
        batch.begin();
        for (Map.Entry<Integer, TextureRegion> entry : renderables.entrySet()) {
            EntityState state = states.apply(entry.getKey());
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
        batch.dispose();
    }
}
