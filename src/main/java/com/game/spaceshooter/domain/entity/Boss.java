package com.game.spaceshooter.domain.entity;

import com.game.spaceshooter.domain.movement.MoveToTarget;
import com.game.spaceshooter.domain.port.ProjectileFactory;
import com.game.spaceshooter.domain.weapon.RifleAssault;
import com.game.spaceshooter.infrastructure.config.EnemyConfig;

public class Boss extends Enemy{
    private static final int DEFAULT_WIDTH = 300;
    private static final int DEFAULT_HEIGHT = 240;
    private static final double MAX_SPEED = EnemyConfig.BOSS_MAX_SPEED;
    private static final int DEFAULT_HEALTH = EnemyConfig.BOSS_DEFAULT_HEALTH;
    private static final int DEFAULT_DAMAGE = EnemyConfig.BOSS_DEFAULT_DAMAGE;
    private static final double MOVE_RADIUS = EnemyConfig.BOSS_MOVE_RADIUS;
    private static final int SCORE = EnemyConfig.BOSS_SCORE;

    public Boss(ProjectileFactory projectiles) {
        super(0, 0, DEFAULT_WIDTH, DEFAULT_HEIGHT, DEFAULT_HEALTH, DEFAULT_DAMAGE, EnemyType.BOSS, SCORE);
        setMaxSpeed(MAX_SPEED);
        setWeapon(new RifleAssault(projectiles));
        setMovement(new MoveToTarget(MOVE_RADIUS, EnemyConfig.ACCELERATION, EnemyConfig.DRAG));
    }
}
