package ru.mipt.bit.platformer.graphics;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Rectangle;
import static ru.mipt.bit.platformer.util.GdxGameUtils.*;

public final class SpriteRenderer implements EntityRenderer {
    private final Sprite sprite;

    public SpriteRenderer(Sprite sprite) {
        this.sprite = sprite;
    }

    @Override
    public Rectangle bounds() {
        return createBoundingRectangle(sprite);
    }

    @Override
    public void render(Batch batch, Rectangle bounds, float rotation) {
        batch.setColor(sprite.getColor());
        drawTextureRegionUnscaled(batch, sprite, bounds, rotation);
    }
}
