package ru.mipt.bit.platformer.control;

import org.junit.Test;
import ru.mipt.bit.platformer.input.InputController;
import ru.mipt.bit.platformer.model.*;
import java.util.*;
import static org.junit.Assert.*;

public class ShootingCommandsTest {
    @Test public void keyboardQueuesShotWithoutExecutingIt() {
        GameWorld world = new GameWorld(new RectangleField(4, 4), 0.4f);
        Tank tank = new Tank(1);
        world.add(tank, new Cell(1, 1), Direction.RIGHT);
        CommandQueue queue = new CommandQueue();
        InputController input = new InputController(queue::add);
        input.bindAction(32, new ShootCommand(tank));
        input.update(key -> true, key -> key == 32);
        BulletSystem system = new BulletSystem(world, 0.1f, 25);
        system.update(0);
        assertEquals(1, world.entities().size());
        queue.execute();
        system.update(0);
        assertEquals(2, world.entities().size());
        input.update(key -> true, key -> false);
        queue.execute();
        system.update(0);
        assertEquals(2, world.entities().size());
    }

    @Test public void queueExecutesInOrderOnceAndDefersNewCommands() {
        CommandQueue queue = new CommandQueue();
        List<Integer> calls = new ArrayList<>();
        queue.add(() -> { calls.add(1); queue.add(() -> calls.add(3)); });
        queue.add(() -> calls.add(2));
        queue.execute();
        assertEquals(Arrays.asList(1, 2), calls);
        queue.execute();
        queue.execute();
        assertEquals(Arrays.asList(1, 2, 3), calls);
    }

    @Test public void aiCanQueueShotAndDeadAiDoesNotReadRemovedState() {
        GameWorld world = new GameWorld(new RectangleField(4, 4), 0.4f);
        Tank tank = new Tank(1);
        world.add(tank, new Cell(1, 1), Direction.RIGHT);
        CommandQueue queue = new CommandQueue();
        Random shoot = new Random() {
            @Override public int nextInt(int bound) { return bound - 1; }
        };
        RandomControl control = new RandomControl(tank, world::getState, shoot, queue::add);
        control.update();
        queue.execute();
        new BulletSystem(world, 0.1f, 25).update(0);
        assertEquals(2, world.entities().size());
        tank.takeDamage(100);
        world.remove(1);
        control.update();
        queue.execute();
        assertFalse(world.contains(1));
    }

    @Test public void queuedCommandsForKilledTankDoNothing() {
        Tank tank = new Tank(1);
        CommandQueue queue = new CommandQueue();
        queue.add(new MoveCommand(tank, Direction.UP));
        queue.add(new ShootCommand(tank));
        tank.takeDamage(100);
        queue.execute();
        assertFalse(tank.isAlive());
        assertEquals(0, tank.health());
    }

    @Test public void longBattleKeepsWorldAndObserversConsistent() {
        GameWorld world = new GameWorld(new RectangleField(8, 5), 0.4f);
        CommandQueue queue = new CommandQueue();
        BulletSystem system = new BulletSystem(world, 0.1f, 25);
        Set<Integer> visible = new HashSet<>();
        int[] shots = {0};
        world.addListener(new WorldListener() {
            public void entityAdded(Entity entity) {
                visible.add(entity.id);
                if (entity instanceof Bullet) shots[0]++;
            }
            public void entityRemoved(Entity entity) { visible.remove(entity.id); }
        });
        List<ControlStrategy> controls = new ArrayList<>();
        for (int id = 1; id <= 4; id++) {
            Tank tank = new Tank(id);
            world.add(tank, new Cell(id, 2), Direction.RIGHT);
            controls.add(new RandomControl(tank, world::getState, new Random(id), queue::add));
        }
        for (int tick = 0; tick < 1000; tick++) {
            for (ControlStrategy control : controls) control.update();
            queue.execute();
            system.update(0.05f);
            Set<Integer> ids = new HashSet<>();
            Set<Cell> occupied = new HashSet<>();
            for (Entity entity : world.entities()) {
                ids.add(entity.id);
                EntityState state = world.getState(entity.id);
                assertTrue(world.contains(state.cell));
                if (entity instanceof Tank) {
                    assertTrue(((Tank) entity).isAlive());
                    assertTrue(occupied.add(state.cell));
                    if (!state.cell.equals(state.destination)) {
                        assertTrue(occupied.add(state.destination));
                    }
                }
            }
            assertEquals(ids, visible);
        }
        assertTrue(shots[0] > 0);
    }
}
