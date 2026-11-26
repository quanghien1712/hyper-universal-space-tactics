package com.game.spaceshooter.infrastructure.config;

public class PlayerConfig {
    private PlayerConfig() {}


    public static final int MAX_HEALTH = 1000;
    public static final int DEFAULT_WIDTH = 60;
    public static final int DEFAULT_HEIGHT = 60;

    public static final double MAX_SPEED = 200;

    public static final double ROTATION_SPEED = 3;

    public static final double SIZE_SCALE = 1.7;
    public static final int SPRITE_SIZE = (int) (GameConfig.TILE_SIZE * SIZE_SCALE);
}
