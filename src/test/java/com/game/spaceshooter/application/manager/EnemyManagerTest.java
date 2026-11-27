package com.game.spaceshooter.application.manager;

import com.game.spaceshooter.application.model.Camera;
import com.game.spaceshooter.application.port.ProjectileSpawner;
import com.game.spaceshooter.domain.entity.Enemy;
import com.game.spaceshooter.domain.entity.EnemyType;
import com.game.spaceshooter.domain.entity.Player;
import com.game.spaceshooter.domain.port.GameEventListener;
import com.game.spaceshooter.domain.projectiles.Projectile;
import com.game.spaceshooter.infrastructure.config.EnemyConfig;
import com.game.spaceshooter.infrastructure.config.PlayerConfig;
import com.game.spaceshooter.infrastructure.pool.PoolManager;
import com.game.spaceshooter.infrastructure.utils.Vector2D;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EnemyManagerTest {

    /** Small step that never reaches WAVE_DELAY, so the wave manager does not spawn extra enemies. */
    private static final double SMALL_STEP = 0.01;

    private static class RecordingPool extends PoolManager {
        private final List<Enemy> releasedEnemies = new ArrayList<>();

        @Override
        public void release(Enemy enemy) {
            releasedEnemies.add(enemy);
            super.release(enemy);
        }
    }

    private static class RecordingSpawner implements ProjectileSpawner {
        private final List<Projectile> projectiles = new ArrayList<>();

        @Override
        public void addActiveProjectile(Projectile projectile) {
            projectiles.add(projectile);
        }
    }

    private static class RecordingListener implements GameEventListener {
        private int playerDamaged;
        private final List<Vector2D> defeatedAt = new ArrayList<>();

        @Override
        public void onPlayerDamaged() { playerDamaged++; }

        @Override
        public void onPlayerShot() {}

        @Override
        public void onPlayerBoosted() {}

        @Override
        public void onEnemyDefeated(Vector2D position) { defeatedAt.add(position); }

        @Override
        public void onProjectileHit(Projectile projectile) {}
    }

    private final RecordingPool pool = new RecordingPool();
    private final RecordingSpawner spawner = new RecordingSpawner();
    private final RecordingListener listener = new RecordingListener();
    private final Camera camera = new Camera();
    private final EnemyRoster roster = new EnemyRoster();
    private final Player player = new Player(0, 0, pool, listener);
    private final EnemyManager manager = new EnemyManager(player, camera, roster, spawner, listener, pool);

    private Enemy addEnemy(EnemyType type, double x, double y) {
        Enemy enemy = pool.acquire(type);
        enemy.setPosition(x, y);
        manager.addActiveEnemy(enemy);
        return enemy;
    }

    @Test
    void constructor_nullArguments_throw() {
        assertThrows(NullPointerException.class,
                () -> new EnemyManager(player, camera, null, spawner, listener, pool));
        assertThrows(NullPointerException.class,
                () -> new EnemyManager(player, camera, roster, null, listener, pool));
        assertThrows(NullPointerException.class,
                () -> new EnemyManager(player, camera, roster, spawner, null, pool));
        assertThrows(NullPointerException.class,
                () -> new EnemyManager(player, camera, roster, spawner, listener, null));
    }

    @Nested
    class Roster {

        @Test
        void addActiveEnemy_addsToRosterAndTargetsPlayer() {
            Enemy drone = addEnemy(EnemyType.DRONE, 1000, 0);

            assertEquals(List.of(drone), manager.snapshot());

            manager.update(0.5);

            assertTrue(drone.getPosition().getX() < 1000, "drone should move toward the player");
        }

        @Test
        void update_afterWaveDelay_spawnsWaveEnemyIntoRoster() {
            manager.update(EnemyConfig.WAVE_DELAY);

            assertEquals(1, roster.snapshot().size());
        }

        @Test
        void update_setsEnemyBoundToScreenPosition() {
            Enemy drone = addEnemy(EnemyType.DRONE, 1000, 500);

            manager.update(SMALL_STEP);

            Vector2D screen = camera.getScreenPos(drone.getPosition());
            assertEquals((int) screen.getX() - drone.getWidth() / 2, drone.getBound().x);
            assertEquals((int) screen.getY() - drone.getHeight() / 2, drone.getBound().y);
        }

        @Test
        void aliveEnemyFarFromPlayer_staysActive() {
            Enemy drone = addEnemy(EnemyType.DRONE, 1000, 1000);

            manager.update(SMALL_STEP);

            assertTrue(drone.isActive());
            assertEquals(List.of(drone), manager.snapshot());
            assertTrue(pool.releasedEnemies.isEmpty());
            assertEquals(PlayerConfig.MAX_HEALTH, player.getHealth());
        }
    }

    @Nested
    class CollisionAndDefeat {

        @Test
        void collisionWithPlayer_killsEnemyDamagesPlayerAddsScore() {
            Enemy drone = addEnemy(EnemyType.DRONE, 10, 0);

            manager.update(SMALL_STEP);

            assertEquals(PlayerConfig.MAX_HEALTH - EnemyConfig.DRONE_DEFAULT_DAMAGE, player.getHealth());
            assertEquals(1, listener.playerDamaged);
            assertEquals(EnemyConfig.DRONE_SCORE, player.getScore());
            assertEquals(1, listener.defeatedAt.size());
            assertEquals(List.of(drone), pool.releasedEnemies);
            assertTrue(manager.snapshot().isEmpty());
        }

        @Test
        void deadEnemy_releasedToPool() {
            Enemy drone = addEnemy(EnemyType.DRONE, 1000, 1000);
            drone.takeDamage(drone.getMaxHealth());

            manager.update(SMALL_STEP);

            assertEquals(List.of(drone), pool.releasedEnemies);
            assertTrue(manager.snapshot().isEmpty());
            assertFalse(drone.isActive());
            assertEquals(drone.getMaxHealth(), drone.getHealth(), "released enemy is reset for reuse");
        }

        @Test
        void deadEnemy_notifiesListenerAndAddsScoreWithoutDamagingPlayer() {
            Enemy drone = addEnemy(EnemyType.DRONE, 1000, 1000);
            drone.takeDamage(drone.getMaxHealth());

            manager.update(SMALL_STEP);

            assertEquals(1, listener.defeatedAt.size());
            assertTrue(listener.defeatedAt.get(0).getX() > 900, "event carries the enemy position");
            assertEquals(EnemyConfig.DRONE_SCORE, player.getScore());
            assertEquals(PlayerConfig.MAX_HEALTH, player.getHealth());
            assertEquals(0, listener.playerDamaged);
        }

        @Test
        void inactiveEnemy_releasedWithoutScore() {
            Enemy drone = addEnemy(EnemyType.DRONE, 1000, 1000);
            drone.setActive(false);

            manager.update(SMALL_STEP);

            assertEquals(List.of(drone), pool.releasedEnemies);
            assertTrue(manager.snapshot().isEmpty());
            assertEquals(0, player.getScore());
            assertTrue(listener.defeatedAt.isEmpty());
        }
    }

    @Nested
    class Shooting {

        @Test
        void enemyShot_addedToProjectileSpawner() {
            addEnemy(EnemyType.ORBITING_ENEMY, 1000, 0);

            manager.update(0.2);

            assertEquals(1, spawner.projectiles.size());
            Projectile projectile = spawner.projectiles.get(0);
            assertEquals(Projectile.Owner.ENEMY, projectile.getOwner());
            assertTrue(projectile.isActive());
        }

        @Test
        void enemyShot_boundSetToScreenPosition() {
            addEnemy(EnemyType.ORBITING_ENEMY, 1000, 0);

            manager.update(0.2);

            Projectile projectile = spawner.projectiles.get(0);
            Vector2D screen = camera.getScreenPos(projectile.getPosition());
            assertEquals((int) screen.getX() - projectile.getWidth() / 2, projectile.getBound().x);
            assertEquals((int) screen.getY() - projectile.getHeight() / 2, projectile.getBound().y);
        }

        @Test
        void weaponOnCooldown_noProjectile() {
            addEnemy(EnemyType.ORBITING_ENEMY, 1000, 0);

            manager.update(SMALL_STEP);

            assertTrue(spawner.projectiles.isEmpty());
        }

        @Test
        void unarmedEnemy_neverShoots() {
            addEnemy(EnemyType.DRONE, 1000, 1000);

            manager.update(1.0);

            assertTrue(spawner.projectiles.isEmpty());
        }
    }
}
