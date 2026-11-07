package ru.mipt.bit.platformer.level;

import org.junit.Test;
import ru.mipt.bit.platformer.model.*;
import java.util.*;
import static org.junit.Assert.*;

public class RandomTankSpawnerTest {
    @Test public void positionsAvoidPlayerTreesAndReservations() {
        GameWorld world = new GameWorld(new RectangleField(5, 3), 0.4f);
        Tank player = new Tank(1);
        world.add(player, new Cell(0, 0), Direction.RIGHT);
        world.add(new Tree(2), new Cell(2, 0), Direction.RIGHT);
        player.move(Direction.RIGHT);
        world.update(0.1f);
        List<Cell> cells = new RandomTankSpawner(new Random(42)).positions(world, 5, 3, 8);
        assertEquals(8, cells.size());
        assertEquals(8, new HashSet<>(cells).size());
        for (Cell cell : cells) assertTrue(world.isFreeForMovement(cell));
        int id = 3;
        for (Cell cell : cells) world.add(new Tank(id++), cell, Direction.RIGHT);
        for (Cell cell : cells) assertFalse(world.isFreeForMovement(cell));
    }

    @Test public void countIsCappedToFreeCells() {
        GameWorld world = new GameWorld(new RectangleField(2, 1), 0.4f);
        world.add(new Tank(1), new Cell(0, 0), Direction.RIGHT);
        RandomTankSpawner spawner = new RandomTankSpawner(new Random(1));
        assertEquals(Arrays.asList(new Cell(1, 0)), spawner.positions(world, 2, 1, 10));
        world.add(new Tree(2), new Cell(1, 0), Direction.RIGHT);
        assertTrue(spawner.positions(world, 2, 1, 10).isEmpty());
    }

    @Test public void seedMakesPositionsReproducible() {
        GameWorld world = new GameWorld(new RectangleField(10, 8), 0.4f);
        assertEquals(new RandomTankSpawner(new Random(42)).positions(world, 10, 8, 5),
                new RandomTankSpawner(new Random(42)).positions(world, 10, 8, 5));
        assertNotEquals(new RandomTankSpawner(new Random(42)).positions(world, 10, 8, 5),
                new RandomTankSpawner(new Random(43)).positions(world, 10, 8, 5));
    }

    @Test public void zeroCountWorksAndInvalidArgumentsFail() {
        GameWorld world = new GameWorld(new RectangleField(2, 2), 0.4f);
        RandomTankSpawner spawner = new RandomTankSpawner(new Random(1));
        assertTrue(spawner.positions(world, 2, 2, 0).isEmpty());
        assertThrows(IllegalArgumentException.class, () -> spawner.positions(world, 2, 2, -1));
        assertThrows(IllegalArgumentException.class, () -> spawner.positions(world, 0, 2, 1));
        assertThrows(IllegalArgumentException.class, () -> spawner.positions(world, 2, -1, 1));
    }
}
