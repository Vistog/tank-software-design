package ru.mipt.bit.platformer.model;

/** Domain: common entity properties and movement contract. */
public abstract class Entity {
    public final int id;
    public final boolean blocksMovement;

    protected Entity(int id, boolean blocksMovement) {
        this.id = id;
        this.blocksMovement = blocksMovement;
    }

    public float speedMultiplier() {
        return 1f;
    }

    // A passive object has no movement requests.
    protected Direction takeMoveRequest() {
        return null;
    }
}
