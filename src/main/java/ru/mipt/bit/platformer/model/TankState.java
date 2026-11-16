package ru.mipt.bit.platformer.model;

/** Domain: State contract; new states can provide their own movement and firing rules. */
public interface TankState {
    float speedMultiplier();
    boolean canShoot();
}
