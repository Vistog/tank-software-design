package ru.mipt.bit.platformer.control;

import org.junit.Before;
import org.junit.Test;
import ru.mipt.bit.platformer.input.InputController;
import ru.mipt.bit.platformer.model.*;
import java.util.*;
import static org.junit.Assert.*;

public class ControlTest {
    private GameWorld world;
    private Tank player;
    private Tank bot;

    @Before public void setup() {
        world = new GameWorld(new RectangleField(6, 6), 0.4f);
        player = new Tank(1);
        bot = new Tank(2);
        world.add(player, new Cell(1, 1), Direction.RIGHT);
        world.add(bot, new Cell(4, 4), Direction.RIGHT);
    }

    @Test public void commandOnlyRequestsMovementAndCanBeReused() {
        Runnable command = new MoveCommand(player, Direction.UP);
        command.run();
        assertEquals(new Cell(1, 1), world.getState(1).cell);
        world.update(0.4f);
        assertEquals(new Cell(1, 2), world.getState(1).cell);
        command.run();
        world.update(0.4f);
        assertEquals(new Cell(1, 3), world.getState(1).cell);
        assertEquals(new Cell(4, 4), world.getState(2).cell);
    }

    @Test public void keyboardStrategyUsesBindingsAndActions() {
        InputController input = new InputController(Runnable::run);
        input.bindHeld(10, new MoveCommand(player, Direction.UP));
        input.bindHeld(20, new MoveCommand(player, Direction.LEFT));
        int[] shots = {0};
        input.bindAction(30, () -> shots[0]++);
        ControlStrategy control = new KeyboardControl(input, key -> true, key -> key == 30);
        control.update();
        world.update(0.4f);
        assertEquals(new Cell(1, 2), world.getState(1).cell);
        assertEquals(1, shots[0]);
        assertEquals(new Cell(4, 4), world.getState(2).cell);
    }

    @Test public void releasedKeyboardDoesNotMovePlayer() {
        InputController input = new InputController(Runnable::run);
        input.bindHeld(10, new MoveCommand(player, Direction.UP));
        new KeyboardControl(input, key -> false, key -> false).update();
        world.update(0.4f);
        assertEquals(new Cell(1, 1), world.getState(1).cell);
    }

    @Test public void aiUsesRandomDirectionButDoesNotMovePlayer() {
        int seed = 42;
        Direction expected = Direction.values()[new Random(seed).nextInt(5)];
        ControlStrategy ai = new RandomControl(bot, world::getState, new Random(seed), Runnable::run);
        ai.update();
        assertEquals(new Cell(4, 4), world.getState(2).cell);
        world.update(0.4f);
        assertEquals(expected.nextCell(new Cell(4, 4)), world.getState(2).cell);
        assertEquals(new Cell(1, 1), world.getState(1).cell);
    }

    @Test public void aiDoesNotChooseNewDirectionDuringMovement() {
        bot.move(Direction.LEFT);
        world.update(0.1f);
        Random unexpected = new Random() {
            @Override public int nextInt(int bound) {
                throw new AssertionError("AI must wait until arrival");
            }
        };
        new RandomControl(bot, world::getState, unexpected, Runnable::run).update();
        world.update(0.1f);
        assertEquals(Direction.LEFT, world.getState(2).facing);
        assertEquals(new Cell(3, 4), world.getState(2).destination);
    }

    @Test public void blockedAiCanRetryOnNextUpdate() {
        Tank trapped = new Tank(3);
        world.add(trapped, new Cell(0, 0), Direction.RIGHT);
        Random directions = new Random() {
            private int calls;
            @Override public int nextInt(int bound) {
                return calls++ == 0 ? Direction.LEFT.ordinal() : Direction.UP.ordinal();
            }
        };
        ControlStrategy ai = new RandomControl(trapped, world::getState, directions, Runnable::run);
        ai.update();
        world.update(0.4f);
        assertEquals(new Cell(0, 0), world.getState(3).cell);
        ai.update();
        world.update(0.4f);
        assertEquals(new Cell(0, 1), world.getState(3).cell);
    }

    @Test public void allFourBoundariesBlockCommands() {
        for (Direction direction : Direction.values()) {
            GameWorld singleCell = new GameWorld(new RectangleField(1, 1), 0.4f);
            Tank tank = new Tank(1);
            singleCell.add(tank, new Cell(0, 0), Direction.RIGHT);
            new MoveCommand(tank, direction).run();
            singleCell.update(0.4f);
            assertEquals(new Cell(0, 0), singleCell.getState(1).cell);
            assertEquals(direction, singleCell.getState(1).facing);
        }
    }

    @Test public void otherTanksCannotEnterEitherCellOfMovingTank() {
        Tank mover = new Tank(3);
        Tank behind = new Tank(4);
        Tank ahead = new Tank(5);
        world.add(mover, new Cell(2, 2), Direction.RIGHT);
        world.add(behind, new Cell(1, 2), Direction.RIGHT);
        world.add(ahead, new Cell(4, 2), Direction.LEFT);
        new MoveCommand(mover, Direction.RIGHT).run();
        world.update(0.1f);
        new MoveCommand(behind, Direction.RIGHT).run();
        new MoveCommand(ahead, Direction.LEFT).run();
        world.update(0.1f);
        assertEquals(new Cell(1, 2), world.getState(4).destination);
        assertEquals(new Cell(4, 2), world.getState(5).destination);
        assertFalse(world.isFreeForMovement(new Cell(2, 2)));
        assertFalse(world.isFreeForMovement(new Cell(3, 2)));
        world.update(0.2f);
        assertTrue(world.isFreeForMovement(new Cell(2, 2)));
        assertFalse(world.isFreeForMovement(new Cell(3, 2)));
    }

    @Test public void manyAiUpdatesPreserveOccupancyAndPlayerControl() {
        world.add(new Tree(3), new Cell(3, 3), Direction.RIGHT);
        Tank otherBot = new Tank(4);
        world.add(otherBot, new Cell(2, 4), Direction.LEFT);
        List<ControlStrategy> controls = Arrays.asList(
                new RandomControl(bot, world::getState, new Random(12), Runnable::run),
                new RandomControl(otherBot, world::getState, new Random(31), Runnable::run));
        boolean moved = false;
        RectangleField field = new RectangleField(6, 6);
        for (int tick = 0; tick < 1000; tick++) {
            for (ControlStrategy control : controls) control.update();
            world.update(0.1f);
            Set<Cell> occupied = new HashSet<>();
            for (int id = 1; id <= 4; id++) {
                EntityState state = world.getState(id);
                assertTrue(field.contains(state.cell));
                assertTrue(field.contains(state.destination));
                assertTrue(occupied.add(state.cell));
                if (!state.cell.equals(state.destination)) assertTrue(occupied.add(state.destination));
            }
            moved |= !world.getState(2).cell.equals(new Cell(4, 4));
            assertEquals(new Cell(1, 1), world.getState(1).cell);
        }
        assertTrue(moved);
        for (Direction direction : Direction.values()) {
            Cell target = direction.nextCell(world.getState(1).cell);
            if (world.isFreeForMovement(target)) {
                new MoveCommand(player, direction).run();
                world.update(0.4f);
                assertEquals(target, world.getState(1).cell);
                return;
            }
        }
        fail("Expected a free neighbour for the player");
    }
}
