package com.game.spaceshooter.domain.weapon;

import com.game.spaceshooter.domain.port.ProjectileFactory;
import com.game.spaceshooter.domain.projectiles.Projectile;
import com.game.spaceshooter.domain.projectiles.ProjectileType;
import com.game.spaceshooter.infrastructure.utils.Vector2D;

public class PlasmaGun extends Weapon{
    private static final double FIRE_RATE = 7;

    private final ProjectileFactory projectiles;

    public PlasmaGun(ProjectileFactory projectiles) {
        super(FIRE_RATE);
        if (projectiles == null) throw new IllegalArgumentException("projectiles is required");
        this.projectiles = projectiles;
    }

    @Override
    public Projectile createProjectile(Vector2D playerPos, Vector2D aimDirection) {
        Projectile projectile = projectiles.acquire(ProjectileType.PLASMA);
        projectile.setPosition(playerPos);
        projectile.setDirection(aimDirection);
        return projectile;
    }
}
