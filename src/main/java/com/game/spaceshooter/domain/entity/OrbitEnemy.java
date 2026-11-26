package com.game.spaceshooter.domain.entity;

import com.game.spaceshooter.domain.movement.OrbitMovement;
import com.game.spaceshooter.domain.port.ProjectileFactory;
import com.game.spaceshooter.domain.weapon.RifleAssault;
import com.game.spaceshooter.infrastructure.config.EnemyConfig;

public class OrbitEnemy extends Enemy {
    private static final int DEFAULT_WIDTH = 50;
    private static final int DEFAULT_HEIGHT = 50;
    private static final double MAX_SPEED = EnemyConfig.ORBIT_MAX_SPEED;
    private static final int DEFAULT_HEALTH = EnemyConfig.ORBIT_DEFAULT_HEALTH;
    private static final int DEFAULT_DAMAGE = EnemyConfig.ORBIT_DEFAULT_DAMAGE;
    private static final double MOVE_RADIUS = EnemyConfig.ORBIT_MOVE_RADIUS;
    private static final int SCORE = EnemyConfig.ORBIT_SCORE;

    public OrbitEnemy(ProjectileFactory projectiles) {
        super(0, 0, DEFAULT_WIDTH, DEFAULT_HEIGHT, DEFAULT_HEALTH, DEFAULT_DAMAGE, EnemyType.ORBITING_ENEMY, SCORE);
        setMaxSpeed(MAX_SPEED);
        setWeapon(new RifleAssault(projectiles));
        setMovement(new OrbitMovement(MOVE_RADIUS, EnemyConfig.ACCELERATION, EnemyConfig.DRAG));
    }
}
