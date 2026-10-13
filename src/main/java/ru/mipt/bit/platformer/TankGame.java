package ru.mipt.bit.platformer;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.math.Interpolation;
import ru.mipt.bit.platformer.graphics.RenderSystem;
import ru.mipt.bit.platformer.graphics.GroundMap;
import ru.mipt.bit.platformer.level.*;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Random;
import ru.mipt.bit.platformer.input.InputController;
import ru.mipt.bit.platformer.model.*;

import static com.badlogic.gdx.Input.Keys.*;

public final class TankGame extends ApplicationAdapter {
    private final String levelPath;
    private final LevelSource levelSource;
    private final float movementDuration;
    private TiledMap level;
    private GameWorld world;
    private InputController input;
    private RenderSystem renderer;

    public TankGame() {
        this(new RandomLevelSource(10, 8, 15, new Random()), "level.tmx", 0.4f);
    }

    public TankGame(LevelSource levelSource, String levelPath, float movementDuration) {
        this.levelSource = levelSource;
        this.levelPath = levelPath;
        this.movementDuration = movementDuration;
    }

    @Override
    public void create() {
        LevelData data;
        try {
            data = levelSource.load();
        } catch (IOException error) {
            throw new UncheckedIOException("Cannot read level", error);
        }
        level = new TmxMapLoader().load(levelPath);
        GroundMap.resize(level, data.width, data.height);
        world = new GameWorld(new RectangleField(data.width, data.height),
                movementDuration);
        renderer = new RenderSystem(level, Interpolation.smooth);
        Tank tank = new Tank(1);
        world.add(tank, data.player, Direction.RIGHT);
        renderer.register(tank.id, "images/tank_blue.png");
        int treeId = 2;
        for (Cell cell : data.trees) {
            Tree tree = new Tree(treeId++);
            world.add(tree, cell, Direction.RIGHT);
            renderer.register(tree.id, "images/greenTree.png");
        }

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
