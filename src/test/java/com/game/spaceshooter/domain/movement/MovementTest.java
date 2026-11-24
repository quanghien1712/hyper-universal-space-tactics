package com.game.spaceshooter.domain.movement;

import com.game.spaceshooter.domain.entity.DynamicEntity;
import com.game.spaceshooter.infrastructure.config.EnemyConfig;
import com.game.spaceshooter.infrastructure.utils.Vector2D;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MovementTest {

    private static final double EPS = 1e-9;

    private static class TestEntity extends DynamicEntity {
        TestEntity(double x, double y, double rotationSpeed) {
            super(x, y, 10, 10);
            setMaxSpeed(1000);
            setRotationSpeed(rotationSpeed);
        }

        @Override
        public void update(double deltaTime) {}
    }

    private static void assertVector(double expectedX, double expectedY, Vector2D actual) {
        assertEquals(expectedX, actual.getX(), EPS, "x");
        assertEquals(expectedY, actual.getY(), EPS, "y");
    }

    @Nested
    class MoveToTargetTests {

        @Test
        void moveToTarget_negativeParams_throws() {
            assertThrows(IllegalArgumentException.class, () -> new MoveToTarget(-1, 1, 1));
            assertThrows(IllegalArgumentException.class, () -> new MoveToTarget(1, -1, 1));
            assertThrows(IllegalArgumentException.class, () -> new MoveToTarget(1, 1, -1));
        }

        @Test
        void moveToTarget_noTarget_doesNothing() {
            TestEntity entity = new TestEntity(10, 20, 0);
            entity.setSpeed(30);
            MoveToTarget movement = new MoveToTarget(0, 50, 5);

            movement.move(entity, 1);

            assertVector(10, 20, entity.getPosition());
            assertEquals(30, entity.getSpeed(), EPS);
        }

        @Test
        void moveToTarget_movesTowardTarget() {
            TestEntity entity = new TestEntity(0, 0, 0);
            entity.setCurrentAngle(Math.PI / 2);
            MoveToTarget movement = new MoveToTarget(100, 50, 5);
            movement.setTarget(() -> new Vector2D(1000, 0));

            movement.move(entity, 1);

            assertEquals(50, entity.getSpeed(), EPS);
            assertVector(50, 0, entity.getPosition());
            assertVector(1, 0, entity.getDirection());
        }

        @Test
        void moveToTarget_withinRadius_appliesDrag() {
            TestEntity entity = new TestEntity(0, 0, 0);
            entity.setCurrentAngle(Math.PI / 2);
            entity.setSpeed(30);
            MoveToTarget movement = new MoveToTarget(100, 50, 5);
            movement.setTarget(() -> new Vector2D(10, 0));

            movement.move(entity, 1);

            assertEquals(25, entity.getSpeed(), EPS);
            assertVector(25, 0, entity.getPosition());
        }

        @Test
        void moveToTarget_turnsTowardTarget() {
            TestEntity entity = new TestEntity(0, 0, 1);
            MoveToTarget movement = new MoveToTarget(0, 0, 0);
            movement.setTarget(() -> new Vector2D(100, 0));

            movement.move(entity, 0.5);

            assertEquals(Math.PI / 4, entity.getCurrentAngle(), EPS);
        }

        @Test
        void moveToTarget_followsUpdatedTarget() {
            TestEntity entity = new TestEntity(0, 0, 0);
            entity.setCurrentAngle(Math.PI);
            Vector2D target = new Vector2D(1000, 0);
            MoveToTarget movement = new MoveToTarget(0, 10, 0);
            movement.setTarget(target::copy);

            target.copy(0, 1000);
            movement.move(entity, 1);

            assertVector(0, 10, entity.getPosition());
        }
    }

    @Nested
    class OrbitMovementTests {

        @Test
        void orbitMovement_negativeParams_throws() {
            assertThrows(IllegalArgumentException.class, () -> new OrbitMovement(-1, 1, 1));
            assertThrows(IllegalArgumentException.class, () -> new OrbitMovement(1, -1, 1));
            assertThrows(IllegalArgumentException.class, () -> new OrbitMovement(1, 1, -1));
        }

        @Test
        void orbitMovement_noTarget_doesNothing() {
            TestEntity entity = new TestEntity(10, 20, 0);
            entity.setSpeed(30);
            entity.setCurrentAngle(1);
            OrbitMovement movement = new OrbitMovement(0, 50, 5);

            movement.move(entity, 1);

            assertVector(10, 20, entity.getPosition());
            assertEquals(30, entity.getSpeed(), EPS);
            assertEquals(1, entity.getCurrentAngle(), EPS);
        }

        @Test
        void orbitMovement_rotatesByRotationSpeed() {
            TestEntity entity = new TestEntity(0, 0, 0);
            OrbitMovement movement = new OrbitMovement(100, 0, 0);
            movement.setTarget(() -> new Vector2D(0, 0));

            movement.move(entity, 0.5);

            double angle = EnemyConfig.ROTATION_SPEED * 0.5;
            assertEquals(angle, entity.getCurrentAngle(), EPS);
            assertVector(Math.cos(angle - Math.PI / 2), Math.sin(angle - Math.PI / 2), entity.getDirection());
        }

        @Test
        void orbitMovement_angleStaysWithinFullCircle() {
            TestEntity entity = new TestEntity(0, 0, 0);
            OrbitMovement movement = new OrbitMovement(100, 0, 0);
            movement.setTarget(() -> new Vector2D(0, 0));

            movement.move(entity, 5);

            assertEquals((EnemyConfig.ROTATION_SPEED * 5) % (2 * Math.PI), entity.getCurrentAngle(), EPS);
        }

        @Test
        void orbitMovement_outsideRadius_movesTowardTarget() {
            TestEntity entity = new TestEntity(0, 0, 0);
            OrbitMovement movement = new OrbitMovement(100, 50, 5);
            movement.setTarget(() -> new Vector2D(1000, 0));

            movement.move(entity, 0.5);

            assertEquals(50, entity.getSpeed(), EPS);
            assertVector(25, 0, entity.getPosition());
        }

        @Test
        void orbitMovement_withinRadius_appliesDragAndStays() {
            TestEntity entity = new TestEntity(0, 0, 0);
            entity.setSpeed(30);
            OrbitMovement movement = new OrbitMovement(100, 50, 10);
            movement.setTarget(() -> new Vector2D(10, 0));

            movement.move(entity, 1);

            assertEquals(20, entity.getSpeed(), EPS);
            assertVector(0, 0, entity.getPosition());
        }
    }
}
