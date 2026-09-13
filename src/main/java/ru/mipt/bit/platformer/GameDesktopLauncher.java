package ru.mipt.bit.platformer;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapRenderer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Rectangle;
import ru.mipt.bit.platformer.util.TileMovement;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import static com.badlogic.gdx.Input.Keys.*;
import static com.badlogic.gdx.graphics.GL20.GL_COLOR_BUFFER_BIT;
import static ru.mipt.bit.platformer.util.GdxGameUtils.*;

public class GameDesktopLauncher extends ApplicationAdapter {
    private static final float TANK_MOVEMENT_DURATION_SECONDS = 0.4f;
    private static final int PLAYER_TANK_ID = 1;
    private static final int TREE_ID = 2;
    private static final Map<Integer, Direction> KEY_BINDINGS = createKeyBindings();

    private static Map<Integer, Direction> createKeyBindings() {
        Map<Integer, Direction> bindings = new LinkedHashMap<>();
        bindings.put(UP, Direction.UP);
        bindings.put(W, Direction.UP);
        bindings.put(LEFT, Direction.LEFT);
        bindings.put(A, Direction.LEFT);
        bindings.put(DOWN, Direction.DOWN);
        bindings.put(S, Direction.DOWN);
        bindings.put(RIGHT, Direction.RIGHT);
        bindings.put(D, Direction.RIGHT);
        return java.util.Collections.unmodifiableMap(bindings);
    }

    private Batch batch;
    private TiledMap level;
    private MapRenderer levelRenderer;
    private Texture blueTankTexture;
    private Texture greenTreeTexture;
    private GameWorld world;
    private final MovementManager movementManager = new MovementManager();
    private RenderSystem renderSystem;

    @Override
    public void create() {
        batch = new SpriteBatch();
        level = new TmxMapLoader().load("level.tmx");
        levelRenderer = createSingleLayerMapRenderer(level, batch);
        TiledMapTileLayer groundLayer = getSingleLayer(level);

        world = new GameWorld(new RectangleField(groundLayer.getWidth(), groundLayer.getHeight()));
        renderSystem = new RenderSystem(groundLayer);
        world.add(new Tank(PLAYER_TANK_ID), new Cell(1, 1), Direction.RIGHT);
        world.add(new Tree(TREE_ID), new Cell(1, 3), Direction.RIGHT);

        blueTankTexture = new Texture("images/tank_blue.png");
        greenTreeTexture = new Texture("images/greenTree.png");
        renderSystem.register(PLAYER_TANK_ID, new TextureRegion(blueTankTexture));
        renderSystem.register(TREE_ID, new TextureRegion(greenTreeTexture));
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0f, 0f, 0.2f, 1f);
        Gdx.gl.glClear(GL_COLOR_BUFFER_BIT);
        Direction requestedDirection = pressedDirection();
        if (requestedDirection != null) {
            movementManager.tryStartMove(world, PLAYER_TANK_ID, requestedDirection,
                    TANK_MOVEMENT_DURATION_SECONDS);
        }
        movementManager.update(world, Gdx.graphics.getDeltaTime());

        levelRenderer.render();
        batch.begin();
        renderSystem.render(batch, world);
        batch.end();
    }

    private Direction pressedDirection() {
        // First pressed binding wins: up, left, down, right.
        for (Map.Entry<Integer, Direction> binding : KEY_BINDINGS.entrySet()) {
            if (Gdx.input.isKeyPressed(binding.getKey())) {
                return binding.getValue();
            }
        }
        return null;
    }

    @Override
    public void dispose() {
        greenTreeTexture.dispose();
        blueTankTexture.dispose();
        level.dispose();
        batch.dispose();
    }

    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setWindowedMode(1280, 1024);
        new Lwjgl3Application(new GameDesktopLauncher(), config);
    }

    public enum Direction {
        UP(0, 1, 90f), LEFT(-1, 0, 180f), DOWN(0, -1, -90f), RIGHT(1, 0, 0f);

        private final int dx;
        private final int dy;
        private final float rotationDegrees;

        Direction(int dx, int dy, float rotationDegrees) {
            this.dx = dx;
            this.dy = dy;
            this.rotationDegrees = rotationDegrees;
        }

        public Cell nextCell(Cell cell) {
            return new Cell(cell.x + dx, cell.y + dy);
        }
    }

    public static final class Cell {
        public final int x;
        public final int y;

        public Cell(int x, int y) {
            this.x = x;
            this.y = y;
        }

        private GridPoint2 toGridPoint() {
            return new GridPoint2(x, y);
        }

        @Override
        public boolean equals(Object other) {
            if (!(other instanceof Cell)) {
                return false;
            }
            Cell cell = (Cell) other;
            return x == cell.x && y == cell.y;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y);
        }
    }

    public static class Entity {
        public final int id;
        public final boolean blocksMovement;

        protected Entity(int id, boolean blocksMovement) {
            this.id = id;
            this.blocksMovement = blocksMovement;
        }
    }

    public static final class Tank extends Entity {
        public Tank(int id) {
            super(id, true);
        }
    }

    public static final class Tree extends Entity {
        public Tree(int id) {
            super(id, true);
        }
    }

    public interface Field {
        boolean contains(Cell cell);
    }

    public static final class RectangleField implements Field {
        private final int width;
        private final int height;

        public RectangleField(int width, int height) {
            if (width <= 0 || height <= 0) {
                throw new IllegalArgumentException("Field dimensions must be positive");
            }
            this.width = width;
            this.height = height;
        }

        public boolean contains(Cell cell) {
            return cell.x >= 0 && cell.x < width && cell.y >= 0 && cell.y < height;
        }
    }

    // One world-owned entry per entity. Entities themselves have no coordinates.
    private static final class EntityState {
        private final Entity entity;
        private Cell cell;
        private Direction facing;
        private Movement movement;

        private EntityState(Entity entity, Cell cell, Direction facing) {
            this.entity = entity;
            this.cell = cell;
            this.facing = facing;
        }
    }

    private static final class Movement {
        private final Cell destination;
        private final float duration;
        private float elapsed;

        private Movement(Cell destination, float duration) {
            this.destination = destination;
            this.duration = duration;
        }

        private float getProgress() {
            return Math.min(1f, elapsed / duration);
        }
    }

    public static final class GameWorld {
        private final Field field;
        private final Map<Integer, EntityState> states = new LinkedHashMap<>();

        public GameWorld(Field field) {
            this.field = Objects.requireNonNull(field, "field");
        }

        public void add(Entity entity, Cell cell, Direction facing) {
            Objects.requireNonNull(entity, "entity");
            Objects.requireNonNull(cell, "cell");
            Objects.requireNonNull(facing, "facing");
            if (states.containsKey(entity.id) || !field.contains(cell)) {
                throw new IllegalArgumentException("Duplicate id or position outside the field");
            }
            if (entity.blocksMovement && !isFreeForMovement(cell)) {
                throw new IllegalArgumentException("Cell is occupied or reserved");
            }
            states.put(entity.id, new EntityState(entity, cell, facing));
        }

        private EntityState getState(int entityId) {
            EntityState state = states.get(entityId);
            if (state == null) {
                throw new IllegalArgumentException("Unknown entity: " + entityId);
            }
            return state;
        }

        public boolean isFreeForMovement(Cell cell) {
            if (!field.contains(cell)) {
                return false;
            }
            for (EntityState state : states.values()) {
                if (state.entity.blocksMovement && state.cell.equals(cell)) {
                    return false;
                }
                // A movement's destination is its reservation until arrival.
                if (state.movement != null && state.movement.destination.equals(cell)) {
                    return false;
                }
            }
            return true;
        }
    }

    public static final class MovementManager {
        public boolean tryStartMove(GameWorld world, int entityId, Direction direction, float duration) {
            if (!Float.isFinite(duration) || duration <= 0f) {
                throw new IllegalArgumentException("Movement duration must be finite and positive");
            }
            Objects.requireNonNull(direction, "direction");
            EntityState state = world.getState(entityId);
            if (state.movement != null) {
                return false;
            }
            state.facing = direction;
            Cell destination = direction.nextCell(state.cell);
            if (!world.isFreeForMovement(destination)) {
                return false;
            }
            state.movement = new Movement(destination, duration);
            return true;
        }

        public void update(GameWorld world, float deltaTime) {
            if (!Float.isFinite(deltaTime) || deltaTime < 0f) {
                throw new IllegalArgumentException("Delta time must be finite and non-negative");
            }
            for (EntityState state : world.states.values()) {
                Movement movement = state.movement;
                if (movement == null) {
                    continue;
                }
                movement.elapsed = Math.min(movement.duration, movement.elapsed + deltaTime);
                if (movement.getProgress() == 1f) {
                    state.cell = movement.destination;
                    state.movement = null;
                }
            }
        }
    }

    public static final class RenderSystem {
        private final TileMovement tileMovement;
        private final Map<Integer, TextureRegion> renderables = new LinkedHashMap<>();

        public RenderSystem(TiledMapTileLayer tileLayer) {
            tileMovement = new TileMovement(tileLayer, Interpolation.smooth);
        }

        public void register(int entityId, TextureRegion graphics) {
            renderables.put(entityId, graphics);
        }

        public void render(Batch batch, GameWorld world) {
            for (Map.Entry<Integer, TextureRegion> entry : renderables.entrySet()) {
                EntityState state = world.getState(entry.getKey());
                Movement movement = state.movement;
                Cell destination = movement == null ? state.cell : movement.destination;
                float progress = movement == null ? 1f : movement.getProgress();
                TextureRegion graphics = entry.getValue();
                Rectangle rectangle = createBoundingRectangle(graphics);
                tileMovement.moveRectangleBetweenTileCenters(rectangle, state.cell.toGridPoint(),
                        destination.toGridPoint(), progress);
                drawTextureRegionUnscaled(batch, graphics, rectangle, state.facing.rotationDegrees);
            }
        }
    }
}
