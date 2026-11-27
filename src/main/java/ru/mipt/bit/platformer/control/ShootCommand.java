package ru.mipt.bit.platformer.control;

import ru.mipt.bit.platformer.model.Tank;

/** Application: shooting use case implementing the Runnable input port. */
public final class ShootCommand implements Runnable {
    private final Tank tank;

    public ShootCommand(Tank tank) {
        this.tank = tank;
    }

    @Override
    public void run() {
        tank.shoot();
    }
}
