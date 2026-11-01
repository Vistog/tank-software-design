package ru.mipt.bit.platformer.model;

public interface WorldListener {
    void entityAdded(Entity entity);
    void entityRemoved(Entity entity);
}
