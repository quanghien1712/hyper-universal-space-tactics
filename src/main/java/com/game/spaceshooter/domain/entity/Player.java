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
    private double boostCooldown = 0;
    private double boostTimer = 0;
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
        boostCooldown = Math.max(0, boostCooldown - deltaTime);
        updateRotation(deltaTime);
        if (state == PlayerState.BOOSTING) {
            boost(deltaTime);
        }
        if (state == PlayerState.BOOST_DECAY) {
            boostDecay(deltaTime);
        }
        move(deltaTime);
    }

    public void startBoost() {
        if (!shouldBoost()) return;
        setState(PlayerState.BOOSTING);
        boostCooldown = PlayerConfig.BOOST_COOLDOWN;
    }

    @Override
    protected double getSpeedLimit() {
        return state == PlayerState.BOOSTING || state == PlayerState.BOOST_DECAY
              ? PlayerConfig.BOOST_SPEED : super.getSpeedLimit();
    }

    private void boost(double deltaTime) {
        boostTimer += deltaTime;
        setSpeed(PlayerConfig.BOOST_SPEED);
        if (boostTimer >= PlayerConfig.BOOST_DURATION) {
            setState(PlayerState.BOOST_DECAY);
            boostTimer = 0;
        }
    }

    private void boostDecay(double deltaTime) {
        setSpeed(Math.max(PlayerConfig.MAX_SPEED, getSpeed() - PlayerConfig.DECAY_RATE * deltaTime));
        if (getSpeed() <= PlayerConfig.MAX_SPEED) {
            setState(PlayerState.NORMAL);
            setSpeed(PlayerConfig.MAX_SPEED);
        }
    }

    @Override
    public void updateRotation(double deltaTime) {
        requireNonNegative(deltaTime, "deltaTime");
        if (state == PlayerState.BOOSTING || state == PlayerState.BOOST_DECAY) {
            super.updateRotation(deltaTime);
        } else setCurrentAngle(viewAngle);
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

    public double getBoostCooldown() {
        return boostCooldown;
    }

    public double getBoostCooldownDuration() {
        return PlayerConfig.BOOST_COOLDOWN;
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

    public double getBoostProgress() {
        return boostTimer / PlayerConfig.BOOST_DURATION;
    }

    public boolean shouldBoost() {
        return boostCooldown == 0;
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
        setSpeed(getSpeed());
    }

}
