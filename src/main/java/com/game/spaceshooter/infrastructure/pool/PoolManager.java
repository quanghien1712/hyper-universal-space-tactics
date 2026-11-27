package com.game.spaceshooter.infrastructure.pool;

import com.game.spaceshooter.application.port.GameObjectPool;
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

import java.util.Map;

public class PoolManager implements GameObjectPool {
    private final Map<ProjectileType, ObjectPool<Projectile>> projectilePools = Map.of(
          ProjectileType.PLASMA, new ObjectPool<>(Plasma::new, 30),
          ProjectileType.HOMING_BULLET, new ObjectPool<>(HomingBullet::new, 30),
          ProjectileType.NORMAL_BULLET, new ObjectPool<>(NormalBullet::new, 80)
    );

    private final Map<EnemyType, ObjectPool<Enemy>> enemyPools = Map.of(
          EnemyType.BASIC_ENEMY, new ObjectPool<>(() -> new BasicEnemy(this), 20),
          EnemyType.ORBITING_ENEMY, new ObjectPool<>(() -> new OrbitEnemy(this), 20),
          EnemyType.DRONE, new ObjectPool<>(Drone::new, 20),
          EnemyType.BOSS, new ObjectPool<>(() -> new Boss(this), 1)
    );

    private final Map<EffectType, ObjectPool<Effect>> effectPools = Map.of(
          EffectType.PLASMA_EXPLOSION, new ObjectPool<>(ExplosionEffect::new, 15),
          EffectType.BULLET_EXPLOSION, new ObjectPool<>(ExplosionEffect::new, 15),
          EffectType.DEATH, new ObjectPool<>(DeathEffect::new, 20)
    );

    @Override
    public Projectile acquire(ProjectileType type) {
        ObjectPool<Projectile> pool = projectilePools.get(type);
        if (pool == null) throw new IllegalArgumentException("No pool for type: " + type);
        return pool.acquire();
    }

    @Override
    public Effect acquire(EffectType effectType) {
        ObjectPool<Effect> pool = effectPools.get(effectType);
        if (pool == null) throw new IllegalArgumentException("No pool for type: " + effectType);
        Effect effect = pool.acquire();
        effect.setEffectType(effectType);
        return effect;
    }

    @Override
    public Enemy acquire(EnemyType type) {
        ObjectPool<Enemy> pool = enemyPools.get(type);
        if (pool == null) throw new IllegalArgumentException("No pool for type: " + type);
        return pool.acquire();
    }

    @Override
    public void release(Effect effect) {
        ObjectPool<Effect> pool = effectPools.get(effect.getEffectType());
        if (pool == null) throw new IllegalArgumentException("No pool for type: " + effect.getEffectType());
        pool.release(effect);
    }

    @Override
    public void release(Projectile projectile) {
        ObjectPool<Projectile> pool = projectilePools.get(projectile.getProjectileType());
        if (pool == null) throw new IllegalArgumentException("No pool for type: " + projectile.getProjectileType());
        pool.release(projectile);
    }

    @Override
    public void release(Enemy enemy) {
        ObjectPool<Enemy> pool = enemyPools.get(enemy.getEnemyType());
        if (pool == null) throw new IllegalArgumentException("No pool for type: " + enemy.getEnemyType());
        pool.release(enemy);
    }
}
