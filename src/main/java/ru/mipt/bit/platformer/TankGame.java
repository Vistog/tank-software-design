package ru.mipt.bit.platformer;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import ru.mipt.bit.platformer.graphics.RenderSystem;
import ru.mipt.bit.platformer.input.InputController;
import ru.mipt.bit.platformer.model.*;

import static com.badlogic.gdx.Input.Keys.*;

public final class TankGame extends ApplicationAdapter {
    private GameWorld world;
    private InputController input;
    private RenderSystem renderer;

    @Override
    public void create() {
        renderer = new RenderSystem();
        world = new GameWorld(renderer.createField(), 0.4f);
        Tank tank = new Tank(1);
        Tree tree = new Tree(2);
        world.add(tank, new Cell(1, 1), Direction.RIGHT);
        world.add(tree, new Cell(1, 3), Direction.RIGHT);
        renderer.register(tank.id, "images/tank_blue.png");
        renderer.register(tree.id, "images/greenTree.png");

        input = new InputController(tank);
        input.bindMovement(UP, Direction.UP);
        input.bindMovement(W, Direction.UP);
        input.bindMovement(LEFT, Direction.LEFT);
        input.bindMovement(A, Direction.LEFT);
        input.bindMovement(DOWN, Direction.DOWN);
        input.bindMovement(S, Direction.DOWN);
        input.bindMovement(RIGHT, Direction.RIGHT);
        input.bindMovement(D, Direction.RIGHT);
    }

    @Override
    public void render() {
        input.update(Gdx.input::isKeyPressed, Gdx.input::isKeyJustPressed);
        world.update(Gdx.graphics.getDeltaTime());
        renderer.render(world);
    }

    @Override
    public void dispose() {
        renderer.dispose();
    }
}
