package com.game.spaceshooter.domain.projectiles;

import com.game.spaceshooter.domain.entity.Damageable;
import com.game.spaceshooter.domain.movement.MoveToTarget;

public class HomingBullet extends Projectile{
    private static final int DEFAULT_WIDTH = 10;
    private static final int DEFAULT_HEIGHT = 60;
    private static final int DEFAULT_DAMAGE = 5;
    private static final double DEFAULT_SPEED = 260;
    private static final double ROTATION_SPEED = 1.4;

    private Damageable target;

    public HomingBullet() {
        super(0, 0, DEFAULT_WIDTH, DEFAULT_HEIGHT, DEFAULT_DAMAGE, ProjectileType.HOMING_BULLET);
        setRotationSpeed(ROTATION_SPEED);
        setMaxSpeed(DEFAULT_SPEED);
        setSpeed(DEFAULT_SPEED);
        setMovement(new MoveToTarget(0, 2, 0));
    }

    @Override
    public void update(double deltaTime) {
        if (target != null && !target.isDead()) {
            move(deltaTime);
        }
    }

    @Override
    public void setTarget(Damageable target) {
        this.target = target;
        getMovement().setTarget(target == null ? null : target::getPos);
    }

    @Override
    public void onRelease() {
        super.onRelease();
        setTarget(null);
    }
}
