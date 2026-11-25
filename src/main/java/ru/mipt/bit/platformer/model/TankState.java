package ru.mipt.bit.platformer.model;

/** Domain: State contract; new states can provide their own movement and firing rules. */
public interface TankState {
    float speedMultiplier();
    boolean canShoot();
    /** Minimum time between shots, in simulation seconds. */
    float shotInterval();

    // Custom states persist by default; a state may choose a different transition.
    default TankState afterDamage(int health, int maxHealth) {
        return this;
    }
}
