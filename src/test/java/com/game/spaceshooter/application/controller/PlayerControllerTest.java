package com.game.spaceshooter.application.controller;

import com.game.spaceshooter.application.model.Camera;
import com.game.spaceshooter.application.port.ProjectileSpawner;
import com.game.spaceshooter.domain.entity.Player;
import com.game.spaceshooter.domain.entity.PlayerState;
import com.game.spaceshooter.domain.port.GameEventListener;
import com.game.spaceshooter.domain.port.ProjectileFactory;
import com.game.spaceshooter.domain.projectiles.HomingBullet;
import com.game.spaceshooter.domain.projectiles.NormalBullet;
import com.game.spaceshooter.domain.projectiles.Plasma;
import com.game.spaceshooter.domain.projectiles.Projectile;
import com.game.spaceshooter.domain.projectiles.ProjectileType;
import com.game.spaceshooter.infrastructure.config.GameConfig;
import com.game.spaceshooter.infrastructure.config.PlayerConfig;
import com.game.spaceshooter.infrastructure.utils.Vector2D;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PlayerControllerTest {

    private static final double EPS = 1e-9;
    private static final double PLASMA_FIRE_INTERVAL = 1.0 / 7;
    private static final double PLAYER_X = 100;
    private static final double PLAYER_Y = 100;

    private static final ProjectileFactory FACTORY = type -> switch (type) {
        case PLASMA -> new Plasma();
        case NORMAL_BULLET -> new NormalBullet();
        case HOMING_BULLET -> new HomingBullet();
    };

    private static class RecordingSpawner implements ProjectileSpawner {
        private final List<Projectile> added = new ArrayList<>();

        @Override
        public void addActiveProjectile(Projectile projectile) {
            added.add(projectile);
        }
    }

    private static class RecordingListener implements GameEventListener {
        private int playerDamaged;
        private int playerShot;
        private int playerBoosted;

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
        }
    }

    private Camera camera;
    private Player player;
    private RecordingSpawner spawner;
    private RecordingListener feedback;
    private PlayerController controller;

    @BeforeEach
    void setUp() {
        camera = new Camera();
        player = new Player(PLAYER_X, PLAYER_Y, FACTORY, new RecordingListener());
        spawner = new RecordingSpawner();
        feedback = new RecordingListener();
        controller = new PlayerController(camera, player, spawner, feedback);
    }

    /** Không di chuyển, không bắn, không boost, ngắm vào điểm (aimX, aimY) trên màn hình. */
    private void aimOnly(double aimX, double aimY) {
        controller.update(0, 0, aimX, aimY, false, false);
    }

    private void aimRight() {
        aimOnly(PLAYER_X + 100, PLAYER_Y);
    }

    private void aimUp() {
        aimOnly(PLAYER_X, PLAYER_Y - 100);
    }

    private void waitForWeaponCooldown() {
        player.update(PLASMA_FIRE_INTERVAL + 0.01);
    }

    @Nested
    @DisplayName("Constructor")
    class Constructor {

        @Test
        void constructor_nullSpawner_throws() {
            assertThrows(NullPointerException.class,
                  () -> new PlayerController(camera, player, null, feedback));
        }

        @Test
        void constructor_nullFeedback_throws() {
            assertThrows(NullPointerException.class,
                  () -> new PlayerController(camera, player, spawner, null));
        }
    }

    @Nested
    @DisplayName("Ngắm (aim) và bound")
    class AimAndBound {

        @Test
        void aimRight_setsViewAngleHalfPi() {
            aimRight();

            assertEquals(Math.PI / 2, player.getViewAngle(), EPS);
        }

        @Test
        void aimUp_setsViewAngleZero() {
            aimUp();

            assertEquals(0, player.getViewAngle(), EPS);
        }

        @Test
        void aimDown_setsViewAnglePi() {
            aimOnly(PLAYER_X, PLAYER_Y + 100);

            assertEquals(Math.PI, player.getViewAngle(), EPS);
        }

        @Test
        void update_setsPlayerBoundAtScreenPosition() {
            aimRight();

            Rectangle bound = player.getBound();
            assertEquals((int) PLAYER_X - PlayerConfig.DEFAULT_WIDTH / 2, bound.x);
            assertEquals((int) PLAYER_Y - PlayerConfig.DEFAULT_HEIGHT / 2, bound.y);
        }

        @Test
        void update_withMovedCamera_usesScreenCoordinates() {
            double worldX = GameConfig.PANEL_WIDTH / 2.0 + GameConfig.DEAD_ZONE_WIDTH / 2.0 + 100;
            double worldY = GameConfig.PANEL_HEIGHT / 2.0;
            Player farPlayer = new Player(worldX, worldY, FACTORY, new RecordingListener());
            camera.followPlayer(farPlayer, 1.0);
            PlayerController farController = new PlayerController(camera, farPlayer, spawner, feedback);
            double screenX = worldX - 100;

            farController.update(0, 0, screenX, worldY - 100, false, false);

            assertEquals(0, farPlayer.getViewAngle(), EPS, "aim is relative to screen position, not world");
            Rectangle bound = farPlayer.getBound();
            assertEquals((int) screenX - PlayerConfig.DEFAULT_WIDTH / 2, bound.x);
            assertEquals((int) worldY - PlayerConfig.DEFAULT_HEIGHT / 2, bound.y);
            assertEquals(worldX, farPlayer.getPosition().getX(), EPS, "world position is unchanged");
        }
    }

    @Nested
    @DisplayName("Di chuyển ở trạng thái NORMAL")
    class NormalMovement {

        @Test
        void moving_setsDirectionAndAccelerates() {
            controller.update(1, 0, PLAYER_X, PLAYER_Y - 100, false, false);

            assertEquals(new Vector2D(1, 0), player.getDirection());
            assertEquals(PlayerConfig.ACCELERATION, player.getSpeed(), EPS);
            assertEquals(PlayerState.NORMAL, player.getPlayerState());
        }

        @Test
        void moving_diagonal_keepsRawInputDirection() {
            controller.update(-1, 1, PLAYER_X, PLAYER_Y - 100, false, false);

            assertEquals(new Vector2D(-1, 1), player.getDirection());
        }

        @Test
        void moving_accelerationAccumulates() {
            for (int i = 0; i < 3; i++) {
                controller.update(0, -1, PLAYER_X, PLAYER_Y - 100, false, false);
            }

            assertEquals(3 * PlayerConfig.ACCELERATION, player.getSpeed(), EPS);
        }

        @Test
        void moving_speedCapsAtMaxSpeed() {
            for (int i = 0; i < 1000; i++) {
                controller.update(1, 0, PLAYER_X, PLAYER_Y - 100, false, false);
            }

            assertEquals(PlayerConfig.MAX_SPEED, player.getSpeed(), EPS);
        }

        @Test
        void noInput_appliesDrag() {
            for (int i = 0; i < 3; i++) {
                controller.update(1, 0, PLAYER_X, PLAYER_Y - 100, false, false);
            }

            aimUp();

            assertEquals(3 * PlayerConfig.ACCELERATION - PlayerConfig.DRAG, player.getSpeed(), EPS);
            assertEquals(new Vector2D(1, 0), player.getDirection(), "drag keeps the last direction");
        }

        @Test
        void noInput_dragStopsAtZero() {
            controller.update(1, 0, PLAYER_X, PLAYER_Y - 100, false, false);

            for (int i = 0; i < 10; i++) {
                aimUp();
            }

            assertEquals(0, player.getSpeed(), EPS);
        }

        @Test
        void aimingOnly_doesNotChangeMoveDirection() {
            controller.update(1, 0, PLAYER_X, PLAYER_Y - 100, false, false);

            aimOnly(PLAYER_X, PLAYER_Y + 100);

            assertEquals(new Vector2D(1, 0), player.getDirection());
        }
    }

    @Nested
    @DisplayName("Bắn")
    class Shooting {

        @Test
        void shooting_addsProjectileAndNotifies() {
            waitForWeaponCooldown();

            controller.update(0, 0, PLAYER_X + 100, PLAYER_Y, true, false);

            assertEquals(1, spawner.added.size());
            assertEquals(1, feedback.playerShot);
            Projectile projectile = spawner.added.getFirst();
            assertEquals(ProjectileType.PLASMA, projectile.getProjectileType());
            assertEquals(Projectile.Owner.PLAYER, projectile.getOwner());
        }

        @Test
        void shooting_projectileHeadsTowardAim() {
            waitForWeaponCooldown();

            controller.update(0, 0, PLAYER_X + 100, PLAYER_Y, true, false);

            Projectile projectile = spawner.added.getFirst();
            assertEquals(new Vector2D(PLAYER_X + PlayerConfig.SPRITE_SIZE / 2.0, PLAYER_Y), projectile.getPosition());
            assertEquals(new Vector2D(100, 0), projectile.getDirection());
            assertEquals(Math.PI / 2, projectile.getCurrentAngle(), EPS);
        }

        @Test
        void shooting_setsProjectileBoundAtScreenPosition() {
            waitForWeaponCooldown();

            controller.update(0, 0, PLAYER_X + 100, PLAYER_Y, true, false);

            Projectile projectile = spawner.added.getFirst();
            Vector2D pos = projectile.getPosition();
            Rectangle bound = projectile.getBound();
            assertEquals((int) pos.getX() - projectile.getWidth() / 2, bound.x);
            assertEquals((int) pos.getY() - projectile.getHeight() / 2, bound.y);
        }

        @Test
        void shooting_duringWeaponCooldown_addsNothing() {
            controller.update(0, 0, PLAYER_X + 100, PLAYER_Y, true, false);

            assertTrue(spawner.added.isEmpty());
            assertEquals(0, feedback.playerShot);
        }

        @Test
        void shooting_twiceInARow_onlyFirstSucceeds() {
            waitForWeaponCooldown();

            controller.update(0, 0, PLAYER_X + 100, PLAYER_Y, true, false);
            controller.update(0, 0, PLAYER_X + 100, PLAYER_Y, true, false);

            assertEquals(1, spawner.added.size());
            assertEquals(1, feedback.playerShot);
        }

        @Test
        void notShooting_addsNothing() {
            waitForWeaponCooldown();

            aimRight();

            assertTrue(spawner.added.isEmpty());
            assertEquals(0, feedback.playerShot);
        }

        @Test
        void shooting_whileMoving_bothApply() {
            waitForWeaponCooldown();

            controller.update(0, 1, PLAYER_X + 100, PLAYER_Y, true, false);

            assertEquals(1, spawner.added.size());
            assertEquals(PlayerConfig.ACCELERATION, player.getSpeed(), EPS);
        }
    }

    @Nested
    @DisplayName("Boost")
    class Boost {

        @Test
        void boostRequested_startsBoost() {
            controller.update(0, 0, PLAYER_X + 100, PLAYER_Y, false, true);

            assertEquals(PlayerState.BOOSTING, player.getPlayerState());
            assertEquals(PlayerConfig.BOOST_COOLDOWN, player.getBoostCooldown(), EPS);
            assertEquals(1, feedback.playerBoosted);
        }

        @Test
        void boostRequested_targetsViewAngle() {
            controller.update(0, 0, PLAYER_X + 100, PLAYER_Y, false, true);

            player.update(0.1);

            double expected = (Math.PI / 2) * PlayerConfig.ROTATION_SPEED * 0.1;
            assertEquals(expected, player.getCurrentAngle(), EPS, "turns toward the aim at ROTATION_SPEED");
        }

        @Test
        void boostRequested_duringCooldown_staysNormalAndMoves() {
            controller.update(0, 0, PLAYER_X, PLAYER_Y - 100, false, true);
            player.update(PlayerConfig.BOOST_DURATION + PlayerConfig.BOOST_DECAY + 0.1);
            assertEquals(PlayerState.NORMAL, player.getPlayerState());
            double speedBefore = player.getSpeed();

            controller.update(1, 0, PLAYER_X, PLAYER_Y - 100, false, true);

            assertEquals(PlayerState.NORMAL, player.getPlayerState());
            assertEquals(1, feedback.playerBoosted);
            assertEquals(new Vector2D(1, 0), player.getDirection());
            assertEquals(Math.min(PlayerConfig.MAX_SPEED, speedBefore + PlayerConfig.ACCELERATION),
                  player.getSpeed(), EPS);
        }

        @Test
        void whileBoosting_ignoresMovementAndBoostInput() {
            controller.update(0, 0, PLAYER_X, PLAYER_Y - 100, false, true);
            Vector2D directionBefore = player.getDirection();
            double speedBefore = player.getSpeed();

            controller.update(1, 1, PLAYER_X, PLAYER_Y - 100, false, true);

            assertEquals(PlayerState.BOOSTING, player.getPlayerState());
            assertEquals(1, feedback.playerBoosted);
            assertEquals(directionBefore, player.getDirection());
            assertEquals(speedBefore, player.getSpeed(), EPS);
        }

        @Test
        void whileBoosting_targetFollowsViewAngle() {
            controller.update(0, 0, PLAYER_X + 100, PLAYER_Y, false, true);

            aimUp();
            player.update(0.1);

            assertEquals(0, player.getCurrentAngle(), EPS, "target changed to the new aim (angle 0)");
            assertEquals(0, player.getViewAngle(), EPS);
        }

        @Test
        void boostDecay_withMovement_targetsMovementAngle() {
            controller.update(0, 0, PLAYER_X, PLAYER_Y - 100, false, true);
            player.update(PlayerConfig.BOOST_DURATION);
            assertEquals(PlayerState.BOOST_DECAY, player.getPlayerState());
            double speedBefore = player.getSpeed();

            controller.update(1, 0, PLAYER_X, PLAYER_Y - 100, false, false);

            assertEquals(PlayerState.BOOST_DECAY, player.getPlayerState(), "controller must not reset to NORMAL");
            assertEquals(speedBefore, player.getSpeed(), EPS, "no acceleration during decay");
            double before = player.getCurrentAngle();
            player.update(0.1);
            double expected = before + (Math.PI / 2 - before) * PlayerConfig.ROTATION_SPEED * 0.1;
            assertEquals(expected, player.getCurrentAngle(), EPS);
        }

        @Test
        void boostDecay_withoutMovement_keepsTargetAndSpeed() {
            controller.update(0, 0, PLAYER_X, PLAYER_Y - 100, false, true);
            player.update(PlayerConfig.BOOST_DURATION);
            double speedBefore = player.getSpeed();

            aimRight();

            assertEquals(PlayerState.BOOST_DECAY, player.getPlayerState());
            assertEquals(speedBefore, player.getSpeed(), EPS, "no drag during decay");
            player.update(0.1);
            assertEquals(0, player.getCurrentAngle(), EPS, "target stays at the boost angle (0)");
        }

        @Test
        void afterDecay_controllerResumesNormalMovement() {
            controller.update(0, 0, PLAYER_X, PLAYER_Y - 100, false, true);
            player.update(PlayerConfig.BOOST_DURATION);
            player.update(PlayerConfig.BOOST_DECAY);
            assertEquals(PlayerState.NORMAL, player.getPlayerState());

            aimUp();

            assertEquals(PlayerConfig.MAX_SPEED - PlayerConfig.DRAG, player.getSpeed(), EPS);
        }
    }
}
