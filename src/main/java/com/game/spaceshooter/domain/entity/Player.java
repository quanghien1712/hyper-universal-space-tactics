package com.game.spaceshooter.domain.entity;

import com.game.spaceshooter.domain.movement.LinearMovement;
import com.game.spaceshooter.domain.port.GameEventListener;
import com.game.spaceshooter.domain.port.ProjectileFactory;
import com.game.spaceshooter.domain.projectiles.Projectile;
import com.game.spaceshooter.domain.weapon.PlasmaGun;
import com.game.spaceshooter.domain.weapon.Weapon;
import com.game.spaceshooter.infrastructure.config.PlayerConfig;
import com.game.spaceshooter.infrastructure.utils.Vector2D;

public class Player extends DynamicEntity implements Damageable {
    private int health;
    private PlayerState state;
    private double viewAngle = 0;
    private int score;
    private final Weapon weapon;
    private final GameEventListener events;


    public Player(double x, double y, ProjectileFactory projectiles, GameEventListener events) {
        super(x, y, PlayerConfig.DEFAULT_WIDTH, PlayerConfig.DEFAULT_HEIGHT);
        setSpeed(0);
        setRotationSpeed(PlayerConfig.ROTATION_SPEED);
        setMaxSpeed(PlayerConfig.MAX_SPEED);
        this.health = PlayerConfig.MAX_HEALTH;
        setMovement(new LinearMovement());
        state = PlayerState.NORMAL;
        weapon = new PlasmaGun(projectiles);
        if (events == null) throw new IllegalArgumentException("events is required");
        this.events = events;
    }

    @Override
    public void update(double deltaTime) {
        weapon.update(deltaTime);
        updateRotation(deltaTime);
        move(deltaTime);
    }

    @Override
    public void updateRotation(double deltaTime) {
        requireNonNegative(deltaTime, "deltaTime");
        setCurrentAngle(viewAngle);
    }

    public Projectile shoot(Vector2D direction) {
        if (direction == null) throw new IllegalArgumentException("direction is required");
        Vector2D deltaPos = direction.normalize();
        deltaPos.multiplyInPlace(PlayerConfig.SPRITE_SIZE / 2.0);
        Projectile projectile = weapon.fire(getPosition().add(deltaPos), direction);
        if (projectile != null) {
            projectile.setCurrentAngle(viewAngle);
            projectile.setOwner(Projectile.Owner.PLAYER);
        }
        return projectile;
    }

    @Override
    public boolean isDead() {
        return health <= 0;
    }

    @Override
    public void takeDamage(int damage) {
        if (damage < 0) throw new IllegalArgumentException("damage must be nonnegative");
        events.onPlayerDamaged();
        health = Math.max(0, health - damage);
    }

    @Override
    public Vector2D getPos() {
        return getPosition();
    }

    public int getScore() {
        return score;
    }

    public void addScore(int score) {
        if (score < 0) {
            throw new IllegalArgumentException("score must be nonnegative");
        }
        this.score += score;
    }

    public double getViewAngle() {
        return viewAngle;
    }

    public void setViewAngle(double viewAngle) {
        this.viewAngle = viewAngle;
    }

    public int getHealth() {
        return health;
    }

    public PlayerState getPlayerState() {
        return state;
    }

    public void setState(PlayerState state) {
        if (state == null) throw new IllegalArgumentException("state is required");
        this.state = state;
    }

}
