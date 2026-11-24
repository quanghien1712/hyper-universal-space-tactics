package com.game.spaceshooter.domain.projectiles;

import com.game.spaceshooter.domain.movement.LinearMovement;

public class Plasma extends Projectile {
    private static final int DEFAULT_WIDTH = 30;
    private static final int DEFAULT_HEIGHT = 30;
    private static final int DEFAULT_DAMAGE = 20;
    private static final double DEFAULT_SPEED = 1000;


    public Plasma() {
        super(0, 0, DEFAULT_WIDTH, DEFAULT_HEIGHT, DEFAULT_DAMAGE, ProjectileType.PLASMA);
        setMovement(new LinearMovement());
        setMaxSpeed(DEFAULT_SPEED);
        setSpeed(DEFAULT_SPEED);
    }




}
