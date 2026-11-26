package com.game.spaceshooter.infrastructure.config;

public class EnemyConfig {
    private EnemyConfig() {}

    public static final double ROTATION_SPEED = 1.4;

    public static final double ACCELERATION = 2;
    public static final double DRAG = 2;

    public static final double BASIC_MAX_SPEED = 250;
    public static final int BASIC_DEFAULT_HEALTH = 100;
    public static final int BASIC_DEFAULT_DAMAGE = 50;
    public static final int BASIC_SCORE = 100;
    public static final double BASIC_MOVE_RADIUS = 700;
    public static final double DRONE_MAX_SPEED = 350;
    public static final int DRONE_DEFAULT_HEALTH = 200;
    public static final int DRONE_DEFAULT_DAMAGE = 200;
    public static final int DRONE_SCORE = 200;
    public static final double DRONE_MOVE_RADIUS = 0;
}
