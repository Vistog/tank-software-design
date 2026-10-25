package ru.mipt.bit.platformer.control;

import ru.mipt.bit.platformer.model.*;
import java.util.Random;
import java.util.function.IntFunction;

public final class RandomControl implements ControlStrategy {
    private final Tank tank;
    private final IntFunction<EntityState> states;
    private final Random random;
    private final Direction[] directions = Direction.values();

    public RandomControl(Tank tank, IntFunction<EntityState> states, Random random) {
        this.tank = tank;
        this.states = states;
        this.random = random;
    }

    @Override
    public void update() {
        if (states.apply(tank.id).progress < 1f) return;
        Direction direction = directions[random.nextInt(directions.length)];
        new MoveCommand(tank, direction).run();
    }
}
