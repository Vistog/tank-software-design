package ru.mipt.bit.platformer.input;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.IntPredicate;

public final class InputController {
    private final Map<Integer, Runnable> heldActions = new LinkedHashMap<>();
    private final Map<Integer, Runnable> actions = new LinkedHashMap<>();

    // First held binding wins; useful for mutually exclusive commands.
    public void bindHeld(int key, Runnable action) {
        heldActions.put(key, action);
    }

    public void bindAction(int key, Runnable action) {
        actions.put(key, action);
    }

    public void update(IntPredicate isPressed, IntPredicate isJustPressed) {
        for (Map.Entry<Integer, Runnable> binding : heldActions.entrySet()) {
            if (isPressed.test(binding.getKey())) {
                binding.getValue().run();
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
