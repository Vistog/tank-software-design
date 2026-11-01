package ru.mipt.bit.platformer.input;

import org.junit.Test;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static org.junit.Assert.*;

public class InputCommandsTest {
    @Test
    public void commandsDoNotNeedTankOrWorld() {
        InputController input = new InputController(Runnable::run);
        List<String> calls = new ArrayList<>();
        input.bindHeld(1, () -> calls.add("camera"));
        input.bindHeld(2, () -> calls.add("zoom"));
        input.bindAction(3, () -> calls.add("pause"));
        input.bindAction(4, () -> calls.add("menu"));

        input.update(key -> true, key -> true);
        assertEquals(Arrays.asList("camera", "pause", "menu"), calls);
        input.update(key -> key == 2, key -> false);
        assertEquals(Arrays.asList("camera", "pause", "menu", "zoom"), calls);
    }

    @Test
    public void actionCanBeReplacedWithoutChangingController() {
        InputController input = new InputController(Runnable::run);
        List<String> calls = new ArrayList<>();
        input.bindAction(1, () -> calls.add("old"));
        input.bindAction(1, () -> calls.add("new"));
        input.update(key -> false, key -> key == 1);
        assertEquals(Arrays.asList("new"), calls);
    }

    @Test
    public void unboundKeysDoNothing() {
        InputController input = new InputController(Runnable::run);
        input.bindHeld(1, () -> fail("Wrong held key"));
        input.bindAction(2, () -> fail("Wrong action key"));
        input.update(key -> key == 99, key -> key == 99);
    }

    @Test
    public void emptyControllerDoesNotQueryKeyboard() {
        InputController input = new InputController(Runnable::run);
        input.update(key -> { fail("No bindings"); return false; },
                key -> { fail("No bindings"); return false; });
    }
}
