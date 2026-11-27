package com.game.spaceshooter.application.manager;

import com.game.spaceshooter.domain.effect.DeathEffect;
import com.game.spaceshooter.domain.effect.Effect;
import com.game.spaceshooter.domain.effect.EffectType;
import com.game.spaceshooter.domain.effect.ExplosionEffect;
import com.game.spaceshooter.infrastructure.pool.PoolManager;
import com.game.spaceshooter.infrastructure.utils.Vector2D;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EffectManagerTest {

    private static final double EPS = 1e-9;
    /** ExplosionEffect: 16 frames, 3 updates per frame. */
    private static final int EXPLOSION_UPDATES = 16 * 3;

    private static class RecordingPool extends PoolManager {
        private final List<Effect> released = new ArrayList<>();

        @Override
        public void release(Effect effect) {
            released.add(effect);
            super.release(effect);
        }
    }

    private final RecordingPool pool = new RecordingPool();
    private final EffectManager effects = new EffectManager(pool);

    private void update(int times) {
        for (int i = 0; i < times; i++) {
            effects.update();
        }
    }

    @Test
    void constructor_nullPool_throws() {
        assertThrows(NullPointerException.class, () -> new EffectManager(null));
    }

    @Nested
    class Spawn {

        @Test
        void spawn_addsActiveEffect() {
            effects.spawn(EffectType.BULLET_EXPLOSION, new Vector2D(10, 20));

            List<Effect> snapshot = effects.snapshot();
            assertEquals(1, snapshot.size());
            Effect effect = snapshot.get(0);
            assertTrue(effect.isActive());
            assertEquals(EffectType.BULLET_EXPLOSION, effect.getEffectType());
            assertEquals(10, effect.getPosition().getX(), EPS);
            assertEquals(20, effect.getPosition().getY(), EPS);
        }

        @Test
        void spawn_usesClassOfRequestedType() {
            effects.spawn(EffectType.PLASMA_EXPLOSION, new Vector2D(0, 0));
            effects.spawn(EffectType.DEATH, new Vector2D(0, 0));

            List<Effect> snapshot = effects.snapshot();
            assertInstanceOf(ExplosionEffect.class, snapshot.get(0));
            assertEquals(EffectType.PLASMA_EXPLOSION, snapshot.get(0).getEffectType());
            assertInstanceOf(DeathEffect.class, snapshot.get(1));
        }

        @Test
        void spawn_copiesPosition() {
            Vector2D position = new Vector2D(5, 5);

            effects.spawn(EffectType.DEATH, position);
            position.copy(100, 100);

            assertEquals(5, effects.snapshot().get(0).getPosition().getX(), EPS);
        }

        @Test
        void addActiveEffect_addsGivenEffect() {
            Effect effect = new DeathEffect();
            effect.setActive(true);

            effects.addActiveEffect(effect);

            assertEquals(List.of(effect), effects.snapshot());
        }

        @Test
        void snapshot_isIndependentCopy() {
            effects.spawn(EffectType.DEATH, new Vector2D(0, 0));

            effects.snapshot().clear();

            assertEquals(1, effects.snapshot().size());
        }
    }

    @Nested
    class Update {

        @Test
        void update_advancesAnimation() {
            effects.spawn(EffectType.BULLET_EXPLOSION, new Vector2D(0, 0));

            update(3);

            assertEquals(1, effects.snapshot().get(0).getAniIndex());
        }

        @Test
        void unfinishedAnimation_staysInList() {
            effects.spawn(EffectType.BULLET_EXPLOSION, new Vector2D(0, 0));

            update(EXPLOSION_UPDATES - 1);

            assertEquals(1, effects.snapshot().size());
            assertTrue(pool.released.isEmpty());
        }

        @Test
        void finishedAnimation_releasedToPool() {
            effects.spawn(EffectType.BULLET_EXPLOSION, new Vector2D(0, 0));
            Effect effect = effects.snapshot().get(0);

            update(EXPLOSION_UPDATES);

            assertTrue(effects.snapshot().isEmpty());
            assertEquals(List.of(effect), pool.released);
            assertFalse(effect.isActive());
        }

        @Test
        void update_removesOnlyFinishedEffects() {
            effects.spawn(EffectType.BULLET_EXPLOSION, new Vector2D(0, 0));
            effects.spawn(EffectType.DEATH, new Vector2D(0, 0));
            Effect death = effects.snapshot().get(1);

            update(EXPLOSION_UPDATES);

            assertEquals(List.of(death), effects.snapshot());
            assertEquals(1, pool.released.size());
        }

        @Test
        void update_severalFinishInSameFrame_allReleased() {
            for (int i = 0; i < 3; i++) {
                effects.spawn(EffectType.BULLET_EXPLOSION, new Vector2D(i, i));
            }
            List<Effect> spawned = effects.snapshot();

            update(EXPLOSION_UPDATES);

            assertTrue(effects.snapshot().isEmpty());
            assertEquals(3, pool.released.size());
            assertTrue(pool.released.containsAll(spawned));
        }

        @Test
        void update_inactiveEffect_releasedWithoutWaitingForAnimation() {
            effects.spawn(EffectType.DEATH, new Vector2D(0, 0));
            Effect effect = effects.snapshot().get(0);
            effect.setActive(false);

            effects.update();

            assertTrue(effects.snapshot().isEmpty());
            assertEquals(List.of(effect), pool.released);
        }
    }
}
