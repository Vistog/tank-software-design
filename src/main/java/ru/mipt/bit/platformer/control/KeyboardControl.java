package ru.mipt.bit.platformer.control;

import ru.mipt.bit.platformer.input.InputController;
import java.util.function.IntPredicate;

public final class KeyboardControl implements ControlStrategy {
    private final InputController input;
    private final IntPredicate isPressed;
    private final IntPredicate isJustPressed;

    public KeyboardControl(InputController input, IntPredicate isPressed, IntPredicate isJustPressed) {
        this.input = input;
        this.isPressed = isPressed;
        this.isJustPressed = isJustPressed;
    }

    @Override
    public void update() {
        input.update(isPressed, isJustPressed);
    }
}
