package com.game.spaceshooter.domain.movement;

import com.game.spaceshooter.domain.entity.DynamicEntity;
import com.game.spaceshooter.infrastructure.utils.Vector2D;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LinearMovementTest {

    private static final double EPS = 1e-9;

    private LinearMovement movement;
    private TestEntity entity;

    private static class TestEntity extends DynamicEntity {
        TestEntity(double x, double y) {
            super(x, y, 10, 10);
            setMaxSpeed(1000);
        }

        void useMovement(MovementBehavior movement) {
            setMovement(movement);
        }

        @Override
        public void update(double deltaTime) {}
    }

    private static void assertVector(double expectedX, double expectedY, Vector2D actual) {
        assertEquals(expectedX, actual.getX(), EPS, "x");
        assertEquals(expectedY, actual.getY(), EPS, "y");
    }

    @BeforeEach
    void setUp() {
        movement = new LinearMovement();
        entity = new TestEntity(100, 100);
    }

    @Test
    void move_followsDirectionAndSpeed() {
        entity.setDirection(1, 0);
        entity.setSpeed(200);
        movement.move(entity, 0.5);
        assertVector(200, 100, entity.getPosition());
    }

    @Test
    void move_unnormalizedDirection_isNormalized() {
        entity.setDirection(3, 4);
        entity.setSpeed(10);
        movement.move(entity, 1);
        assertVector(106, 108, entity.getPosition());
    }

    @Test
    void move_diagonal_coversSpeedTimesDeltaTime() {
        entity.setDirection(-1, -1);
        entity.setSpeed(50);
        movement.move(entity, 2);
        Vector2D moved = entity.getPosition().subtract(new Vector2D(100, 100));
        assertEquals(100, moved.length(), EPS);
        assertEquals(moved.getX(), moved.getY(), EPS);
        assertTrue(moved.getX() < 0);
    }

    @Test
    void move_zeroDirection_staysInPlace() {
        entity.setSpeed(200);
        movement.move(entity, 1);
        assertVector(100, 100, entity.getPosition());
    }

    @Test
    void move_zeroSpeed_staysInPlace() {
        entity.setDirection(0, 1);
        movement.move(entity, 1);
        assertVector(100, 100, entity.getPosition());
    }

    @Test
    void move_zeroDeltaTime_staysInPlace() {
        entity.setDirection(0, 1);
        entity.setSpeed(200);
        movement.move(entity, 0);
        assertVector(100, 100, entity.getPosition());
    }

    @Test
    void move_repeatedSteps_accumulate() {
        entity.setDirection(0, 1);
        entity.setSpeed(120);
        for (int i = 0; i < 120; i++) {
            movement.move(entity, 1.0 / 120);
        }
        assertVector(100, 220, entity.getPosition());
    }

    @Test
    void move_doesNotChangeDirectionOrSpeed() {
        entity.setDirection(3, 4);
        entity.setSpeed(10);
        movement.move(entity, 1);
        assertVector(3, 4, entity.getDirection());
        assertEquals(10, entity.getSpeed());
    }

    @Test
    void move_sharedInstance_movesEntitiesIndependently() {
        TestEntity other = new TestEntity(0, 0);
        entity.setDirection(1, 0);
        entity.setSpeed(10);
        other.setDirection(0, -1);
        other.setSpeed(20);
        movement.move(entity, 1);
        movement.move(other, 1);
        assertVector(110, 100, entity.getPosition());
        assertVector(0, -20, other.getPosition());
    }

    @Test
    void entityMove_usesLinearMovement() {
        entity.useMovement(movement);
        entity.setDirection(0, -1);
        entity.setSpeed(40);
        entity.move(0.5);
        assertVector(100, 80, entity.getPosition());
    }
}
