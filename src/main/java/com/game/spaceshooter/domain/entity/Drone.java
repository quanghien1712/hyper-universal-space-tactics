package com.game.spaceshooter.domain.entity;

import com.game.spaceshooter.domain.movement.MoveToTarget;
import com.game.spaceshooter.infrastructure.config.EnemyConfig;

public class Drone extends Enemy{
    private static final int DEFAULT_WIDTH = 30;
    private static final int DEFAULT_HEIGHT = 30;
    private static final double MAX_SPEED = EnemyConfig.DRONE_MAX_SPEED;
    private static final int DEFAULT_HEALTH = EnemyConfig.DRONE_DEFAULT_HEALTH;
    private static final int DEFAULT_DAMAGE = EnemyConfig.DRONE_DEFAULT_DAMAGE;
    private static final double MOVE_RADIUS = EnemyConfig.DRONE_MOVE_RADIUS;
    private static final int SCORE = EnemyConfig.DRONE_SCORE;

    public Drone() {
        super(0, 0, DEFAULT_WIDTH, DEFAULT_HEIGHT, DEFAULT_HEALTH, DEFAULT_DAMAGE, EnemyType.DRONE, SCORE);
        setMaxSpeed(MAX_SPEED);
        setMovement(new MoveToTarget(MOVE_RADIUS, EnemyConfig.ACCELERATION, EnemyConfig.DRAG));
    }

}
