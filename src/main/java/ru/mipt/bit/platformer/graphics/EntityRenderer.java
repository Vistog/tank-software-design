package ru.mipt.bit.platformer.graphics;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;

/** Infrastructure: internal graphics contract used by the decorator. */
public interface EntityRenderer {
    Rectangle bounds();
    void render(Batch batch, Rectangle bounds, float rotation);
}
