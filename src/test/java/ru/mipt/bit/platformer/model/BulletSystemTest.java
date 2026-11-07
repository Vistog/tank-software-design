package ru.mipt.bit.platformer.model;

import org.junit.Before;
import org.junit.Test;
import java.util.*;
import static org.junit.Assert.*;

public class BulletSystemTest {
    private GameWorld world;
    private Tank player;
    private BulletSystem system;

    @Before public void setup() {
        world = new GameWorld(new RectangleField(8, 4), 0.4f);
        player = new Tank(1);
        world.add(player, new Cell(1, 1), Direction.RIGHT);
        system = new BulletSystem(world, 0.1f, 25);
    }

    private List<Bullet> bullets() {
        List<Bullet> result = new ArrayList<>();
        for (Entity entity : world.entities()) {
            if (entity instanceof Bullet) result.add((Bullet) entity);
        }
        return result;
    }

    @Test public void shotSpawnsAheadAndRequestIsConsumedOnce() {
        player.shoot();
        assertTrue(bullets().isEmpty());
        system.update(0);
        Bullet bullet = bullets().get(0);
        assertEquals(1, bullet.ownerId);
        assertEquals(25, bullet.damage);
        assertEquals(new Cell(2, 1), world.getState(bullet.id).cell);
        assertEquals(Direction.RIGHT, world.getState(bullet.id).facing);
        system.update(0);
        assertEquals(1, bullets().size());
        system.update(0.1f);
        assertEquals(new Cell(3, 1), world.getState(bullet.id).cell);
    }

    @Test public void simultaneousMoveAndShotUseNewFacing() {
        player.move(Direction.UP);
        player.shoot();
        system.update(0);
        assertEquals(new Cell(1, 2), world.getState(bullets().get(0).id).cell);
        assertEquals(100, player.health());
    }

    @Test public void adjacentTankIsHitImmediatelyAndDiesAtZero() {
        Tank enemy = new Tank(2, 50);
        world.add(enemy, new Cell(2, 1), Direction.LEFT);
        player.shoot();
        system.update(0);
        assertEquals(25, enemy.health());
        assertTrue(world.contains(2));
        assertTrue(bullets().isEmpty());
        player.shoot();
        system.update(0);
        assertEquals(0, enemy.health());
        assertFalse(world.contains(2));
        assertTrue(world.isFreeForMovement(new Cell(2, 1)));
    }

    @Test public void adjacentTreeStopsBulletButSurvives() {
        world.add(new Tree(2), new Cell(2, 1), Direction.RIGHT);
        player.shoot();
        system.update(0);
        assertTrue(world.contains(2));
        assertTrue(bullets().isEmpty());
    }

    @Test public void bulletDoesNotSkipObstacleOnLargeDelta() {
        world.add(new Tree(2), new Cell(4, 1), Direction.RIGHT);
        Tank enemy = new Tank(3);
        world.add(enemy, new Cell(5, 1), Direction.LEFT);
        player.shoot();
        system.update(10);
        assertTrue(bullets().isEmpty());
        assertEquals(100, enemy.health());
        assertTrue(world.contains(2));
    }

    @Test public void bulletDamageIsConfigurable() {
        Tank enemy = new Tank(2);
        world.add(enemy, new Cell(4, 1), Direction.LEFT);
        player.shoot();
        new BulletSystem(world, 0.1f, 60).update(0.3f);
        assertEquals(40, enemy.health());
        assertTrue(bullets().isEmpty());
    }

    @Test public void bulletsDisappearAtMapBoundary() {
        player.shoot();
        system.update(1);
        assertTrue(bullets().isEmpty());
        GameWorld tiny = new GameWorld(new RectangleField(1, 1), 0.4f);
        Tank tank = new Tank(1);
        tiny.add(tank, new Cell(0, 0), Direction.RIGHT);
        tank.shoot();
        new BulletSystem(tiny, 0.1f, 25).update(0);
        assertEquals(1, tiny.entities().size());
        assertEquals(100, tank.health());
    }

    @Test public void bulletsMeetingInSameCellBothDisappear() {
        world.add(new Bullet(2, 90, 25), new Cell(3, 2), Direction.RIGHT);
        world.add(new Bullet(3, 91, 25), new Cell(5, 2), Direction.LEFT);
        system.update(0.1f);
        assertTrue(bullets().isEmpty());
    }

    @Test public void oppositeBulletsCannotSwapCells() {
        world.add(new Bullet(2, 90, 25), new Cell(3, 2), Direction.RIGHT);
        world.add(new Bullet(3, 91, 25), new Cell(4, 2), Direction.LEFT);
        system.update(0.1f);
        assertTrue(bullets().isEmpty());
    }

    @Test public void bulletsCreatedInSameCellCollideImmediately() {
        Tank enemy = new Tank(2);
        world.add(enemy, new Cell(3, 1), Direction.LEFT);
        player.shoot();
        enemy.shoot();
        system.update(0);
        assertTrue(bullets().isEmpty());
        assertEquals(100, player.health());
        assertEquals(100, enemy.health());
    }

    @Test public void simultaneousAdjacentShotsCanKillBothTanks() {
        Tank enemy = new Tank(2, 25);
        player.takeDamage(75);
        world.add(enemy, new Cell(2, 1), Direction.LEFT);
        player.shoot();
        enemy.shoot();
        system.update(0);
        assertTrue(world.entities().isEmpty());
    }

    @Test public void movingTankCanBeHitInItsReservedCell() {
        Tank enemy = new Tank(2, 25);
        world.add(enemy, new Cell(3, 2), Direction.DOWN);
        enemy.move(Direction.DOWN);
        world.update(0);
        world.add(new Bullet(3, 1, 25), new Cell(3, 1), Direction.RIGHT);
        system.update(0);
        assertFalse(world.contains(2));
        assertTrue(world.isFreeForMovement(new Cell(3, 1)));
        assertTrue(world.isFreeForMovement(new Cell(3, 2)));
    }

    @Test public void movingShooterDoesNotHitItself() {
        player.move(Direction.RIGHT);
        player.shoot();
        system.update(0.4f);
        assertEquals(100, player.health());
        assertEquals(new Cell(2, 1), world.getState(1).cell);
    }

    @Test public void deadTankIgnoresCommands() {
        player.takeDamage(1000);
        player.move(Direction.UP);
        player.shoot();
        assertFalse(player.isAlive());
        assertNull(player.takeMoveRequest());
        assertFalse(player.takeShotRequest());
    }

    @Test public void invalidParametersAndTimeAreRejectedWithoutConsumingShot() {
        assertThrows(IllegalArgumentException.class, () -> new Tank(2, 0));
        assertThrows(IllegalArgumentException.class, () -> player.takeDamage(-1));
        assertThrows(IllegalArgumentException.class, () -> new Bullet(2, 1, 0));
        assertThrows(IllegalArgumentException.class, () -> new BulletSystem(world, 0, 25));
        assertThrows(IllegalArgumentException.class, () -> new BulletSystem(world, 0.1f, 0));
        player.shoot();
        for (float dt : new float[]{-1, Float.NaN, Float.POSITIVE_INFINITY}) {
            assertThrows(IllegalArgumentException.class, () -> system.update(dt));
        }
        system.update(0);
        assertEquals(1, bullets().size());
    }

    @Test public void splitTimeMatchesSingleUpdateForStationaryTargets() {
        player.shoot();
        system.update(0.04f);
        system.update(0.06f);
        assertEquals(new Cell(3, 1), world.getState(bullets().get(0).id).cell);
    }
}
