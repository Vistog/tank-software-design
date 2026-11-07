package ru.mipt.bit.platformer.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class BulletSystem {
    private final GameWorld world;
    private final float stepDuration;
    private final int damage;
    private float elapsed;

    public BulletSystem(GameWorld world, float stepDuration, int damage) {
        if (!Float.isFinite(stepDuration) || stepDuration <= 0 || damage <= 0) {
            throw new IllegalArgumentException("Positive bullet duration and damage required");
        }
        this.world = world;
        this.stepDuration = stepDuration;
        this.damage = damage;
    }

    public void update(float deltaTime) {
        if (!Float.isFinite(deltaTime) || deltaTime < 0) {
            throw new IllegalArgumentException("Invalid delta time");
        }
        // Apply turns and reserve tank destinations before handling shots.
        world.update(0);
        fireRequests();
        resolveCollisions();

        float remaining = deltaTime;
        while (remaining > 0) {
            if (bullets().isEmpty()) {
                elapsed = 0;
                world.update(remaining);
                return;
            }
            float step = Math.min(remaining, stepDuration - elapsed);
            world.update(step);
            resolveCollisions();
            elapsed += step;
            remaining -= step;
            // Split frame times can sum to just below the boundary due to float rounding.
            if (stepDuration - elapsed <= Math.ulp(stepDuration)) {
                elapsed = 0;
                moveBullets();
                resolveCollisions();
            }
        }
    }

    private void fireRequests() {
        for (Entity entity : world.entities()) {
            if (!(entity instanceof Tank)) continue;
            Tank tank = (Tank) entity;
            if (!tank.takeShotRequest()) continue;
            EntityState state = world.getState(tank.id);
            Cell cell = state.facing.nextCell(state.cell);
            if (world.contains(cell)) {
                world.add(new Bullet(world.newId(), tank.id, damage), cell, state.facing);
            }
        }
    }

    private List<Bullet> bullets() {
        List<Bullet> result = new ArrayList<>();
        for (Entity entity : world.entities()) {
            if (entity instanceof Bullet) result.add((Bullet) entity);
        }
        return result;
    }

    private void moveBullets() {
        List<Bullet> bullets = bullets();
        Map<Integer, Cell> targets = new LinkedHashMap<>();
        Set<Integer> removed = new HashSet<>();
        for (Bullet bullet : bullets) {
            EntityState state = world.getState(bullet.id);
            Cell target = state.facing.nextCell(state.cell);
            targets.put(bullet.id, target);
            if (!world.contains(target)) removed.add(bullet.id);
        }
        // Opposite bullets must collide, not swap places and pass through each other.
        for (int i = 0; i < bullets.size(); i++) {
            for (int j = i + 1; j < bullets.size(); j++) {
                Bullet first = bullets.get(i);
                Bullet second = bullets.get(j);
                if (targets.get(first.id).equals(world.getState(second.id).cell) &&
                        targets.get(second.id).equals(world.getState(first.id).cell)) {
                    removed.add(first.id);
                    removed.add(second.id);
                }
            }
        }
        for (Bullet bullet : bullets) {
            if (removed.contains(bullet.id)) world.remove(bullet.id);
            else world.moveBullet(bullet, targets.get(bullet.id));
        }
    }

    private void resolveCollisions() {
        Set<Integer> removed = new HashSet<>();
        for (Bullet bullet : bullets()) {
            Cell cell = world.getState(bullet.id).cell;
            for (Entity target : world.entitiesAt(cell)) {
                if (target.id == bullet.id || target.id == bullet.ownerId) continue;
                removed.add(bullet.id);
                if (target instanceof Bullet) removed.add(target.id);
                if (target instanceof Tank) {
                    Tank tank = (Tank) target;
                    tank.takeDamage(bullet.damage);
                    if (!tank.isAlive()) removed.add(tank.id);
                }
            }
        }
        for (int id : removed) world.remove(id);
    }
}
