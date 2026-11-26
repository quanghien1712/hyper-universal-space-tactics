package com.game.spaceshooter.domain.effect;

import com.game.spaceshooter.infrastructure.utils.Vector2D;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EffectTest {

    private static final double EPS = 1e-9;
    private static final int ANI_SPEED = 2;
    private static final int TOTAL_FRAME = 3;

    private static class TestEffect extends Effect {
        TestEffect(double x, double y, int width, int height, int aniSpeed, int totalFrame, EffectType type) {
            super(x, y, width, height, aniSpeed, totalFrame, type);
        }

        TestEffect() {
            this(0, 0, 10, 10, ANI_SPEED, TOTAL_FRAME, EffectType.DEATH);
        }
    }

    private static void tick(Effect effect, int times) {
        for (int i = 0; i < times; i++) {
            effect.updateAnimation();
        }
    }

    private static TestEffect activeEffect() {
        TestEffect effect = new TestEffect();
        effect.setActive(true);
        return effect;
    }

    @Nested
    class Constructor {

        @Test
        void constructor_invalidArguments_throws() {
            assertThrows(IllegalArgumentException.class,
                    () -> new TestEffect(0, 0, 0, 10, ANI_SPEED, TOTAL_FRAME, EffectType.DEATH));
            assertThrows(IllegalArgumentException.class,
                    () -> new TestEffect(0, 0, 10, -1, ANI_SPEED, TOTAL_FRAME, EffectType.DEATH));
            assertThrows(IllegalArgumentException.class,
                    () -> new TestEffect(0, 0, 10, 10, 0, TOTAL_FRAME, EffectType.DEATH));
            assertThrows(IllegalArgumentException.class,
                    () -> new TestEffect(0, 0, 10, 10, ANI_SPEED, 0, EffectType.DEATH));
            assertThrows(IllegalArgumentException.class,
                    () -> new TestEffect(0, 0, 10, 10, ANI_SPEED, TOTAL_FRAME, null));
        }

        @Test
        void constructor_startsInactiveAtFirstFrame() {
            TestEffect effect = new TestEffect(3, 4, 10, 20, ANI_SPEED, TOTAL_FRAME, EffectType.PLASMA_EXPLOSION);

            assertFalse(effect.isActive());
            assertEquals(0, effect.getAniIndex());
            assertEquals(10, effect.getWidth());
            assertEquals(20, effect.getHeight());
            assertEquals(EffectType.PLASMA_EXPLOSION, effect.getEffectType());
            assertEquals(3, effect.getPosition().getX(), EPS);
            assertEquals(4, effect.getPosition().getY(), EPS);
        }

        @Test
        void explosionEffect_hasExplosionSizeAndType() {
            ExplosionEffect effect = new ExplosionEffect();

            assertEquals(60, effect.getWidth());
            assertEquals(60, effect.getHeight());
            assertEquals(EffectType.BULLET_EXPLOSION, effect.getEffectType());
        }

        @Test
        void deathEffect_hasDeathSizeAndType() {
            DeathEffect effect = new DeathEffect();

            assertEquals(85, effect.getWidth());
            assertEquals(85, effect.getHeight());
            assertEquals(EffectType.DEATH, effect.getEffectType());
        }
    }

    @Nested
    class Animation {

        @Test
        void updateAnimation_beforeAniSpeed_staysOnFrame() {
            TestEffect effect = activeEffect();

            tick(effect, ANI_SPEED - 1);

            assertEquals(0, effect.getAniIndex());
        }

        @Test
        void updateAnimation_reachAniSpeed_advancesFrame() {
            TestEffect effect = activeEffect();

            tick(effect, ANI_SPEED);
            assertEquals(1, effect.getAniIndex());

            tick(effect, ANI_SPEED);
            assertEquals(2, effect.getAniIndex());
        }

        @Test
        void updateAnimation_beforeLastFrameEnds_staysActive() {
            TestEffect effect = activeEffect();

            tick(effect, ANI_SPEED * TOTAL_FRAME - 1);

            assertTrue(effect.isActive());
            assertEquals(TOTAL_FRAME - 1, effect.getAniIndex());
        }

        @Test
        void updateAnimation_lastFrame_deactivates() {
            TestEffect effect = activeEffect();

            tick(effect, ANI_SPEED * TOTAL_FRAME);

            assertFalse(effect.isActive());
            assertEquals(0, effect.getAniIndex());
        }
    }

    @Nested
    class Pool {

        @Test
        void setActive_togglesState() {
            TestEffect effect = new TestEffect();

            effect.setActive(true);
            assertTrue(effect.isActive());

            effect.setActive(false);
            assertFalse(effect.isActive());
        }

        @Test
        void onRelease_resetsAnimation() {
            TestEffect effect = activeEffect();
            tick(effect, ANI_SPEED + 1);

            effect.onRelease();

            assertFalse(effect.isActive());
            assertEquals(0, effect.getAniIndex());
            effect.setActive(true);
            tick(effect, ANI_SPEED - 1);
            assertEquals(0, effect.getAniIndex(), "aniTick must also be reset");
        }
    }

    @Nested
    class PositionAndType {

        @Test
        void setPosition_copiesValue() {
            TestEffect effect = new TestEffect();
            Vector2D pos = new Vector2D(5, 6);

            effect.setPosition(pos);
            pos.copy(100, 100);

            assertEquals(5, effect.getPosition().getX(), EPS);
            assertEquals(6, effect.getPosition().getY(), EPS);
        }

        @Test
        void getPosition_returnsCopy() {
            TestEffect effect = new TestEffect(1, 2, 10, 10, ANI_SPEED, TOTAL_FRAME, EffectType.DEATH);

            effect.getPosition().copy(50, 50);

            assertEquals(1, effect.getPosition().getX(), EPS);
            assertEquals(2, effect.getPosition().getY(), EPS);
        }

        @Test
        void setPosition_null_throws() {
            TestEffect effect = new TestEffect();

            assertThrows(IllegalArgumentException.class, () -> effect.setPosition(null));
        }

        @Test
        void setEffectType_changesType() {
            TestEffect effect = new TestEffect();

            effect.setEffectType(EffectType.BULLET_EXPLOSION);

            assertEquals(EffectType.BULLET_EXPLOSION, effect.getEffectType());
        }

        @Test
        void setEffectType_null_throws() {
            TestEffect effect = new TestEffect();

            assertThrows(IllegalArgumentException.class, () -> effect.setEffectType(null));
            assertEquals(EffectType.DEATH, effect.getEffectType());
        }
    }
}
