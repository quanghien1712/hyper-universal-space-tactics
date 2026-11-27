package com.game.spaceshooter.infrastructure.pool;

import com.game.spaceshooter.domain.effect.DeathEffect;
import com.game.spaceshooter.domain.effect.Effect;
import com.game.spaceshooter.domain.effect.EffectType;
import com.game.spaceshooter.domain.effect.ExplosionEffect;
import com.game.spaceshooter.domain.entity.BasicEnemy;
import com.game.spaceshooter.domain.entity.Boss;
import com.game.spaceshooter.domain.entity.Drone;
import com.game.spaceshooter.domain.entity.Enemy;
import com.game.spaceshooter.domain.entity.EnemyType;
import com.game.spaceshooter.domain.entity.OrbitEnemy;
import com.game.spaceshooter.domain.projectiles.HomingBullet;
import com.game.spaceshooter.domain.projectiles.NormalBullet;
import com.game.spaceshooter.domain.projectiles.Plasma;
import com.game.spaceshooter.domain.projectiles.Projectile;
import com.game.spaceshooter.domain.projectiles.ProjectileType;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PoolManagerTest {

    private final PoolManager pool = new PoolManager();

    @Nested
    class Acquire {

        @Test
        void acquire_eachEnemyType_returnsMatchingClass() {
            assertInstanceOf(BasicEnemy.class, pool.acquire(EnemyType.BASIC_ENEMY));
            assertInstanceOf(OrbitEnemy.class, pool.acquire(EnemyType.ORBITING_ENEMY));
            assertInstanceOf(Drone.class, pool.acquire(EnemyType.DRONE));
            assertInstanceOf(Boss.class, pool.acquire(EnemyType.BOSS));
        }

        @Test
        void acquire_eachProjectileType_returnsMatchingClass() {
            assertInstanceOf(Plasma.class, pool.acquire(ProjectileType.PLASMA));
            assertInstanceOf(HomingBullet.class, pool.acquire(ProjectileType.HOMING_BULLET));
            assertInstanceOf(NormalBullet.class, pool.acquire(ProjectileType.NORMAL_BULLET));
        }

        @Test
        void acquire_eachEffectType_returnsMatchingClass() {
            assertInstanceOf(ExplosionEffect.class, pool.acquire(EffectType.BULLET_EXPLOSION));
            assertInstanceOf(ExplosionEffect.class, pool.acquire(EffectType.PLASMA_EXPLOSION));
            assertInstanceOf(DeathEffect.class, pool.acquire(EffectType.DEATH));
        }

        @Test
        void acquire_effect_setsEffectType() {
            Effect plasmaExplosion = pool.acquire(EffectType.PLASMA_EXPLOSION);
            Effect bulletExplosion = pool.acquire(EffectType.BULLET_EXPLOSION);

            assertEquals(EffectType.PLASMA_EXPLOSION, plasmaExplosion.getEffectType());
            assertEquals(EffectType.BULLET_EXPLOSION, bulletExplosion.getEffectType());
        }

        @Test
        void acquire_returnsActiveObjects() {
            assertTrue(pool.acquire(EnemyType.DRONE).isActive());
            assertTrue(pool.acquire(ProjectileType.NORMAL_BULLET).isActive());
            assertTrue(pool.acquire(EffectType.DEATH).isActive());
        }

        @Test
        void acquire_twice_returnsDifferentInstances() {
            assertNotSame(pool.acquire(EnemyType.DRONE), pool.acquire(EnemyType.DRONE));
            assertNotSame(pool.acquire(ProjectileType.PLASMA), pool.acquire(ProjectileType.PLASMA));
            assertNotSame(pool.acquire(EffectType.DEATH), pool.acquire(EffectType.DEATH));
        }

        @Test
        void acquire_nullType_throws() {
            assertThrows(NullPointerException.class, () -> pool.acquire((EnemyType) null));
            assertThrows(NullPointerException.class, () -> pool.acquire((ProjectileType) null));
            assertThrows(NullPointerException.class, () -> pool.acquire((EffectType) null));
        }

        @Test
        void acquire_bossBeyondPreallocated_createsNew() {
            Enemy first = pool.acquire(EnemyType.BOSS);
            Enemy second = pool.acquire(EnemyType.BOSS);

            assertInstanceOf(Boss.class, second);
            assertNotSame(first, second);
        }
    }

    @Nested
    class Release {

        @Test
        void release_enemy_deactivatesAndResets() {
            Enemy boss = pool.acquire(EnemyType.BOSS);
            boss.takeDamage(1);

            pool.release(boss);

            assertFalse(boss.isActive());
            assertEquals(boss.getMaxHealth(), boss.getHealth());
        }

        @Test
        void release_thenAcquire_reusesSameEnemy() {
            Enemy boss = pool.acquire(EnemyType.BOSS);

            pool.release(boss);

            assertSame(boss, pool.acquire(EnemyType.BOSS));
        }

        @Test
        void release_projectile_deactivates() {
            Projectile projectile = pool.acquire(ProjectileType.HOMING_BULLET);

            pool.release(projectile);

            assertFalse(projectile.isActive());
        }

        @Test
        void release_effect_deactivatesAndResetsAnimation() {
            Effect effect = pool.acquire(EffectType.BULLET_EXPLOSION);
            for (int i = 0; i < 10; i++) {
                effect.updateAnimation();
            }

            pool.release(effect);

            assertFalse(effect.isActive());
            assertEquals(0, effect.getAniIndex());
        }
    }

    @Test
    void poolsAreIsolatedBetweenManagers() {
        PoolManager other = new PoolManager();
        Enemy boss = pool.acquire(EnemyType.BOSS);
        Enemy otherBoss = other.acquire(EnemyType.BOSS);

        assertNotSame(boss, otherBoss);

        pool.release(boss);

        assertNotSame(boss, other.acquire(EnemyType.BOSS));
        assertSame(boss, pool.acquire(EnemyType.BOSS));
    }
}
