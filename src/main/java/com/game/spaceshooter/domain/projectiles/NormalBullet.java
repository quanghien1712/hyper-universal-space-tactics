package com.game.spaceshooter.domain.projectiles;

import com.game.spaceshooter.domain.movement.LinearMovement;

public class NormalBullet extends Projectile {

    private static final int DEFAULT_WIDTH = 10;
    private static final int DEFAULT_HEIGHT = 40;
    private static final int DEFAULT_DAMAGE = 15;
    private static final double DEFAULT_SPEED = 700;


    public NormalBullet() {
        super(0, 0, DEFAULT_WIDTH, DEFAULT_HEIGHT, DEFAULT_DAMAGE, ProjectileType.NORMAL_BULLET);
        setMovement(new LinearMovement());
        setMaxSpeed(DEFAULT_SPEED);
        setSpeed(DEFAULT_SPEED);
    }
}
