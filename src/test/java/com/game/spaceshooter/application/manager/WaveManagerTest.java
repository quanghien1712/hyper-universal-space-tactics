package com.game.spaceshooter.application.manager;

import com.game.spaceshooter.application.model.Camera;
import com.game.spaceshooter.domain.entity.Enemy;
import com.game.spaceshooter.domain.entity.EnemyType;
import com.game.spaceshooter.infrastructure.config.EnemyConfig;
import com.game.spaceshooter.infrastructure.config.GameConfig;
import com.game.spaceshooter.infrastructure.pool.PoolManager;
import com.game.spaceshooter.infrastructure.utils.Vector2D;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WaveManagerTest {

    private final List<Enemy> spawned = new ArrayList<>();
    private final WaveManager waves = new WaveManager(spawned::add, new Camera(), new PoolManager());

    private static int enemiesInWave(int wave) {
        return EnemyConfig.BASE_ENEMIES + wave * EnemyConfig.ENEMIES_PER_LEVEL;
    }

    /** Starts the next wave and spawns all of its enemies. */
    private void spawnWholeWave() {
        int expectedTotal = spawned.size() + enemiesInWave(waves.getCurrentWave() + 1);
        waves.update(EnemyConfig.WAVE_DELAY);
        while (spawned.size() < expectedTotal) {
            waves.update(EnemyConfig.SPAWN_INTERVAL);
        }
    }

    private void defeat(int count) {
        for (int i = 0; i < count; i++) {
            waves.enemyOnDefeated();
        }
    }

    /** Spawns and defeats every enemy of the next wave, then lets the manager close it. */
    private void clearWave() {
        spawnWholeWave();
        defeat(enemiesInWave(waves.getCurrentWave()));
        waves.update(0);
    }

    @Test
    void constructor_nullArguments_throw() {
        assertThrows(NullPointerException.class, () -> new WaveManager(null, new Camera(), new PoolManager()));
        assertThrows(NullPointerException.class, () -> new WaveManager(spawned::add, new Camera(), null));
    }

    @Test
    void update_negativeDelta_throws() {
        assertThrows(IllegalArgumentException.class, () -> waves.update(-0.1));
    }

    @Nested
    class WaveStart {

        @Test
        void beforeWaveDelay_noWaveStarted() {
            waves.update(EnemyConfig.WAVE_DELAY / 2);

            assertEquals(0, waves.getCurrentWave());
            assertTrue(spawned.isEmpty());
        }

        @Test
        void afterWaveDelay_firstWaveStartsAndSpawnsImmediately() {
            waves.update(EnemyConfig.WAVE_DELAY);

            assertEquals(1, waves.getCurrentWave());
            assertEquals(1, spawned.size());
        }

        @Test
        void firstWave_hasSevenEnemies() {
            waves.update(EnemyConfig.WAVE_DELAY);
            for (int i = 0; i < 20; i++) {
                waves.update(EnemyConfig.SPAWN_INTERVAL);
            }

            assertEquals(7, spawned.size());
            assertEquals(1, waves.getCurrentWave());
        }

        @Test
        void nextWave_hasTwoMoreEnemies() {
            clearWave();
            int before = spawned.size();

            spawnWholeWave();
            for (int i = 0; i < 20; i++) {
                waves.update(EnemyConfig.SPAWN_INTERVAL);
            }

            assertEquals(2, waves.getCurrentWave());
            assertEquals(9, spawned.size() - before);
        }
    }

    @Nested
    class Spawning {

        @Test
        void spawn_respectsTwoSecondInterval() {
            waves.update(EnemyConfig.WAVE_DELAY);

            waves.update(1.5);
            assertEquals(1, spawned.size());

            waves.update(0.5);
            assertEquals(2, spawned.size());
        }

        @Test
        void spawnedEnemy_isActiveAndOutsideScreen() {
            spawnWholeWave();

            for (Enemy enemy : spawned) {
                Vector2D pos = enemy.getPosition();
                boolean outside = pos.getX() < 0 || pos.getX() > GameConfig.PANEL_WIDTH
                        || pos.getY() < 0 || pos.getY() > GameConfig.PANEL_HEIGHT;
                assertTrue(enemy.isActive());
                assertTrue(outside, "enemy should spawn outside the camera view: " + pos.getX() + ", " + pos.getY());
            }
        }
    }

    @Nested
    class BossWave {

        @Test
        void firstTwoWaves_areNotBossWaves() {
            clearWave();
            assertEquals(1, waves.getCurrentWave());
            assertFalse(waves.isBossWave());

            clearWave();
            assertEquals(2, waves.getCurrentWave());
            assertFalse(waves.isBossWave());
        }

        @Test
        void thirdWave_isBossWave_firstEnemyIsBoss() {
            clearWave();
            clearWave();
            int firstOfWave = spawned.size();

            waves.update(EnemyConfig.WAVE_DELAY);

            assertEquals(3, waves.getCurrentWave());
            assertTrue(waves.isBossWave());
            assertEquals(EnemyType.BOSS, spawned.get(firstOfWave).getEnemyType());
        }

        @Test
        void bossWave_hasOnlyOneBoss() {
            clearWave();
            clearWave();
            int firstOfWave = spawned.size();

            spawnWholeWave();

            long bosses = spawned.subList(firstOfWave, spawned.size()).stream()
                    .filter(enemy -> enemy.getEnemyType() == EnemyType.BOSS)
                    .count();
            assertEquals(1, bosses);
        }
    }

    @Nested
    class WaveEnd {

        @Test
        void wave_endsOnlyWhenAllDefeated() {
            spawnWholeWave();
            defeat(enemiesInWave(1) - 1);

            waves.update(EnemyConfig.WAVE_DELAY * 5);
            assertEquals(1, waves.getCurrentWave());

            defeat(1);
            waves.update(0);
            waves.update(EnemyConfig.WAVE_DELAY);
            assertEquals(2, waves.getCurrentWave());
        }

        @Test
        void wave_allDefeatedBeforeAllSpawned_doesNotEnd() {
            waves.update(EnemyConfig.WAVE_DELAY);
            defeat(1);

            waves.update(0);
            waves.update(EnemyConfig.WAVE_DELAY);

            assertEquals(1, waves.getCurrentWave());
        }

        @Test
        void enemyOnDefeated_neverExceedsSpawned() {
            waves.update(EnemyConfig.WAVE_DELAY);
            defeat(10);

            while (spawned.size() < enemiesInWave(1)) {
                waves.update(EnemyConfig.SPAWN_INTERVAL);
            }
            waves.update(EnemyConfig.WAVE_DELAY);

            assertEquals(1, waves.getCurrentWave(), "extra defeats must not finish the wave early");
        }

        @Test
        void enemyOnDefeated_beforeAnyWave_isIgnored() {
            defeat(5);

            waves.update(EnemyConfig.WAVE_DELAY);

            assertEquals(1, waves.getCurrentWave());
            assertEquals(1, spawned.size());
        }
    }
}
