package com.game.spaceshooter.domain.entity;

import com.game.spaceshooter.domain.movement.MovementBehavior;
import com.game.spaceshooter.infrastructure.config.GameConfig;
import com.game.spaceshooter.infrastructure.utils.Vector2D;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DynamicEntityTest {

    private static final double EPS = 1e-9;

    private TestEntity entity;

    private static class TestEntity extends DynamicEntity {
        TestEntity(double x, double y, int width, int height) {
            super(x, y, width, height);
        }

        @Override
        public void update(double deltaTime) {}
    }

    private static class LimitedEntity extends TestEntity {
        private final double limit;

        LimitedEntity(double limit) {
            super(0, 0, 10, 10);
            this.limit = limit;
        }

        @Override
        protected double getSpeedLimit() {
            return limit;
        }
    }

    private static class RecordingMovement implements MovementBehavior {
        private final List<DynamicEntity> entities = new ArrayList<>();
        private final List<Double> deltaTimes = new ArrayList<>();

        @Override
        public void move(DynamicEntity entity, double deltaTime) {
            entities.add(entity);
            deltaTimes.add(deltaTime);
        }
    }

    private static void assertVector(double expectedX, double expectedY, Vector2D actual) {
        assertEquals(expectedX, actual.getX(), EPS, "x");
        assertEquals(expectedY, actual.getY(), EPS, "y");
    }

    @BeforeEach
    void setUp() {
        entity = new TestEntity(100, 200, 40, 20);
    }

    @Nested
    @DisplayName("Constructor")
    class ConstructorTests {
        @Test
        void constructor_validArgs_storesPositionAndSize() {
            assertVector(100, 200, entity.getPosition());
            assertEquals(40, entity.getWidth());
            assertEquals(20, entity.getHeight());
        }

        @Test
        void constructor_initialMotionState_isAtRest() {
            assertEquals(0, entity.getSpeed());
            assertEquals(0, entity.getCurrentAngle());
            assertVector(0, 0, entity.getDirection());
        }

        @Test
        void constructor_bound_isCenteredOnPosition() {
            assertEquals(new Rectangle(80, 190, 40, 20), entity.getBound());
        }

        @Test
        void constructor_nonPositiveSize_throws() {
            assertThrows(IllegalArgumentException.class, () -> new TestEntity(0, 0, 0, 10));
            assertThrows(IllegalArgumentException.class, () -> new TestEntity(0, 0, 10, 0));
            assertThrows(IllegalArgumentException.class, () -> new TestEntity(0, 0, -5, 10));
            assertThrows(IllegalArgumentException.class, () -> new TestEntity(0, 0, 10, -5));
        }
    }

    @Nested
    @DisplayName("Position & bound")
    class PositionTests {
        @Test
        void getPosition_returnsCopy() {
            Vector2D pos = entity.getPosition();
            pos.copy(-1, -1);
            assertVector(100, 200, entity.getPosition());
        }

        @Test
        void setPosition_vector_copiesValue() {
            Vector2D newPos = new Vector2D(5, 6);
            entity.setPosition(newPos);
            newPos.copy(50, 60);
            assertVector(5, 6, entity.getPosition());
        }

        @Test
        void setPosition_coordinates_updatesPosition() {
            entity.setPosition(-3.5, 7.25);
            assertVector(-3.5, 7.25, entity.getPosition());
        }

        @Test
        void setPosition_null_throws() {
            assertThrows(NullPointerException.class, () -> entity.setPosition(null));
            assertVector(100, 200, entity.getPosition());
        }

        @Test
        void setPosition_doesNotMoveBound() {
            entity.setPosition(0, 0);
            assertEquals(new Rectangle(80, 190, 40, 20), entity.getBound());
        }

        @Test
        void setBoundPos_centersBoundOnScreenPos() {
            entity.setBoundPos(new Vector2D(50, 50));
            assertEquals(new Rectangle(30, 40, 40, 20), entity.getBound());
            assertVector(100, 200, entity.getPosition());
        }

        @Test
        void setBoundPos_null_throws() {
            assertThrows(NullPointerException.class, () -> entity.setBoundPos(null));
        }

        @Test
        void getBound_returnsCopy() {
            Rectangle bound = entity.getBound();
            bound.setLocation(0, 0);
            assertEquals(new Rectangle(80, 190, 40, 20), entity.getBound());
        }
    }

    @Nested
    @DisplayName("Direction")
    class DirectionTests {
        @Test
        void setDirection_vector_copiesValue() {
            Vector2D dir = new Vector2D(1, 0);
            entity.setDirection(dir);
            dir.copy(0, 1);
            assertVector(1, 0, entity.getDirection());
        }

        @Test
        void setDirection_coordinates_updatesDirection() {
            entity.setDirection(3, 4);
            assertVector(3, 4, entity.getDirection());
        }

        @Test
        void getDirection_returnsCopy() {
            entity.setDirection(1, 0);
            entity.getDirection().copy(9, 9);
            assertVector(1, 0, entity.getDirection());
        }

        @Test
        void setDirection_null_throws() {
            assertThrows(IllegalArgumentException.class, () -> entity.setDirection(null));
        }
    }

    @Nested
    @DisplayName("Speed")
    class SpeedTests {
        @BeforeEach
        void setUp() {
            entity.setMaxSpeed(100);
        }

        @Test
        void setSpeed_withinLimit_storesValue() {
            entity.setSpeed(40);
            assertEquals(40, entity.getSpeed());
        }

        @Test
        void setSpeed_aboveLimit_capsAtLimit() {
            entity.setSpeed(500);
            assertEquals(100, entity.getSpeed());
        }

        @Test
        void setSpeed_negative_throws() {
            entity.setSpeed(30);
            assertThrows(IllegalArgumentException.class, () -> entity.setSpeed(-1));
            assertEquals(30, entity.getSpeed());
        }

        @Test
        void setSpeed_defaultMaxSpeed_isZero() {
            TestEntity fresh = new TestEntity(0, 0, 10, 10);
            fresh.setSpeed(50);
            assertEquals(0, fresh.getSpeed());
        }

        @Test
        void setMaxSpeed_lowerThanSpeed_reducesSpeed() {
            entity.setSpeed(80);
            entity.setMaxSpeed(30);
            assertEquals(30, entity.getSpeed());
        }

        @Test
        void setMaxSpeed_higherThanSpeed_keepsSpeed() {
            entity.setSpeed(80);
            entity.setMaxSpeed(200);
            assertEquals(80, entity.getSpeed());
        }

        @Test
        void setMaxSpeed_negative_throws() {
            assertThrows(IllegalArgumentException.class, () -> entity.setMaxSpeed(-1));
        }

        @Test
        void accelerate_increasesSpeed() {
            entity.accelerate(30);
            entity.accelerate(20);
            assertEquals(50, entity.getSpeed());
        }

        @Test
        void accelerate_capsAtSpeedLimit() {
            entity.setSpeed(90);
            entity.accelerate(30);
            assertEquals(100, entity.getSpeed());
        }

        @Test
        void accelerate_negative_throws() {
            assertThrows(IllegalArgumentException.class, () -> entity.accelerate(-1));
        }

        @Test
        void dragged_decreasesSpeed() {
            entity.setSpeed(50);
            entity.dragged(20);
            assertEquals(30, entity.getSpeed());
        }

        @Test
        void dragged_stopsAtZero() {
            entity.setSpeed(10);
            entity.dragged(25);
            assertEquals(0, entity.getSpeed());
        }

        @Test
        void dragged_negative_throws() {
            assertThrows(IllegalArgumentException.class, () -> entity.dragged(-1));
        }

        @Test
        void overriddenSpeedLimit_isUsedForCapping() {
            LimitedEntity limited = new LimitedEntity(25);
            limited.setSpeed(100);
            assertEquals(25, limited.getSpeed());
            limited.setSpeed(0);
            limited.accelerate(40);
            assertEquals(25, limited.getSpeed());
        }
    }

    @Nested
    @DisplayName("Movement strategy")
    class MovementTests {
        @Test
        void move_delegatesToMovementBehavior() {
            RecordingMovement movement = new RecordingMovement();
            entity.setMovement(movement);
            entity.move(0.25);
            assertEquals(List.of(entity), movement.entities);
            assertEquals(List.of(0.25), movement.deltaTimes);
        }

        @Test
        void setMovement_replacesStrategy() {
            RecordingMovement first = new RecordingMovement();
            RecordingMovement second = new RecordingMovement();
            entity.setMovement(first);
            entity.setMovement(second);
            assertSame(second, entity.getMovement());
            entity.move(1);
            assertTrue(first.entities.isEmpty());
            assertEquals(1, second.entities.size());
        }

        @Test
        void setMovement_null_throws() {
            assertThrows(NullPointerException.class, () -> entity.setMovement(null));
        }

        @Test
        void move_negativeDeltaTime_throws() {
            RecordingMovement movement = new RecordingMovement();
            entity.setMovement(movement);
            assertThrows(IllegalArgumentException.class, () -> entity.move(-0.1));
            assertTrue(movement.entities.isEmpty());
        }
    }

    @Nested
    @DisplayName("Rotation")
    class RotationTests {
        @Test
        void updateRotation_angleZero_pointsUp() {
            entity.updateRotation(1);
            assertVector(0, -1, entity.getDirection());
        }

        @Test
        void updateRotation_angleHalfPi_pointsRight() {
            entity.setCurrentAngle(Math.PI / 2);
            entity.setTargetAngle(Math.PI / 2);
            entity.updateRotation(1);
            assertVector(1, 0, entity.getDirection());
        }

        @Test
        void updateRotation_turnsProportionally() {
            entity.setRotationSpeed(2);
            entity.setTargetAngle(1.0);
            entity.updateRotation(0.25);
            assertEquals(0.5, entity.getCurrentAngle(), EPS);
            assertVector(Math.cos(0.5 - Math.PI / 2), Math.sin(0.5 - Math.PI / 2), entity.getDirection());
        }

        @Test
        void updateRotation_takesShortestWay() {
            entity.setRotationSpeed(1);
            entity.setCurrentAngle(0.1);
            entity.setTargetAngle(2 * Math.PI - 0.1);
            entity.updateRotation(0.5);
            assertEquals(0.0, entity.getCurrentAngle(), EPS);
        }

        @Test
        void updateRotation_belowThreshold_snapsToTarget() {
            double target = GameConfig.ROTATION_THRESHOLD / 2;
            entity.setTargetAngle(target);
            entity.updateRotation(1);
            assertEquals(target, entity.getCurrentAngle(), EPS);
        }

        @Test
        void updateRotation_zeroRotationSpeed_keepsAngle() {
            entity.setTargetAngle(1.0);
            entity.updateRotation(1);
            assertEquals(0, entity.getCurrentAngle(), EPS);
        }

        @Test
        void updateRotation_negativeDeltaTime_throws() {
            assertThrows(IllegalArgumentException.class, () -> entity.updateRotation(-1));
        }

        @Test
        void setRotationSpeed_negative_throws() {
            assertThrows(IllegalArgumentException.class, () -> entity.setRotationSpeed(-1));
        }
    }
}
