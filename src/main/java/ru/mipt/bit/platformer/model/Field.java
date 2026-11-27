package ru.mipt.bit.platformer.model;

/** Domain: internal boundary contract, not an infrastructure port. */
public interface Field {
    boolean contains(Cell cell);
}
