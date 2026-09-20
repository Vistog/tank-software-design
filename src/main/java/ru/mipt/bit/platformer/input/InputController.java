package ru.mipt.bit.platformer.input;

import ru.mipt.bit.platformer.model.Direction;
import ru.mipt.bit.platformer.model.Tank;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.IntPredicate;

public final class InputController {
    private final Tank tank;
    private final Map<Integer, Direction> movementKeys = new LinkedHashMap<>();
    private final Map<Integer, Runnable> actions = new LinkedHashMap<>();

    public InputController(Tank tank) {
        this.tank = tank;
    }

    public void bindMovement(int key, Direction direction) {
        movementKeys.put(key, direction);
    }

    public void bindAction(int key, Runnable action) {
        actions.put(key, action);
    }

    public void update(IntPredicate isPressed, IntPredicate isJustPressed) {
        for (Map.Entry<Integer, Direction> binding : movementKeys.entrySet()) {
            if (isPressed.test(binding.getKey())) {
                tank.move(binding.getValue());
                break;
            }
        }
        for (Map.Entry<Integer, Runnable> binding : actions.entrySet()) {
            if (isJustPressed.test(binding.getKey())) {
                binding.getValue().run();
            }
        }
    }
}
