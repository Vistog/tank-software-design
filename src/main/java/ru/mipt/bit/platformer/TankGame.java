package ru.mipt.bit.platformer;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.math.Interpolation;
import ru.mipt.bit.platformer.graphics.RenderSystem;
import ru.mipt.bit.platformer.graphics.GroundMap;
import ru.mipt.bit.platformer.level.*;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Random;
import java.util.ArrayList;
import java.util.List;
import ru.mipt.bit.platformer.control.*;
import ru.mipt.bit.platformer.input.InputController;
import ru.mipt.bit.platformer.model.*;

import static com.badlogic.gdx.Input.Keys.*;

public final class TankGame extends ApplicationAdapter {
    private final String levelPath;
    private final LevelSource levelSource;
    private final float movementDuration;
    private final int aiTankCount;
    private final Random random;
    private final List<ControlStrategy> controls = new ArrayList<>();
    private TiledMap level;
    private GameWorld world;
    private RenderSystem renderer;

    public TankGame() {
        this(new RandomLevelSource(10, 8, 15, new Random()), "level.tmx", 0.4f);
    }

    public TankGame(LevelSource levelSource, String levelPath, float movementDuration) {
        this(levelSource, levelPath, movementDuration, 5, new Random());
    }

    public TankGame(LevelSource levelSource, String levelPath, float movementDuration,
                    int aiTankCount, Random random) {
        if (aiTankCount < 0) throw new IllegalArgumentException("Negative AI tank count");
        this.levelSource = levelSource;
        this.levelPath = levelPath;
        this.movementDuration = movementDuration;
        this.aiTankCount = aiTankCount;
        this.random = random;
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
        int nextId = 2;
        for (Cell cell : data.trees) {
            Tree tree = new Tree(nextId++);
            world.add(tree, cell, Direction.RIGHT);
            renderer.register(tree.id, "images/greenTree.png");
        }

        InputController input = new InputController();
        input.bindHeld(UP, new MoveCommand(tank, Direction.UP));
        input.bindHeld(W, new MoveCommand(tank, Direction.UP));
        input.bindHeld(LEFT, new MoveCommand(tank, Direction.LEFT));
        input.bindHeld(A, new MoveCommand(tank, Direction.LEFT));
        input.bindHeld(DOWN, new MoveCommand(tank, Direction.DOWN));
        input.bindHeld(S, new MoveCommand(tank, Direction.DOWN));
        input.bindHeld(RIGHT, new MoveCommand(tank, Direction.RIGHT));
        input.bindHeld(D, new MoveCommand(tank, Direction.RIGHT));
        controls.add(new KeyboardControl(input, Gdx.input::isKeyPressed, Gdx.input::isKeyJustPressed));

        RandomTankSpawner spawner = new RandomTankSpawner(random);
        for (Cell cell : spawner.positions(world, data.width, data.height, aiTankCount)) {
            Tank aiTank = new Tank(nextId++);
            world.add(aiTank, cell, Direction.RIGHT);
            renderer.register(aiTank.id, "images/tank_blue.png", Color.CYAN);
            controls.add(new RandomControl(aiTank, world::getState, random));
        }
    }

    @Override
    public void render() {
        for (ControlStrategy control : controls) control.update();
        world.update(Gdx.graphics.getDeltaTime());
        renderer.render(world::getState);
    }

    @Override
    public void dispose() {
        renderer.dispose();
        level.dispose();
    }
}
