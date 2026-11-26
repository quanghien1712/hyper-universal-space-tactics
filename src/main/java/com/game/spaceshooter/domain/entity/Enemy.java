package com.game.spaceshooter.domain.entity;

import com.game.spaceshooter.domain.projectiles.Projectile;
import com.game.spaceshooter.domain.weapon.Weapon;
import com.game.spaceshooter.infrastructure.config.EnemyConfig;
import com.game.spaceshooter.infrastructure.pool.Poolable;
import com.game.spaceshooter.infrastructure.utils.Vector2D;

import java.util.Objects;

public abstract class Enemy extends DynamicEntity implements Damageable, Poolable {
    private int health;
    private final int maxHealth;
    private final int damage;
    private final EnemyType enemyType;
    private boolean active;
    private Weapon weapon;
    private final int defeatedScore;

    protected Enemy(double x, double y, int width, int height,
                    int maxHealth, int damage, EnemyType type, int defeatedScore) {
        super(x, y, width, height);
        if (maxHealth <= 0 || damage < 0 || defeatedScore < 0 || type == null) {
            throw new IllegalArgumentException("Enemy health must be positive | damage/score nonnegative | type required");
        }
        this.maxHealth = maxHealth;
        this.health = maxHealth;
        this.damage = damage;
        this.enemyType = type;
        this.defeatedScore = defeatedScore;
        setRotationSpeed(EnemyConfig.ROTATION_SPEED);
    }

    protected void setWeapon(Weapon weapon) {
        this.weapon = Objects.requireNonNull(weapon);
    }
    public int getDamage() {
        return damage;
    }

    public EnemyType getEnemyType() {
        return this.enemyType;
    }

    @Override
    public void update(double deltaTime) {
        requireNonNegative(deltaTime, "deltaTime");
        if (weapon != null) {
            weapon.update(deltaTime);
        }
        move(deltaTime);
    }

    public Projectile shoot() {
        if (weapon == null) return null;
        Vector2D direction = getDirection();
        Vector2D deltaPos = direction.normalize();
        deltaPos.multiplyInPlace(getHeight() / 2.0);
        Projectile projectile = weapon.fire(getPosition().add(deltaPos), direction);
        if (projectile != null) {
            projectile.setOwner(Projectile.Owner.ENEMY);
            projectile.setCurrentAngle(getCurrentAngle());
        }
        return projectile;
    }
    public int getMaxHealth() {
        return maxHealth;
    }

    public int getDefeatedScore() {
        return defeatedScore;
    }

    public int getHealth() {
        return health;
    }

    @Override
    public void takeDamage(int damage) {
        if (damage < 0) throw new IllegalArgumentException("damage must be nonnegative");
        health = Math.max(0, health - damage);
    }

    @Override
    public boolean isDead() {
        return health <= 0;
    }

    @Override
    public Vector2D getPos() {
        return getPosition();
    }

    @Override
    public void onRelease() {
        setTarget(null);
        setPosition(0, 0);
        setDirection(0, 0);
        setCurrentAngle(0);
        setTargetAngle(0);
        active = false;
        health = maxHealth;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public void setActive(boolean active) {
        this.active = active;
    }

    public void setTarget(Damageable target) {
        getMovement().setTarget(target == null ? null : target::getPos);
    }

}
