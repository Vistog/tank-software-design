package ru.mipt.bit.platformer;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import ru.mipt.bit.platformer.level.FileLevelSource;
import java.nio.file.Paths;

public class GameDesktopLauncher {
    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setWindowedMode(1280, 1024);
        TankGame game = args.length == 0 ? new TankGame()
                : new TankGame(new FileLevelSource(Paths.get(args[0])), "level.tmx", 0.4f);
        new Lwjgl3Application(game, config);
    }
}
