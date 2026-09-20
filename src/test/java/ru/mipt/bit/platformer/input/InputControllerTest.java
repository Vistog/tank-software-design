package ru.mipt.bit.platformer.input;

import org.junit.Before;
import org.junit.Test;
import ru.mipt.bit.platformer.model.*;
import static org.junit.Assert.*;

public class InputControllerTest {
    private InputController input;
    private GameWorld world;

    @Before
    public void createInput() {
        Tank tank = new Tank(1);
        world = new GameWorld(new RectangleField(5, 5), 0.4f);
        world.add(tank, new Cell(1, 1), Direction.RIGHT);
        input = new InputController(tank);
        input.bindMovement(10, Direction.UP);
        input.bindMovement(20, Direction.LEFT);
    }

    @Test
    public void inputOnlyRequestsMovement() {
        input.update(key -> key == 10, key -> false);
        assertEquals(new Cell(1, 1), world.getState(1).cell);
        world.update(0.4f);
        assertEquals(new Cell(1, 2), world.getState(1).cell);
    }

    @Test
    public void firstHeldMovementBindingWins() {
        input.update(key -> true, key -> false);
        world.update(0.4f);
        assertEquals(new Cell(1, 2), world.getState(1).cell);
    }

    @Test
    public void noPressedKeysMeansNoMovement() {
        input.update(key -> false, key -> false);
        world.update(1f);
        assertEquals(new Cell(1, 1), world.getState(1).cell);
    }

    @Test
    public void heldMovementRepeatsButStopsWhenReleased() {
        for (int frame = 0; frame < 2; frame++) {
            input.update(key -> key == 10, key -> false);
            world.update(0.4f);
        }
        input.update(key -> false, key -> false);
        world.update(0.4f);
        assertEquals(new Cell(1, 3), world.getState(1).cell);
    }

    @Test
    public void additionalActionRunsOncePerPressAlongsideMovement() {
        int[] shots = {0};
        input.bindAction(30, () -> shots[0]++);
        input.update(key -> key == 10 || key == 30, key -> key == 30);
        world.update(0.4f);
        assertEquals(1, shots[0]);
        assertEquals(new Cell(1, 2), world.getState(1).cell);
        input.update(key -> key == 30, key -> false);
        assertEquals(1, shots[0]);
    }

    @Test
    public void bindingCanBeReassigned() {
        input.bindMovement(10, Direction.RIGHT);
        input.update(key -> key == 10, key -> false);
        world.update(0.4f);
        assertEquals(new Cell(2, 1), world.getState(1).cell);
    }
}
