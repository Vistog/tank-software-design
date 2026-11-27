package ru.mipt.bit.platformer.graphics;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import org.junit.Test;
import ru.mipt.bit.platformer.model.Tank;
import static org.junit.Assert.*;

public class HealthBarRendererTest {
    private final Rectangle bounds = new Rectangle(20, 30, 100, 80);
    private int draws;
    private final EntityRenderer sprite = new EntityRenderer() {
        public Rectangle bounds() { return bounds; }
        public void render(Batch batch, Rectangle rectangle, float rotation) {
            assertSame(bounds, rectangle);
            assertEquals(90f, rotation, 0f);
            draws++;
        }
    };

    @Test public void decoratorPreservesSpriteBounds() {
        HealthBarRenderer decorated = new HealthBarRenderer(sprite, null, new Tank(1), () -> false);
        assertSame(bounds, decorated.bounds());
    }

    @Test public void hiddenBarStillDrawsSpriteWithoutChangingHealth() {
        Tank tank = new Tank(1, 80);
        tank.takeDamage(20);
        HealthBarRenderer decorated = new HealthBarRenderer(sprite, null, tank, () -> false);
        // Hidden bars need neither a texture nor an OpenGL context.
        decorated.render(null, bounds, 90);
        decorated.render(null, bounds, 90);
        assertEquals(2, draws);
        assertEquals(60, tank.health());
    }
}
