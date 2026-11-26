package com.game.spaceshooter.application.port;

import com.game.spaceshooter.domain.projectiles.Projectile;

public interface ProjectileSpawner {
    void addActiveProjectile(Projectile projectile);
}
