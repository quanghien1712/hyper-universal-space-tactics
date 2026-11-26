package com.game.spaceshooter.domain.port;

import com.game.spaceshooter.domain.projectiles.Projectile;
import com.game.spaceshooter.infrastructure.utils.Vector2D;

/** Handlers run before pooled objects are released; copy data that must be retained. */
public interface GameEventListener {
    void onPlayerDamaged();
    void onPlayerShot();
    void onPlayerBoosted();
    void onEnemyDefeated(Vector2D position);
    void onProjectileHit(Projectile projectile);
}
