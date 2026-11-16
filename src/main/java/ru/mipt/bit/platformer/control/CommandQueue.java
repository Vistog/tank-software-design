package ru.mipt.bit.platformer.control;

import java.util.ArrayList;
import java.util.List;

/** Application: receives and executes commands through the Runnable input port. */
public final class CommandQueue {
    private final List<Runnable> pending = new ArrayList<>();

    public void add(Runnable command) {
        pending.add(command);
    }

    public void execute() {
        List<Runnable> commands = new ArrayList<>(pending);
        pending.clear();
        for (Runnable command : commands) command.run();
    }
}
