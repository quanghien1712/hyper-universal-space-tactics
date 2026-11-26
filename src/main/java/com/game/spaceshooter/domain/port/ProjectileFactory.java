package com.game.spaceshooter.domain.port;

import com.game.spaceshooter.domain.projectiles.Projectile;
import com.game.spaceshooter.domain.projectiles.ProjectileType;

public interface ProjectileFactory {
    Projectile acquire(ProjectileType type);
}
