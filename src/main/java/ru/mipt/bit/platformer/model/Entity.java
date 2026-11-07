package ru.mipt.bit.platformer.model;

public abstract class Entity {
    public final int id;
    public final boolean blocksMovement;

    protected Entity(int id, boolean blocksMovement) {
        this.id = id;
        this.blocksMovement = blocksMovement;
    }

    // A passive object has no movement requests.
    protected Direction takeMoveRequest() {
        return null;
    }
}
