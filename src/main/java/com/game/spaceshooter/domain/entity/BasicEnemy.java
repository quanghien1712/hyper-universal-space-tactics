package com.game.spaceshooter.domain.entity;

import com.game.spaceshooter.domain.movement.MoveToTarget;
import com.game.spaceshooter.domain.port.ProjectileFactory;
import com.game.spaceshooter.domain.weapon.SmartGun;
import com.game.spaceshooter.infrastructure.config.EnemyConfig;

public class BasicEnemy extends Enemy{
    private static final int DEFAULT_WIDTH = 60;
    private static final int DEFAULT_HEIGHT = 60;
    private static final double MAX_SPEED = EnemyConfig.BASIC_MAX_SPEED;
    private static final int DEFAULT_HEALTH = EnemyConfig.BASIC_DEFAULT_HEALTH;
    private static final int DEFAULT_DAMAGE = EnemyConfig.BASIC_DEFAULT_DAMAGE;
    private static final double MOVE_RADIUS = EnemyConfig.BASIC_MOVE_RADIUS;
    private static final int SCORE = EnemyConfig.BASIC_SCORE;

    public BasicEnemy(ProjectileFactory projectiles) {
        super(0, 0, DEFAULT_WIDTH, DEFAULT_HEIGHT, DEFAULT_HEALTH, DEFAULT_DAMAGE, EnemyType.BASIC_ENEMY, SCORE);
        setMaxSpeed(MAX_SPEED);
        setWeapon(new SmartGun(projectiles));
        setMovement(new MoveToTarget(MOVE_RADIUS, EnemyConfig.ACCELERATION, EnemyConfig.DRAG));
    }
}
