package ru.mipt.bit.platformer.model;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class GameWorldTest {
    private GameWorld world;
    private Tank tank;

    @Before
    public void createWorld() {
        world = new GameWorld(new RectangleField(5, 5), 0.4f);
        tank = new Tank(1);
        world.add(tank, new Cell(1, 1), Direction.RIGHT);
    }

    @Test
    public void requestDoesNotMoveTankUntilWorldUpdates() {
        tank.move(Direction.UP);
        EntityState state = world.getState(1);
        assertEquals(new Cell(1, 1), state.cell);
        assertEquals(state.cell, state.destination);
        assertEquals(Direction.RIGHT, state.facing);
        assertEquals(1f, state.progress, 0f);
    }

    @Test
    public void movementReservesBothCellsUntilArrival() {
        tank.move(Direction.RIGHT);
        world.update(0.2f);
        EntityState halfway = world.getState(1);
        assertEquals(new Cell(1, 1), halfway.cell);
        assertEquals(new Cell(2, 1), halfway.destination);
        assertEquals(0.5f, halfway.progress, 0.0001f);
        assertFalse(world.isFreeForMovement(new Cell(1, 1)));
        assertFalse(world.isFreeForMovement(new Cell(2, 1)));

        world.update(0.2f);
        assertEquals(new Cell(2, 1), world.getState(1).cell);
        assertTrue(world.isFreeForMovement(new Cell(1, 1)));
        assertFalse(world.isFreeForMovement(new Cell(2, 1)));
        // A previously returned snapshot does not change.
        assertEquals(new Cell(1, 1), halfway.cell);
        assertEquals(0.5f, halfway.progress, 0.0001f);
    }

    @Test
    public void treeStopsTankButAllowsItToTurn() {
        world.add(new Tree(2), new Cell(1, 2), Direction.RIGHT);
        tank.move(Direction.UP);
        world.update(1f);
        assertEquals(new Cell(1, 1), world.getState(1).cell);
        assertEquals(Direction.UP, world.getState(1).facing);
        assertEquals(1f, world.getState(1).progress, 0f);
    }

    @Test
    public void tankCannotLeaveField() {
        tank.move(Direction.LEFT);
        world.update(1f);
        tank.move(Direction.LEFT);
        world.update(1f);
        assertEquals(new Cell(0, 1), world.getState(1).cell);
        assertFalse(world.isFreeForMovement(new Cell(-1, 1)));
    }

    @Test
    public void firstAddedTankWinsContestedDestination() {
        Tank other = new Tank(2);
        world.add(other, new Cell(3, 1), Direction.LEFT);
        other.move(Direction.LEFT);
        tank.move(Direction.RIGHT);
        world.update(0f);
        assertEquals(new Cell(2, 1), world.getState(1).destination);
        assertEquals(new Cell(3, 1), world.getState(2).destination);
        world.update(1f);
        assertEquals(new Cell(2, 1), world.getState(1).cell);
        assertEquals(new Cell(3, 1), world.getState(2).cell);
    }

    @Test
    public void tanksCannotSwapOccupiedCells() {
        Tank other = new Tank(2);
        world.add(other, new Cell(2, 1), Direction.LEFT);
        tank.move(Direction.RIGHT);
        other.move(Direction.LEFT);
        world.update(1f);
        assertEquals(new Cell(1, 1), world.getState(1).cell);
        assertEquals(new Cell(2, 1), world.getState(2).cell);
    }

    @Test
    public void independentTanksCanMoveTogether() {
        Tank other = new Tank(2);
        world.add(other, new Cell(3, 1), Direction.UP);
        tank.move(Direction.UP);
        other.move(Direction.UP);
        world.update(0.4f);
        assertEquals(new Cell(1, 2), world.getState(1).cell);
        assertEquals(new Cell(3, 2), world.getState(2).cell);
    }

    @Test
    public void requestDuringMovementIsNotQueuedAndDoesNotTurnTank() {
        tank.move(Direction.RIGHT);
        world.update(0.1f);
        tank.move(Direction.UP);
        world.update(0.1f);
        assertEquals(Direction.RIGHT, world.getState(1).facing);
        world.update(1f);
        world.update(1f);
        assertEquals(new Cell(2, 1), world.getState(1).cell);
    }

    @Test
    public void blockedRequestIsNotRetriedAutomatically() {
        Tank other = new Tank(2);
        world.add(other, new Cell(2, 1), Direction.UP);
        tank.move(Direction.RIGHT);
        other.move(Direction.UP);
        world.update(1f);
        world.update(1f);
        assertEquals(new Cell(1, 1), world.getState(1).cell);
        tank.move(Direction.RIGHT);
        world.update(1f);
        assertEquals(new Cell(2, 1), world.getState(1).cell);
    }

    @Test
    public void largeDeltaFinishesOnlyTheRequestedStep() {
        tank.move(Direction.UP);
        world.update(100f);
        world.update(100f);
        assertEquals(new Cell(1, 2), world.getState(1).cell);
        assertEquals(1f, world.getState(1).progress, 0f);
    }

    @Test
    public void invalidDeltaDoesNotConsumeRequest() {
        tank.move(Direction.UP);
        for (float value : new float[]{-1f, Float.NaN, Float.POSITIVE_INFINITY}) {
            assertThrows(IllegalArgumentException.class, () -> world.update(value));
        }
        world.update(0.4f);
        assertEquals(new Cell(1, 2), world.getState(1).cell);
    }

    @Test
    public void invalidDurationIsRejected() {
        for (float value : new float[]{0f, -1f, Float.NaN, Float.POSITIVE_INFINITY}) {
            assertThrows(IllegalArgumentException.class,
                    () -> new GameWorld(new RectangleField(5, 5), value));
        }
        assertThrows(NullPointerException.class, () -> new GameWorld(null, 0.4f));
    }

    @Test
    public void invalidPlacementDoesNotChangeWorld() {
        assertThrows(IllegalArgumentException.class,
                () -> world.add(new Tree(1), new Cell(4, 4), Direction.UP));
        assertThrows(IllegalArgumentException.class,
                () -> world.add(new Tree(2), new Cell(5, 0), Direction.UP));
        assertThrows(IllegalArgumentException.class,
                () -> world.add(new Tree(2), new Cell(1, 1), Direction.UP));
        assertThrows(IllegalArgumentException.class, () -> world.getState(99));
        assertEquals(new Cell(1, 1), world.getState(1).cell);
        assertTrue(world.isFreeForMovement(new Cell(4, 4)));
    }

    @Test
    public void cannotSpawnOnReservedCell() {
        tank.move(Direction.UP);
        world.update(0f);
        assertThrows(IllegalArgumentException.class,
                () -> world.add(new Tree(2), new Cell(1, 2), Direction.UP));
    }

    @Test
    public void nonBlockingObjectDoesNotOccupyCell() {
        Entity decoration = new Entity(2, false) {};
        world.add(decoration, new Cell(1, 2), Direction.RIGHT);
        tank.move(Direction.UP);
        world.update(0.4f);
        assertEquals(new Cell(1, 2), world.getState(1).cell);
    }

    @Test
    public void worldUsesEntityContractInsteadOfCheckingTankType() {
        Entity walker = new Entity(2, true) {
            @Override
            protected Direction takeMoveRequest() {
                return Direction.UP;
            }
        };
        world.add(walker, new Cell(3, 1), Direction.RIGHT);
        world.update(0.4f);
        assertEquals(new Cell(3, 2), world.getState(2).cell);
    }

    @Test
    public void worldUsesFieldContract() {
        GameWorld narrowWorld = new GameWorld(cell -> cell.x == 1 && cell.y == 1, 0.4f);
        narrowWorld.add(tank, new Cell(1, 1), Direction.RIGHT);
        tank.move(Direction.UP);
        narrowWorld.update(1f);
        assertEquals(new Cell(1, 1), narrowWorld.getState(1).cell);
    }
}
