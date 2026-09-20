package ru.mipt.bit.platformer.model;

import org.junit.Test;
import static org.junit.Assert.*;

public class ModelTest {
    @Test
    public void cellsAreValues() {
        Cell cell = new Cell(2, 3);
        assertEquals(cell, new Cell(2, 3));
        assertEquals(cell.hashCode(), new Cell(2, 3).hashCode());
        assertNotEquals(cell, new Cell(3, 2));
        assertNotEquals(cell, null);
        assertNotEquals(cell, "2,3");
    }

    @Test
    public void directionsContainStepsAndRotation() {
        Cell origin = new Cell(2, 2);
        assertEquals(new Cell(2, 3), Direction.UP.nextCell(origin));
        assertEquals(new Cell(1, 2), Direction.LEFT.nextCell(origin));
        assertEquals(new Cell(2, 1), Direction.DOWN.nextCell(origin));
        assertEquals(new Cell(3, 2), Direction.RIGHT.nextCell(origin));
        assertEquals(90f, Direction.UP.rotationDegrees, 0f);
        assertEquals(180f, Direction.LEFT.rotationDegrees, 0f);
        assertEquals(-90f, Direction.DOWN.rotationDegrees, 0f);
        assertEquals(0f, Direction.RIGHT.rotationDegrees, 0f);
    }

    @Test
    public void fieldIncludesOnlyItsCells() {
        Field field = new RectangleField(3, 2);
        assertTrue(field.contains(new Cell(0, 0)));
        assertTrue(field.contains(new Cell(2, 1)));
        assertFalse(field.contains(new Cell(-1, 0)));
        assertFalse(field.contains(new Cell(0, -1)));
        assertFalse(field.contains(new Cell(3, 0)));
        assertFalse(field.contains(new Cell(0, 2)));
    }

    @Test
    public void fieldRejectsInvalidSize() {
        assertThrows(IllegalArgumentException.class, () -> new RectangleField(0, 2));
        assertThrows(IllegalArgumentException.class, () -> new RectangleField(2, -1));
    }

    @Test
    public void tankGivesItsLatestRequestOnlyOnce() {
        Tank tank = new Tank(7);
        assertEquals(7, tank.id);
        assertTrue(tank.blocksMovement);
        assertNull(tank.takeMoveRequest());
        tank.move(Direction.UP);
        tank.move(Direction.LEFT);
        assertEquals(Direction.LEFT, tank.takeMoveRequest());
        assertNull(tank.takeMoveRequest());
    }

    @Test
    public void nullDirectionDoesNotErasePendingRequest() {
        Tank tank = new Tank(1);
        tank.move(Direction.UP);
        assertThrows(NullPointerException.class, () -> tank.move(null));
        assertEquals(Direction.UP, tank.takeMoveRequest());
    }

    @Test
    public void treeBlocksMovementButDoesNotRequestIt() {
        Tree tree = new Tree(9);
        assertEquals(9, tree.id);
        assertTrue(tree.blocksMovement);
        assertNull(tree.takeMoveRequest());
    }
}
