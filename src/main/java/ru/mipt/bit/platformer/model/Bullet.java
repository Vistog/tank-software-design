package ru.mipt.bit.platformer.model;

public final class Bullet extends Entity {
    public final int ownerId;
    public final int damage;

    public Bullet(int id, int ownerId, int damage) {
        super(id, false);
        if (damage <= 0) throw new IllegalArgumentException("Damage must be positive");
        this.ownerId = ownerId;
        this.damage = damage;
    }
}
