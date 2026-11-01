package ru.mipt.bit.platformer.graphics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Disposable;
import ru.mipt.bit.platformer.model.*;
import ru.mipt.bit.platformer.util.TileMovement;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.IntFunction;

import static com.badlogic.gdx.graphics.GL20.GL_COLOR_BUFFER_BIT;
import static ru.mipt.bit.platformer.util.GdxGameUtils.*;

public final class RenderSystem implements Disposable, WorldListener {
    private final SpriteBatch batch = new SpriteBatch();
    private final OrthogonalTiledMapRenderer levelRenderer;
    private final TileMovement tileMovement;
    private final Map<Integer, Sprite> renderables = new LinkedHashMap<>();
    private final Map<String, Texture> textures = new LinkedHashMap<>();
    private Texture bulletTexture;

    @Override
    public void entityAdded(Entity entity) {
        if (entity instanceof Tank) {
            register(entity.id, "images/tank_blue.png", entity.id == 1 ? Color.WHITE : Color.CYAN);
        } else if (entity instanceof Tree) {
            register(entity.id, "images/greenTree.png");
        } else if (entity instanceof Bullet) {
            if (bulletTexture == null) {
                Pixmap pixels = new Pixmap(16, 16, Pixmap.Format.RGBA8888);
                pixels.setColor(Color.YELLOW);
                pixels.fillCircle(8, 8, 7);
                bulletTexture = new Texture(pixels);
                pixels.dispose();
            }
            renderables.put(entity.id, new Sprite(bulletTexture));
        }
    }

    @Override
    public void entityRemoved(Entity entity) {
        renderables.remove(entity.id);
    }

    // Textures are shared by sprites and released only when the renderer is disposed.
    public RenderSystem(TiledMap level, Interpolation interpolation) {
        TiledMapTileLayer ground = getSingleLayer(level);
        levelRenderer = new OrthogonalTiledMapRenderer(level, batch);
        tileMovement = new TileMovement(ground, interpolation);
        float width = ground.getWidth() * ground.getTileWidth();
        float height = ground.getHeight() * ground.getTileHeight();
        Matrix4 projection = new Matrix4().setToOrtho2D(0, 0, width, height);
        batch.setProjectionMatrix(projection);
        levelRenderer.setView(projection, 0, 0, width, height);
    }

    public void register(int entityId, String texturePath) {
        register(entityId, texturePath, Color.WHITE);
    }

    public void register(int entityId, String texturePath, Color color) {
        Texture texture = textures.computeIfAbsent(texturePath, Texture::new);
        Sprite graphics = new Sprite(texture);
        graphics.setColor(color);
        renderables.put(entityId, graphics);
    }

    public void render(IntFunction<EntityState> states) {
        Gdx.gl.glClearColor(0f, 0f, 0.2f, 1f);
        Gdx.gl.glClear(GL_COLOR_BUFFER_BIT);
        levelRenderer.render();
        batch.begin();
        for (Map.Entry<Integer, Sprite> entry : renderables.entrySet()) {
            EntityState state = states.apply(entry.getKey());
            Sprite graphics = entry.getValue();
            batch.setColor(graphics.getColor());
            Rectangle rectangle = createBoundingRectangle(graphics);
            tileMovement.moveRectangleBetweenTileCenters(rectangle,
                    new GridPoint2(state.cell.x, state.cell.y),
                    new GridPoint2(state.destination.x, state.destination.y), state.progress);
            drawTextureRegionUnscaled(batch, graphics, rectangle, state.facing.rotationDegrees);
        }
        batch.end();
        batch.setColor(Color.WHITE);
    }

    @Override
    public void dispose() {
        for (Texture texture : textures.values()) texture.dispose();
        textures.clear();
        if (bulletTexture != null) bulletTexture.dispose();
        renderables.clear();
        levelRenderer.dispose();
        batch.dispose();
    }
}
