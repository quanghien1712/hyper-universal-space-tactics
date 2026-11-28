package com.game.spaceshooter.application.core;

import com.game.spaceshooter.application.manager.EffectManager;
import com.game.spaceshooter.application.model.Camera;
import com.game.spaceshooter.domain.effect.Effect;
import com.game.spaceshooter.domain.effect.EffectType;
import com.game.spaceshooter.domain.entity.Enemy;
import com.game.spaceshooter.domain.entity.Player;
import com.game.spaceshooter.domain.entity.PlayerState;
import com.game.spaceshooter.domain.port.GameEventListener;
import com.game.spaceshooter.domain.projectiles.Plasma;
import com.game.spaceshooter.domain.projectiles.Projectile;
import com.game.spaceshooter.domain.projectiles.ProjectileType;
import com.game.spaceshooter.infrastructure.config.EnemyConfig;
import com.game.spaceshooter.infrastructure.config.GameConfig;
import com.game.spaceshooter.infrastructure.config.PlayerConfig;
import com.game.spaceshooter.infrastructure.pool.PoolManager;
import com.game.spaceshooter.infrastructure.utils.Vector2D;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GameplaySessionTest {

    private static final double EPS = 1e-9;
    private static final double CENTER_X = GameConfig.PANEL_WIDTH / 2.0;
    private static final double CENTER_Y = GameConfig.PANEL_HEIGHT / 2.0;
    private static final double PLASMA_FIRE_INTERVAL = 1.0 / 7;
    /** ExplosionEffect: 16 frames, each frame lasts 3 updates. */
    private static final int EXPLOSION_UPDATES = 16 * 3;

    private static class RecordingListener implements GameEventListener {
        private int playerDamaged;
        private int playerShot;
        private int playerBoosted;
        private int projectileHit;

        @Override
        public void onPlayerDamaged() {
            playerDamaged++;
        }

        @Override
        public void onPlayerShot() {
            playerShot++;
        }

        @Override
        public void onPlayerBoosted() {
            playerBoosted++;
        }

        @Override
        public void onEnemyDefeated(Vector2D position) {
        }

        @Override
        public void onProjectileHit(Projectile projectile) {
            projectileHit++;
        }
    }

    private PoolManager pool;
    private Camera camera;
    private RecordingListener feedback;
    private EffectManager effects;
    private GameplaySession session;

    @BeforeEach
    void setUp() {
        pool = new PoolManager();
        camera = new Camera();
        feedback = new RecordingListener();
        effects = new EffectManager(pool);
        session = new GameplaySession(pool, camera, feedback, effects);
    }

    /** Một lượt update không có input: không di chuyển, không bắn, không boost, ngắm lên trên. */
    private void idle(double deltaTime) {
        Vector2D pos = camera.getScreenPos(session.getPlayer().getPosition());
        session.update(deltaTime, 0, 0, pos.getX(), pos.getY() - 100, false, false);
    }

    @Nested
    @DisplayName("Constructor")
    class Constructor {

        @Test
        void constructor_nullView_throws() {
            assertThrows(NullPointerException.class,
                  () -> new GameplaySession(pool, null, feedback, effects));
        }

        @Test
        void constructor_nullEffects_throws() {
            assertThrows(NullPointerException.class,
                  () -> new GameplaySession(pool, camera, feedback, null));
        }

        @Test
        void constructor_nullPool_throws() {
            assertThrows(IllegalArgumentException.class,
                  () -> new GameplaySession(null, camera, feedback, effects));
        }

        @Test
        void constructor_nullFeedback_throws() {
            assertThrows(IllegalArgumentException.class,
                  () -> new GameplaySession(pool, camera, null, effects));
        }

        @Test
        void newSession_playerAtPanelCenterWithFullHealth() {
            Player player = session.getPlayer();

            assertEquals(new Vector2D(CENTER_X, CENTER_Y), player.getPosition());
            assertEquals(PlayerConfig.MAX_HEALTH, player.getHealth());
            assertEquals(0, player.getScore());
            assertEquals(PlayerState.NORMAL, player.getPlayerState());
        }

        @Test
        void getPlayer_returnsSameInstance() {
            assertSame(session.getPlayer(), session.getPlayer());
        }

        @Test
        void newSession_hasNoEnemiesProjectilesOrEffects() {
            assertTrue(session.getEnemies().isEmpty());
            assertTrue(session.getProjectiles().isEmpty());
            assertTrue(session.getEffects().isEmpty());
        }

        @Test
        void snapshots_returnCopies() {
            effects.spawn(EffectType.DEATH, new Vector2D(100, 100));

            session.getEffects().clear();
            session.getEnemies().clear();
            session.getProjectiles().clear();

            assertEquals(1, session.getEffects().size());
        }
    }

    @Nested
    @DisplayName("Người chơi")
    class PlayerFlow {

        @Test
        void update_movementInput_acceleratesAndMovesPlayerInSameUpdate() {
            session.update(0.5, 1, 0, CENTER_X, CENTER_Y - 100, false, false);

            Player player = session.getPlayer();
            assertEquals(PlayerConfig.ACCELERATION, player.getSpeed(), EPS);
            assertEquals(CENTER_X + PlayerConfig.ACCELERATION * 0.5, player.getPosition().getX(), EPS);
            assertEquals(CENTER_Y, player.getPosition().getY(), EPS);
        }

        @Test
        void update_aim_setsPlayerViewAngle() {
            session.update(0.01, 0, 0, CENTER_X + 100, CENTER_Y, false, false);

            assertEquals(Math.PI / 2, session.getPlayer().getViewAngle(), EPS);
        }

        @Test
        void update_boostRequested_startsBoostAndAppliesBoostSpeed() {
            session.update(0, 0, 0, CENTER_X, CENTER_Y - 100, false, true);

            Player player = session.getPlayer();
            assertEquals(PlayerState.BOOSTING, player.getPlayerState());
            assertEquals(PlayerConfig.BOOST_SPEED, player.getSpeed(), EPS);
            assertEquals(1, feedback.playerBoosted);
        }

        @Test
        void update_cameraFollowsPlayer() {
            session.getPlayer().setPosition(CENTER_X + GameConfig.DEAD_ZONE_WIDTH / 2.0 + 100, CENTER_Y);

            idle(1.0);

            assertEquals(new Vector2D(100, 0), camera.getCameraPos());
        }

        @Test
        void update_insideDeadZone_cameraStays() {
            idle(1.0);

            assertEquals(new Vector2D(0, 0), camera.getCameraPos());
        }
    }

    @Nested
    @DisplayName("Bắn")
    class Shooting {

        @Test
        void shooting_beforeWeaponReady_noProjectile() {
            session.update(0.01, 0, 0, CENTER_X + 100, CENTER_Y, true, false);

            assertTrue(session.getProjectiles().isEmpty());
            assertEquals(0, feedback.playerShot);
        }

        @Test
        void shooting_afterWeaponReady_addsPlayerProjectile() {
            idle(PLASMA_FIRE_INTERVAL + 0.01);

            session.update(0.01, 0, 0, CENTER_X + 100, CENTER_Y, true, false);

            List<Projectile> projectiles = session.getProjectiles();
            assertEquals(1, projectiles.size());
            Projectile plasma = projectiles.getFirst();
            assertEquals(ProjectileType.PLASMA, plasma.getProjectileType());
            assertEquals(Projectile.Owner.PLAYER, plasma.getOwner());
            assertEquals(1, feedback.playerShot);
        }

        @Test
        void shooting_projectileMovesInSameUpdate() {
            idle(PLASMA_FIRE_INTERVAL + 0.01);

            session.update(0.01, 0, 0, CENTER_X + 100, CENTER_Y, true, false);

            Projectile plasma = session.getProjectiles().getFirst();
            double spawnX = CENTER_X + PlayerConfig.SPRITE_SIZE / 2.0;
            assertEquals(spawnX + plasma.getSpeed() * 0.01, plasma.getPosition().getX(), 1e-6);
            assertEquals(CENTER_Y, plasma.getPosition().getY(), 1e-6);
        }

        @Test
        void projectile_leavingScreen_isRemoved() {
            idle(PLASMA_FIRE_INTERVAL + 0.01);
            session.update(0.01, 0, 0, CENTER_X + 100, CENTER_Y, true, false);
            assertEquals(1, session.getProjectiles().size());

            for (int i = 0; i < 18; i++) {
                idle(0.1);
            }

            assertTrue(session.getProjectiles().isEmpty());
        }
    }

    @Nested
    @DisplayName("Kẻ địch")
    class Enemies {

        @Test
        void beforeWaveDelay_noEnemySpawns() {
            for (int i = 0; i < 3; i++) {
                idle(EnemyConfig.WAVE_DELAY / 4);
            }

            assertTrue(session.getEnemies().isEmpty());
        }

        @Test
        void afterWaveDelay_firstEnemySpawnsOutsideScreen() {
            for (int i = 0; i < 4; i++) {
                idle(EnemyConfig.WAVE_DELAY / 4);
            }

            List<Enemy> enemies = session.getEnemies();
            assertEquals(1, enemies.size());
            Vector2D pos = enemies.getFirst().getPosition();
            boolean outside = pos.getX() < 0 || pos.getX() > GameConfig.PANEL_WIDTH
                  || pos.getY() < 0 || pos.getY() > GameConfig.PANEL_HEIGHT;
            assertTrue(outside, "enemy spawns outside the visible screen");
        }

        @Test
        void playerBullet_hitsSpawnedEnemy() {
            for (int i = 0; i < 4; i++) {
                idle(EnemyConfig.WAVE_DELAY / 4);
            }
            Enemy enemy = session.getEnemies().getFirst();
            enemy.setPosition(CENTER_X + 110, CENTER_Y);
            int healthBefore = enemy.getHealth();

            session.update(0.05, 0, 0, CENTER_X + 110, CENTER_Y, true, false);

            assertEquals(1, feedback.playerShot);
            assertEquals(healthBefore - new Plasma().getDamage(), enemy.getHealth(),
                  "enemy bound is updated before projectiles are checked in the same update");
            assertTrue(feedback.projectileHit >= 1);
            assertTrue(session.getProjectiles().stream()
                  .noneMatch(p -> p.getOwner() == Projectile.Owner.PLAYER), "the plasma is released after the hit");
        }
    }

    @Nested
    @DisplayName("Hiệu ứng")
    class Effects {

        @Test
        void update_advancesEffects() {
            effects.spawn(EffectType.BULLET_EXPLOSION, new Vector2D(100, 100));
            Effect effect = session.getEffects().getFirst();

            idle(0);
            idle(0);
            idle(0);

            assertEquals(1, effect.getAniIndex());
        }

        @Test
        void finishedEffect_isRemoved() {
            effects.spawn(EffectType.BULLET_EXPLOSION, new Vector2D(100, 100));

            for (int i = 0; i < EXPLOSION_UPDATES - 1; i++) {
                idle(0);
            }
            assertEquals(1, session.getEffects().size());

            idle(0);

            assertTrue(session.getEffects().isEmpty());
        }
    }
}
