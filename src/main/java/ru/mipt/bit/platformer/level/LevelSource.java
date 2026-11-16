package ru.mipt.bit.platformer.level;

import java.io.IOException;

/** Application: output port for loading initial level data. */
public interface LevelSource {
    LevelData load() throws IOException;
}
