package com.game.spaceshooter.domain.weapon;

import com.game.spaceshooter.domain.projectiles.Projectile;
import com.game.spaceshooter.infrastructure.utils.Vector2D;

public abstract class Weapon {
    private final double fireInterval;
    private double cooldownRemaining;

    public Weapon(double fireRate) {
        if (fireRate <= 0) {
            throw new IllegalArgumentException("fireRate must be positive");
        }
        this.fireInterval = 1.0 / fireRate;
        this.cooldownRemaining = fireInterval;
    }

    public abstract Projectile createProjectile(Vector2D playerPos, Vector2D aimDirection);

    public void update(double deltaTime) {
        if (deltaTime < 0) {
            throw new IllegalArgumentException("deltaTime must be nonnegative");
        }
        cooldownRemaining = Math.max(0.0, cooldownRemaining - deltaTime);
    }

    public Projectile fire(Vector2D playerPos, Vector2D aimDirection) {
        if (playerPos == null || aimDirection == null) {
            throw new IllegalArgumentException("Position and direction are required");
        }
        if (cooldownRemaining > 0) {
            return null;
        }
        cooldownRemaining = fireInterval;
        return createProjectile(playerPos, aimDirection);
    }

    public double getCooldownRemaining() {
        return cooldownRemaining;
    }

    public double getFireInterval() {
        return fireInterval;
    }
}
