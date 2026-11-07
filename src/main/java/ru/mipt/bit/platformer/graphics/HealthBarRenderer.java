package ru.mipt.bit.platformer.graphics;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import java.util.function.BooleanSupplier;
import ru.mipt.bit.platformer.model.Tank;

public final class HealthBarRenderer implements EntityRenderer {
    private final EntityRenderer wrapped;
    private final TextureRegion pixel;
    private final Tank tank;
    private final BooleanSupplier visible;

    public HealthBarRenderer(EntityRenderer wrapped, TextureRegion pixel,
                             Tank tank, BooleanSupplier visible) {
        this.wrapped = wrapped;
        this.pixel = pixel;
        this.tank = tank;
        this.visible = visible;
    }

    @Override
    public Rectangle bounds() {
        return wrapped.bounds();
    }

    @Override
    public void render(Batch batch, Rectangle bounds, float rotation) {
        wrapped.render(batch, bounds, rotation);
        if (!visible.getAsBoolean()) return;

        float width = bounds.width * 0.8f;
        float x = bounds.x + (bounds.width - width) / 2;
        // Keep the bar inside the top of the sprite so it fits at the map boundary.
        float y = bounds.y + bounds.height - 10;
        float color = batch.getPackedColor();
        batch.setColor(Color.DARK_GRAY);
        batch.draw(pixel, x, y, width, 8);
        batch.setColor(Color.GREEN);
        batch.draw(pixel, x, y, width * tank.healthFraction(), 8);
        batch.setPackedColor(color);
    }
}
