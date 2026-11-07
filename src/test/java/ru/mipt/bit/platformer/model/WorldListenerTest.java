package ru.mipt.bit.platformer.model;

import org.junit.Test;
import java.util.*;
import static org.junit.Assert.*;

public class WorldListenerTest {
    @Test public void initialObjectsAndDynamicObjectsUseSameEvents() {
        GameWorld world = new GameWorld(new RectangleField(4, 4), 0.4f);
        List<Integer> visible = new ArrayList<>();
        WorldListener listener = new WorldListener() {
            public void entityAdded(Entity entity) {
                assertTrue(world.contains(entity.id));
                visible.add(entity.id);
            }
            public void entityRemoved(Entity entity) {
                assertFalse(world.contains(entity.id));
                visible.remove(Integer.valueOf(entity.id));
            }
        };
        world.addListener(listener);
        world.addListener(listener);
        Tank tank = new Tank(1);
        world.add(tank, new Cell(1, 1), Direction.RIGHT);
        world.add(new Tree(2), new Cell(3, 3), Direction.UP);
        assertEquals(Arrays.asList(1, 2), visible);
        tank.shoot();
        new BulletSystem(world, 0.1f, 25).update(0);
        assertEquals(3, visible.size());
        assertEquals(world.entities().size(), visible.size());
        int bulletId = visible.get(2);
        world.remove(bulletId);
        world.remove(bulletId);
        assertEquals(Arrays.asList(1, 2), visible);
        world.removeListener(listener);
        world.remove(1);
        assertEquals(Arrays.asList(1, 2), visible);
    }

    @Test public void failedAdditionDoesNotPublishAnEvent() {
        GameWorld world = new GameWorld(new RectangleField(1, 1), 0.4f);
        int[] added = {0};
        world.addListener(new WorldListener() {
            public void entityAdded(Entity entity) { added[0]++; }
            public void entityRemoved(Entity entity) {}
        });
        world.add(new Tank(1), new Cell(0, 0), Direction.RIGHT);
        assertThrows(IllegalArgumentException.class,
                () -> world.add(new Tree(2), new Cell(0, 0), Direction.RIGHT));
        assertEquals(1, added[0]);
    }

    @Test public void snapshotsDoNotExposeWorldCollectionAndIdsAreNotReused() {
        GameWorld world = new GameWorld(new RectangleField(2, 2), 0.4f);
        world.add(new Tank(10), new Cell(0, 0), Direction.RIGHT);
        world.entities().clear();
        assertTrue(world.contains(10));
        assertEquals(11, world.newId());
        world.remove(10);
        assertEquals(12, world.newId());
    }
}
