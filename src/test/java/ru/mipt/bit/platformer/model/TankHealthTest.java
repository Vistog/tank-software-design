package ru.mipt.bit.platformer.model;

import org.junit.Test;
import static org.junit.Assert.*;

public class TankHealthTest {
    @Test public void defaultHealthIsOneHundred() {
        Tank tank = new Tank(1);
        assertEquals(100, tank.health());
        assertEquals(1f, tank.healthFraction(), 0f);
        tank.takeDamage(25);
        assertEquals(0.75f, tank.healthFraction(), 0f);
    }

    @Test public void initialHealthDefinesFullBarAndDamageCannotGoBelowZero() {
        Tank tank = new Tank(1, 80);
        assertEquals(1f, tank.healthFraction(), 0f);
        tank.takeDamage(20);
        assertEquals(0.75f, tank.healthFraction(), 0f);
        tank.takeDamage(100);
        assertEquals(0f, tank.healthFraction(), 0f);
        assertFalse(tank.isAlive());
    }
}
