package com.game.spaceshooter.application.manager;

import com.game.spaceshooter.application.model.Camera;
import com.game.spaceshooter.application.port.GameObjectPool;
import com.game.spaceshooter.domain.entity.Enemy;
import com.game.spaceshooter.domain.entity.EnemyType;
import com.game.spaceshooter.infrastructure.config.EnemyConfig;
import com.game.spaceshooter.infrastructure.config.GameConfig;
import com.game.spaceshooter.infrastructure.utils.Vector2D;

import java.util.Objects;
import java.util.Random;
import java.util.function.Consumer;

public class WaveManager {
    private final Consumer<Enemy> enemies;
    private final GameObjectPool pool;
    private final Random random;
    private final Camera camera;
    private int spawnedEnemies, defeatedEnemies;
    private double spawnTimer, waveTimer;
    private int enemiesPerWave;
    private int currentWave;
    private boolean waveInProgress;
    private boolean bossWave;

    public WaveManager(Consumer<Enemy> enemies, Camera camera, GameObjectPool pool) {
        this.enemies = Objects.requireNonNull(enemies);
        this.pool = Objects.requireNonNull(pool);
        this.camera = camera;
        this.random = new Random();
    }

    public void update(double deltaTime) {
        if (deltaTime < 0) {
            throw new IllegalArgumentException("deltaTime must be nonnegative");
        }
        if (!waveInProgress) {
            waveTimer += deltaTime;
            if (waveTimer >= EnemyConfig.WAVE_DELAY) {
                waveTimer = 0;
                startNextWave();
            }
        }
        if (spawnedEnemies < enemiesPerWave) {
            spawnTimer += deltaTime;
            if (spawnTimer >= EnemyConfig.SPAWN_INTERVAL) {
                spawnTimer = 0;
                spawnEnemy();
            }
        }
        if (waveInProgress && spawnedEnemies == enemiesPerWave && defeatedEnemies >= enemiesPerWave) {
            waveInProgress = false;
            waveTimer = 0;
        }
    }

    private void startNextWave() {
        waveInProgress = true;
        currentWave++;
        bossWave = currentWave % EnemyConfig.BOSS_INTERVAL == 0;
        enemiesPerWave = (int) Math.min(Integer.MAX_VALUE, EnemyConfig.BASE_ENEMIES + (long) currentWave * EnemyConfig.ENEMIES_PER_LEVEL);
        spawnedEnemies = 0;
        defeatedEnemies = 0;
        spawnTimer = EnemyConfig.SPAWN_INTERVAL;
    }

    private EnemyType getEnemyType() {
        if (bossWave && spawnedEnemies == 0) {
            return EnemyType.BOSS;
        }
        return switch (random.nextInt(3)) {
            case 0 -> EnemyType.BASIC_ENEMY;
            case 1 -> EnemyType.ORBITING_ENEMY;
            default -> EnemyType.DRONE;
        };
    }

    private void spawnEnemy() {
        Enemy enemy = pool.acquire(getEnemyType());
        double x, y;
        int side = random.nextInt(4);
        int margin = 500;
        Vector2D cameraPos = camera.getCameraPos();
        switch (side) {
            case 0 -> { //left
                x = cameraPos.getX() - margin;
                y = cameraPos.getY() + random.nextInt(GameConfig.PANEL_HEIGHT);
            }
            case 1 -> { //right
                x = cameraPos.getX() + GameConfig.PANEL_WIDTH + margin;
                y = cameraPos.getY() + random.nextInt(GameConfig.PANEL_HEIGHT);
            }
            case 2 -> { //top
                x = cameraPos.getX() + random.nextInt(GameConfig.PANEL_WIDTH);
                y = cameraPos.getY() - margin;
            }
            default -> { //bottom
                x = cameraPos.getX() + random.nextInt(GameConfig.PANEL_WIDTH);
                y = cameraPos.getY() + GameConfig.PANEL_HEIGHT + margin;
            }
        }
        enemy.setPosition(x, y);
        spawnedEnemies++;
        enemies.accept(enemy);
    }

    public void enemyOnDefeated() {
        if (waveInProgress && defeatedEnemies < spawnedEnemies) defeatedEnemies++;
    }

    public int getCurrentWave() {
        return currentWave;
    }
    public boolean isBossWave() {
        return bossWave;
    }
}
