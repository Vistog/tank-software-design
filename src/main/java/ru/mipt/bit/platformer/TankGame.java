package ru.mipt.bit.platformer;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.math.Interpolation;
import ru.mipt.bit.platformer.graphics.RenderSystem;
import ru.mipt.bit.platformer.input.InputController;
import ru.mipt.bit.platformer.model.*;

import static com.badlogic.gdx.Input.Keys.*;
import static ru.mipt.bit.platformer.util.GdxGameUtils.getSingleLayer;

public final class TankGame extends ApplicationAdapter {
    private final String levelPath;
    private final float movementDuration;
    private TiledMap level;
    private GameWorld world;
    private InputController input;
    private RenderSystem renderer;

    public TankGame(String levelPath, float movementDuration) {
        this.levelPath = levelPath;
        this.movementDuration = movementDuration;
    }

    @Override
    public void create() {
        level = new TmxMapLoader().load(levelPath);
        TiledMapTileLayer ground = getSingleLayer(level);
        world = new GameWorld(new RectangleField(ground.getWidth(), ground.getHeight()),
                movementDuration);
        renderer = new RenderSystem(level, Interpolation.smooth);
        Tank tank = new Tank(1);
        Tree tree = new Tree(2);
        world.add(tank, new Cell(1, 1), Direction.RIGHT);
        world.add(tree, new Cell(1, 3), Direction.RIGHT);
        renderer.register(tank.id, "images/tank_blue.png");
        renderer.register(tree.id, "images/greenTree.png");

        input = new InputController();
        input.bindHeld(UP, () -> tank.move(Direction.UP));
        input.bindHeld(W, () -> tank.move(Direction.UP));
        input.bindHeld(LEFT, () -> tank.move(Direction.LEFT));
        input.bindHeld(A, () -> tank.move(Direction.LEFT));
        input.bindHeld(DOWN, () -> tank.move(Direction.DOWN));
        input.bindHeld(S, () -> tank.move(Direction.DOWN));
        input.bindHeld(RIGHT, () -> tank.move(Direction.RIGHT));
        input.bindHeld(D, () -> tank.move(Direction.RIGHT));
    }

    @Override
    public void render() {
        input.update(Gdx.input::isKeyPressed, Gdx.input::isKeyJustPressed);
        world.update(Gdx.graphics.getDeltaTime());
        renderer.render(world::getState);
    }

    @Override
    public void dispose() {
        renderer.dispose();
        level.dispose();
    }
}
