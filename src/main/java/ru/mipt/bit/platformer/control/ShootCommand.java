package ru.mipt.bit.platformer.control;

import ru.mipt.bit.platformer.model.Tank;

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
