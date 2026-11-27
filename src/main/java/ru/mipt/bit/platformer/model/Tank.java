package ru.mipt.bit.platformer.model;

import java.util.Objects;

/** Domain: tank intentions, health and current behavior state. */
public final class Tank extends Entity {
    private Direction requestedDirection;
    private boolean shotRequested;
    private int health;
    private final int maxHealth;
    private TankState state = HealthState.LIGHT;
    private float timeSinceShot = Float.POSITIVE_INFINITY;

    public void changeState(TankState state) {
        this.state = Objects.requireNonNull(state);
    }

    public void restoreHealthState() {
        changeState(HealthState.forHealth(health, maxHealth));
    }

    void advanceReload(float deltaTime) {
        timeSinceShot += deltaTime;
    }

    public Tank(int id) {
        this(id, 100);
    }

    public Tank(int id, int health) {
        super(id, true);
        if (health <= 0) throw new IllegalArgumentException("Health must be positive");
        this.health = health;
        this.maxHealth = health;
    }

    // This is an intention. Only the world can change the actual position.
    public void move(Direction direction) {
        Objects.requireNonNull(direction);
        if (isAlive()) requestedDirection = direction;
    }

    public void shoot() {
        if (isAlive() && state.canShoot()) shotRequested = true;
    }

    @Override
    public float speedMultiplier() {
        return state.speedMultiplier();
    }

    public int health() {
        return health;
    }

    public float healthFraction() {
        return (float) health / maxHealth;
    }

    public boolean isAlive() {
        return health > 0;
    }

    public void takeDamage(int damage) {
        if (damage < 0) throw new IllegalArgumentException("Negative damage");
        health = Math.max(0, health - damage);
        changeState(state.afterDamage(health, maxHealth));
        if (!isAlive()) {
            requestedDirection = null;
            shotRequested = false;
        }
    }

    boolean takeShotRequest() {
        boolean requested = shotRequested;
        shotRequested = false;
        if (!requested || !isAlive() || !state.canShoot()
                || timeSinceShot < state.shotInterval()) return false;
        timeSinceShot = 0;
        return true;
    }

    @Override
    protected Direction takeMoveRequest() {
        Direction direction = requestedDirection;
        requestedDirection = null;
        return direction;
    }
}
