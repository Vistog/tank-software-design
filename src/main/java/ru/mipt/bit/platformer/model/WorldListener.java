package ru.mipt.bit.platformer.model;

/** Domain: output port notifying observers about added and removed entities. */
public interface WorldListener {
    void entityAdded(Entity entity);
    void entityRemoved(Entity entity);
}
