package com.game.spaceshooter.application.model;

import com.game.spaceshooter.domain.entity.Player;
import com.game.spaceshooter.domain.port.GameEventListener;
import com.game.spaceshooter.domain.port.ProjectileFactory;
import com.game.spaceshooter.domain.projectiles.HomingBullet;
import com.game.spaceshooter.domain.projectiles.NormalBullet;
import com.game.spaceshooter.domain.projectiles.Plasma;
import com.game.spaceshooter.domain.projectiles.Projectile;
import com.game.spaceshooter.infrastructure.config.GameConfig;
import com.game.spaceshooter.infrastructure.utils.Vector2D;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CameraTest {

    private static final double EPS = 1e-9;
    private static final double CENTER_X = GameConfig.PANEL_WIDTH / 2.0;
    private static final double CENTER_Y = GameConfig.PANEL_HEIGHT / 2.0;
    private static final double HALF_DEAD_ZONE_W = GameConfig.DEAD_ZONE_WIDTH / 2.0;
    private static final double HALF_DEAD_ZONE_H = GameConfig.DEAD_ZONE_HEIGHT / 2.0;

    private static final ProjectileFactory FACTORY = type -> switch (type) {
        case PLASMA -> new Plasma();
        case NORMAL_BULLET -> new NormalBullet();
        case HOMING_BULLET -> new HomingBullet();
    };

    private static final GameEventListener NO_EVENTS = new GameEventListener() {
        @Override
        public void onPlayerDamaged() {
        }

        @Override
        public void onPlayerShot() {
        }

        @Override
        public void onPlayerBoosted() {
        }

        @Override
        public void onEnemyDefeated(Vector2D position) {
        }

        @Override
        public void onProjectileHit(Projectile projectile) {
        }
    };

    private Camera camera;

    @BeforeEach
    void setUp() {
        camera = new Camera();
    }

    private static Player playerAt(double x, double y) {
        return new Player(x, y, FACTORY, NO_EVENTS);
    }

    private static void assertVector(double x, double y, Vector2D actual) {
        assertEquals(x, actual.getX(), EPS, "x");
        assertEquals(y, actual.getY(), EPS, "y");
    }

    @Nested
    @DisplayName("Khởi tạo và getCameraPos")
    class Initial {

        @Test
        void newCamera_startsAtOrigin() {
            assertVector(0, 0, camera.getCameraPos());
        }

        @Test
        void getCameraPos_returnsCopy() {
            Vector2D pos = camera.getCameraPos();
            pos.copy(500, 500);

            assertVector(0, 0, camera.getCameraPos());
        }
    }

    @Nested
    @DisplayName("getScreenPos")
    class ScreenPos {

        @Test
        void getScreenPos_atOrigin_equalsWorldPos() {
            assertVector(120, -40, camera.getScreenPos(new Vector2D(120, -40)));
        }

        @Test
        void getScreenPos_subtractsCameraPosition() {
            camera.followPlayer(playerAt(CENTER_X + HALF_DEAD_ZONE_W + 100,
                  CENTER_Y + HALF_DEAD_ZONE_H + 20), 1.0);
            assertVector(100, 20, camera.getCameraPos());

            assertVector(400, 280, camera.getScreenPos(new Vector2D(500, 300)));
        }

        @Test
        void getScreenPos_doesNotModifyWorldPos() {
            camera.followPlayer(playerAt(CENTER_X + HALF_DEAD_ZONE_W + 100, CENTER_Y), 1.0);
            Vector2D world = new Vector2D(500, 300);

            camera.getScreenPos(world);

            assertVector(500, 300, world);
        }

        @Test
        void getScreenPos_nullWorldPos_throws() {
            assertThrows(NullPointerException.class, () -> camera.getScreenPos(null));
        }
    }

    @Nested
    @DisplayName("followPlayer – vùng chết (dead zone)")
    class DeadZone {

        @Test
        void followPlayer_insideDeadZone_doesNotMove() {
            camera.followPlayer(playerAt(CENTER_X + 100, CENTER_Y - 40), 1.0);

            assertVector(0, 0, camera.getCameraPos());
        }

        @Test
        void followPlayer_atScreenCenter_doesNotMove() {
            camera.followPlayer(playerAt(CENTER_X, CENTER_Y), 1.0);

            assertVector(0, 0, camera.getCameraPos());
        }

        @Test
        void followPlayer_exactlyOnDeadZoneEdge_doesNotMove() {
            camera.followPlayer(playerAt(CENTER_X + HALF_DEAD_ZONE_W, CENTER_Y - HALF_DEAD_ZONE_H), 1.0);

            assertVector(0, 0, camera.getCameraPos());
        }

        @Test
        void followPlayer_outsideOnlyHorizontally_movesOnlyX() {
            camera.followPlayer(playerAt(CENTER_X + HALF_DEAD_ZONE_W + 100, CENTER_Y + 30), 1.0);

            assertVector(100, 0, camera.getCameraPos());
        }

        @Test
        void followPlayer_outsideOnlyVertically_movesOnlyY() {
            camera.followPlayer(playerAt(CENTER_X - 120, CENTER_Y + HALF_DEAD_ZONE_H + 100), 1.0);

            assertVector(0, 100, camera.getCameraPos());
        }

        @Test
        void followPlayer_leftAndAbove_movesNegative() {
            camera.followPlayer(playerAt(CENTER_X - HALF_DEAD_ZONE_W - 100,
                  CENTER_Y - HALF_DEAD_ZONE_H - 60), 1.0);

            assertVector(-100, -60, camera.getCameraPos());
        }

        @Test
        void followPlayer_afterCatchingUp_playerOnEdge_stopsMoving() {
            Player player = playerAt(CENTER_X + HALF_DEAD_ZONE_W + 100, CENTER_Y);
            camera.followPlayer(player, 1.0);
            assertVector(100, 0, camera.getCameraPos());

            camera.followPlayer(player, 1.0);

            assertVector(100, 0, camera.getCameraPos());
        }
    }

    @Nested
    @DisplayName("followPlayer – nội suy (lerp)")
    class Lerp {

        @Test
        void followPlayer_movesFractionByCameraSpeedTimesDelta() {
            double deltaTime = 0.1;
            double lerp = GameConfig.CAMERA_SPEED * deltaTime;

            camera.followPlayer(playerAt(CENTER_X + HALF_DEAD_ZONE_W + 100, CENTER_Y), deltaTime);

            assertVector(100 * lerp, 0, camera.getCameraPos());
        }

        @Test
        void followPlayer_largeDelta_lerpCappedAtOne() {
            camera.followPlayer(playerAt(CENTER_X + HALF_DEAD_ZONE_W + 100, CENTER_Y), 10.0);

            assertVector(100, 0, camera.getCameraPos());
        }

        @Test
        void followPlayer_zeroDelta_doesNotMove() {
            camera.followPlayer(playerAt(CENTER_X + HALF_DEAD_ZONE_W + 100, CENTER_Y), 0);

            assertVector(0, 0, camera.getCameraPos());
        }

        @Test
        void followPlayer_manySmallSteps_convergesToDeadZoneEdge() {
            Player player = playerAt(CENTER_X + HALF_DEAD_ZONE_W + 100, CENTER_Y + HALF_DEAD_ZONE_H + 40);

            for (int i = 0; i < 600; i++) {
                camera.followPlayer(player, 1.0 / GameConfig.UPS);
            }

            Vector2D pos = camera.getCameraPos();
            assertEquals(100, pos.getX(), 1e-3);
            assertEquals(40, pos.getY(), 1e-3);
            assertTrue(pos.getX() <= 100 && pos.getY() <= 40, "camera must not overshoot");
        }

        @Test
        void followPlayer_doesNotMovePlayer() {
            Player player = playerAt(CENTER_X + HALF_DEAD_ZONE_W + 100, CENTER_Y);

            camera.followPlayer(player, 1.0);

            assertVector(CENTER_X + HALF_DEAD_ZONE_W + 100, CENTER_Y, player.getPosition());
        }

        @Test
        void followPlayer_nullPlayer_throws() {
            assertThrows(NullPointerException.class, () -> camera.followPlayer(null, 0.1));
        }
    }
}
