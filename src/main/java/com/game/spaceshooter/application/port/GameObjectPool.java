package com.game.spaceshooter.application.port;

import com.game.spaceshooter.domain.effect.Effect;
import com.game.spaceshooter.domain.effect.EffectType;
import com.game.spaceshooter.domain.entity.Enemy;
import com.game.spaceshooter.domain.entity.EnemyType;
import com.game.spaceshooter.domain.port.ProjectileFactory;
import com.game.spaceshooter.domain.projectiles.Projectile;

public interface GameObjectPool extends ProjectileFactory {
    Enemy acquire(EnemyType type);
    Effect acquire(EffectType type);
    void release(Enemy enemy);
    void release(Projectile projectile);
    void release(Effect effect);
}
