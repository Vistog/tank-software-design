package ru.mipt.bit.platformer.control;

import ru.mipt.bit.platformer.model.Direction;
import ru.mipt.bit.platformer.model.Tank;

// Runnable is the command interface already used by InputController.
public final class MoveCommand implements Runnable {
    private final Tank tank;
    private final Direction direction;

    public MoveCommand(Tank tank, Direction direction) {
        this.tank = tank;
        this.direction = direction;
    }

    @Override
    public void run() {
        tank.move(direction);
    }
}
