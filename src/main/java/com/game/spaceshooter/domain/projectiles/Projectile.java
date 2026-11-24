package com.game.spaceshooter.domain.projectiles;

import com.game.spaceshooter.domain.entity.Damageable;
import com.game.spaceshooter.domain.entity.DynamicEntity;
import com.game.spaceshooter.infrastructure.pool.Poolable;

import java.awt.*;

public abstract class Projectile extends DynamicEntity implements Poolable {
    public enum Owner {
        PLAYER,
        ENEMY,
        NONE
    }

    private final int damage;
    private final ProjectileType projectileType;
    private boolean active;
    private Owner owner = Owner.NONE;

    protected Projectile(double x, double y, int width, int height, int damage, ProjectileType type) {
        super(x, y, width, height);
        if (damage < 0 || type == null) throw new IllegalArgumentException("Damage must be nonnegative and type required");
        this.damage = damage;
        this.projectileType = type;
    }

    @Override
    public void update(double deltaTime) {
        move(deltaTime);
    }

    public int getDamage() { return damage; }

    public void onHit(Damageable target) {
        if (target == null) throw new IllegalArgumentException("target is required");
        target.takeDamage(damage);
    }

    public void setTarget(Damageable target) {}

    public ProjectileType getProjectileType() {
        return projectileType;
    }


    @Override
    public void onRelease() {
        setPosition(0, 0);
        setDirection(0, 0);
        active = false;
        owner = Owner.NONE;
        setCurrentAngle(0);
        setTargetAngle(0);
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public void setActive(boolean active) {
        this.active = active;
    }

    public void setOwner(Owner owner) {
        if (owner == null) throw new IllegalArgumentException("owner is required");
        this.owner = owner;
    }

    public Owner getOwner() {
        return owner;
    }

}
