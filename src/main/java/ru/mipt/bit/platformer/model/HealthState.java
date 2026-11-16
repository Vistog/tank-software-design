package ru.mipt.bit.platformer.model;

/** Domain: the three health states of a tank. */
public enum HealthState implements TankState {
    LIGHT(1f, true),
    MEDIUM(0.5f, true),
    HEAVY(1f / 3, false);

    private final float speed;
    private final boolean shooting;

    HealthState(float speed, boolean shooting) {
        this.speed = speed;
        this.shooting = shooting;
    }

    public float speedMultiplier() { return speed; }
    public boolean canShoot() { return shooting; }

    static TankState forHealth(int health, int maxHealth) {
        if (100L * health > 70L * maxHealth) return LIGHT;
        if (100L * health >= 15L * maxHealth) return MEDIUM;
        return HEAVY;
    }
}
