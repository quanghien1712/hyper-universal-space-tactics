package com.game.spaceshooter.infrastructure.config;

public class GameConfig {
    private GameConfig(){}

    public static final int TILE_SIZE = 48;
    public static final int TILES_IN_WIDTH = 31;
    public static final int TILES_IN_HEIGHT = 16;
    public static final int PANEL_WIDTH = TILE_SIZE * TILES_IN_WIDTH;
    public static final int PANEL_HEIGHT = TILE_SIZE * TILES_IN_HEIGHT;
}
