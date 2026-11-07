package ru.mipt.bit.platformer.control;

import ru.mipt.bit.platformer.model.*;
import java.util.Random;
import java.util.function.IntFunction;
import java.util.function.Consumer;

public final class RandomControl implements ControlStrategy {
    private final Tank tank;
    private final IntFunction<EntityState> states;
    private final Random random;
    private final Consumer<Runnable> submit;
    private final Direction[] directions = Direction.values();

    public RandomControl(Tank tank, IntFunction<EntityState> states, Random random,
                         Consumer<Runnable> submit) {
        this.tank = tank;
        this.states = states;
        this.random = random;
        this.submit = submit;
    }

    @Override
    public void update() {
        if (!tank.isAlive() || states.apply(tank.id).progress < 1f) return;
        int choice = random.nextInt(directions.length + 1);
        if (choice == directions.length) submit.accept(new ShootCommand(tank));
        else submit.accept(new MoveCommand(tank, directions[choice]));
    }
}
