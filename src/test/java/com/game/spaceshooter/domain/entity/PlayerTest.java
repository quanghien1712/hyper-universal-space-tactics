package com.game.spaceshooter.domain.entity;

import com.game.spaceshooter.domain.port.GameEventListener;
import com.game.spaceshooter.domain.port.ProjectileFactory;
import com.game.spaceshooter.domain.projectiles.HomingBullet;
import com.game.spaceshooter.domain.projectiles.NormalBullet;
import com.game.spaceshooter.domain.projectiles.Plasma;
import com.game.spaceshooter.domain.projectiles.Projectile;
import com.game.spaceshooter.domain.projectiles.ProjectileType;
import com.game.spaceshooter.infrastructure.config.PlayerConfig;
import com.game.spaceshooter.infrastructure.utils.Vector2D;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PlayerTest {

    private static final double EPS = 1e-9;
    private static final double PLASMA_FIRE_INTERVAL = 1.0 / 7;

    private static class FakeProjectileFactory implements ProjectileFactory {
        private final List<ProjectileType> requested = new ArrayList<>();

        @Override
        public Projectile acquire(ProjectileType type) {
            requested.add(type);
            return switch (type) {
                case PLASMA -> new Plasma();
                case NORMAL_BULLET -> new NormalBullet();
                case HOMING_BULLET -> new HomingBullet();
            };
        }
    }

    private static class RecordingListener implements GameEventListener {
        private int playerDamaged;
        private int playerShot;
        private int playerBoosted;
        private int enemyDefeated;
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
            enemyDefeated++;
        }

        @Override
        public void onProjectileHit(Projectile projectile) {
            projectileHit++;
        }
    }

    private FakeProjectileFactory factory;
    private RecordingListener events;
    private Player player;

    @BeforeEach
    void setUp() {
        factory = new FakeProjectileFactory();
        events = new RecordingListener();
        player = new Player(100, 200, factory, events);
    }

    private static void assertVector(double expectedX, double expectedY, Vector2D actual) {
        assertEquals(expectedX, actual.getX(), EPS, "x");
        assertEquals(expectedY, actual.getY(), EPS, "y");
    }

    @Nested
    @DisplayName("Constructor")
    class ConstructorTests {

        @Test
        void newPlayer_hasDefaultStats() {
            assertVector(100, 200, player.getPosition());
            assertEquals(PlayerConfig.DEFAULT_WIDTH, player.getWidth());
            assertEquals(PlayerConfig.DEFAULT_HEIGHT, player.getHeight());
            assertEquals(PlayerConfig.MAX_HEALTH, player.getHealth());
            assertEquals(0, player.getScore());
            assertEquals(0.0, player.getSpeed(), EPS);
            assertEquals(0.0, player.getViewAngle(), EPS);
            assertEquals(PlayerState.NORMAL, player.getPlayerState());
            assertFalse(player.isDead());
        }

        @Test
        void newPlayer_canBoostImmediately() {
            assertTrue(player.shouldBoost());
            assertEquals(0.0, player.getBoostCooldown(), EPS);
            assertEquals(0.0, player.getBoostProgress(), EPS);
            assertEquals(PlayerConfig.BOOST_COOLDOWN, player.getBoostCooldownDuration(), EPS);
        }

        @Test
        void nullFactory_throws() {
            assertThrows(IllegalArgumentException.class, () -> new Player(0, 0, null, events));
        }

        @Test
        void nullEvents_throws() {
            assertThrows(IllegalArgumentException.class, () -> new Player(0, 0, factory, null));
        }

        @Test
        void getPos_returnsCopyOfPosition() {
            Vector2D pos = player.getPos();
            pos.copy(-1, -1);

            assertVector(100, 200, player.getPos());
        }
    }

    @Nested
    @DisplayName("Health & damage")
    class DamageTests {

        @Test
        void takeDamage_negative_throws() {
            assertThrows(IllegalArgumentException.class, () -> player.takeDamage(-1));
            assertEquals(PlayerConfig.MAX_HEALTH, player.getHealth());
            assertEquals(0, events.playerDamaged);
        }

        @Test
        void takeDamage_reducesHealth() {
            player.takeDamage(150);

            assertEquals(PlayerConfig.MAX_HEALTH - 150, player.getHealth());
            assertFalse(player.isDead());
        }

        @Test
        void takeDamage_accumulates() {
            player.takeDamage(100);
            player.takeDamage(250);

            assertEquals(PlayerConfig.MAX_HEALTH - 350, player.getHealth());
        }

        @Test
        void takeDamage_exactHealth_dies() {
            player.takeDamage(PlayerConfig.MAX_HEALTH);

            assertEquals(0, player.getHealth());
            assertTrue(player.isDead());
        }

        @Test
        void takeDamage_overHealth_clampsToZeroAndDies() {
            player.takeDamage(PlayerConfig.MAX_HEALTH + 500);

            assertEquals(0, player.getHealth());
            assertTrue(player.isDead());
        }

        @Test
        void takeDamage_notifiesListener() {
            player.takeDamage(10);
            player.takeDamage(20);

            assertEquals(2, events.playerDamaged);
            assertEquals(0, events.playerShot);
            assertEquals(0, events.playerBoosted);
            assertEquals(0, events.enemyDefeated);
            assertEquals(0, events.projectileHit);
        }
    }

    @Nested
    @DisplayName("Score")
    class ScoreTests {

        @Test
        void addScore_accumulates() {
            player.addScore(10);
            player.addScore(25);

            assertEquals(35, player.getScore());
        }

        @Test
        void addScore_zero_isAllowed() {
            player.addScore(0);

            assertEquals(0, player.getScore());
        }

        @Test
        void addScore_negative_throws() {
            player.addScore(5);

            assertThrows(IllegalArgumentException.class, () -> player.addScore(-1));
            assertEquals(5, player.getScore());
        }
    }

    @Nested
    @DisplayName("Shooting")
    class ShootingTests {

        @Test
        void shoot_beforeWeaponCooldown_returnsNull() {
            assertNull(player.shoot(new Vector2D(0, -1)));
            assertTrue(factory.requested.isEmpty());
        }

        @Test
        void shoot_setsOwnerPlayer() {
            player.update(PLASMA_FIRE_INTERVAL);

            Projectile projectile = player.shoot(new Vector2D(0, -1));

            assertNotNull(projectile);
            assertEquals(Projectile.Owner.PLAYER, projectile.getOwner());
        }

        @Test
        void shoot_firesPlasmaFromPlasmaGun() {
            player.update(PLASMA_FIRE_INTERVAL);

            Projectile projectile = player.shoot(new Vector2D(1, 0));

            assertEquals(List.of(ProjectileType.PLASMA), factory.requested);
            assertEquals(ProjectileType.PLASMA, projectile.getProjectileType());
        }

        @Test
        void shoot_spawnsAtNoseOfShip() {
            player.update(PLASMA_FIRE_INTERVAL);
            double offset = PlayerConfig.SPRITE_SIZE / 2.0;

            Projectile projectile = player.shoot(new Vector2D(0, -1));

            assertVector(100, 200 - offset, projectile.getPosition());
            assertVector(0, -1, projectile.getDirection());
        }

        @Test
        void shoot_unnormalizedDirection_offsetIsStillHalfSprite() {
            player.update(PLASMA_FIRE_INTERVAL);
            double offset = PlayerConfig.SPRITE_SIZE / 2.0;

            Projectile projectile = player.shoot(new Vector2D(3, 4));

            assertVector(100 + offset * 0.6, 200 + offset * 0.8, projectile.getPosition());
        }

        @Test
        void shoot_projectileUsesViewAngle() {
            player.setViewAngle(Math.PI / 3);
            player.update(PLASMA_FIRE_INTERVAL);

            Projectile projectile = player.shoot(new Vector2D(1, 0));

            assertEquals(Math.PI / 3, projectile.getCurrentAngle(), EPS);
        }

        @Test
        void shoot_twiceInARow_secondReturnsNull() {
            player.update(PLASMA_FIRE_INTERVAL);

            assertNotNull(player.shoot(new Vector2D(0, -1)));
            assertNull(player.shoot(new Vector2D(0, -1)));
            assertEquals(1, factory.requested.size());
        }

        @Test
        void shoot_doesNotNotifyListenerItself() {
            player.update(PLASMA_FIRE_INTERVAL);
            player.shoot(new Vector2D(0, -1));

            assertEquals(0, events.playerShot);
        }

        @Test
        void shoot_nullDirection_throws() {
            assertThrows(IllegalArgumentException.class, () -> player.shoot(null));
        }
    }

    @Nested
    @DisplayName("Movement & rotation in NORMAL state")
    class NormalStateTests {

        @Test
        void speed_isCappedAtMaxSpeed() {
            player.setSpeed(10_000);

            assertEquals(PlayerConfig.MAX_SPEED, player.getSpeed(), EPS);
        }

        @Test
        void update_movesAlongDirection() {
            player.setDirection(1, 0);
            player.setSpeed(PlayerConfig.MAX_SPEED);

            player.update(0.5);

            assertVector(100 + PlayerConfig.MAX_SPEED * 0.5, 200, player.getPosition());
        }

        @Test
        void update_currentAngleFollowsViewAngle() {
            player.setViewAngle(1.2);

            player.update(0.01);

            assertEquals(1.2, player.getCurrentAngle(), EPS);
        }

        @Test
        void updateRotation_normal_doesNotChangeDirection() {
            player.setDirection(1, 0);
            player.setViewAngle(Math.PI);

            player.updateRotation(0.1);

            assertVector(1, 0, player.getDirection());
        }

        @Test
        void updateRotation_negativeDelta_throws() {
            assertThrows(IllegalArgumentException.class, () -> player.updateRotation(-0.1));
        }

        @Test
        void update_negativeDelta_throws() {
            assertThrows(IllegalArgumentException.class, () -> player.update(-0.1));
        }

        @Test
        void setState_null_throws() {
            assertThrows(IllegalArgumentException.class, () -> player.setState(null));
            assertEquals(PlayerState.NORMAL, player.getPlayerState());
        }
    }

    @Nested
    @DisplayName("Boost")
    class BoostTests {

        @Test
        void startBoost_setsBoostingAndCooldown() {
            player.startBoost();

            assertEquals(PlayerState.BOOSTING, player.getPlayerState());
            assertEquals(PlayerConfig.BOOST_COOLDOWN, player.getBoostCooldown(), EPS);
            assertFalse(player.shouldBoost());
        }

        @Test
        void startBoost_doesNotNotifyListenerItself() {
            player.startBoost();

            assertEquals(0, events.playerBoosted);
        }

        @Test
        void boosting_raisesSpeedToBoostSpeed() {
            player.startBoost();

            player.update(0.1);

            assertEquals(PlayerConfig.BOOST_SPEED, player.getSpeed(), EPS);
            assertEquals(0.1 / PlayerConfig.BOOST_DURATION, player.getBoostProgress(), EPS);
        }

        @Test
        void boosting_speedLimitIsBoostSpeed() {
            player.startBoost();
            player.setSpeed(10_000);

            assertEquals(PlayerConfig.BOOST_SPEED, player.getSpeed(), EPS);
        }

        @Test
        void boosting_movesAtBoostSpeedAlongCurrentAngle() {
            player.startBoost();

            player.update(0.1);

            // angle 0 -> direction (0, -1)
            assertVector(100, 200 - PlayerConfig.BOOST_SPEED * 0.1, player.getPosition());
        }

        @Test
        void boosting_rotatesGraduallyTowardTargetAngle() {
            player.setTargetAngle(1.0);
            player.startBoost();

            player.update(0.1);

            double expected = 1.0 * PlayerConfig.ROTATION_SPEED * 0.1;
            assertEquals(expected, player.getCurrentAngle(), EPS);
        }

        @Test
        void boosting_ignoresViewAngle() {
            player.setViewAngle(2.0);
            player.startBoost();

            player.update(0.1);

            assertEquals(0.0, player.getCurrentAngle(), EPS);
        }

        @Test
        void boost_beforeDuration_staysBoosting() {
            player.startBoost();

            player.update(0.2);

            assertEquals(PlayerState.BOOSTING, player.getPlayerState());
        }

        @Test
        void boost_afterDuration_switchesToDecay() {
            player.startBoost();

            player.update(PlayerConfig.BOOST_DURATION);

            assertEquals(PlayerState.BOOST_DECAY, player.getPlayerState());
            assertEquals(0.0, player.getBoostProgress(), EPS);
        }

        @Test
        void boost_switchingToDecay_startsDecayInSameUpdate() {
            player.startBoost();

            player.update(PlayerConfig.BOOST_DURATION);

            double expected = PlayerConfig.BOOST_SPEED - PlayerConfig.DECAY_RATE * PlayerConfig.BOOST_DURATION;
            assertEquals(expected, player.getSpeed(), EPS);
        }

        @Test
        void boostDecay_reducesSpeedByDecayRate() {
            player.startBoost();
            player.update(PlayerConfig.BOOST_DURATION);

            double before = player.getSpeed();

            player.update(0.25);

            assertEquals(PlayerState.BOOST_DECAY, player.getPlayerState());
            assertEquals(before - PlayerConfig.DECAY_RATE * 0.25, player.getSpeed(), EPS);
        }

        @Test
        void boostDecay_reachesMaxSpeed_returnsToNormal() {
            player.startBoost();
            player.update(PlayerConfig.BOOST_DURATION);

            player.update(PlayerConfig.BOOST_DECAY);

            assertEquals(PlayerState.NORMAL, player.getPlayerState());
            assertEquals(PlayerConfig.MAX_SPEED, player.getSpeed(), EPS);
        }

        @Test
        void boostDecay_neverDropsBelowMaxSpeed() {
            player.startBoost();
            player.update(PlayerConfig.BOOST_DURATION);

            player.update(5);

            assertEquals(PlayerConfig.MAX_SPEED, player.getSpeed(), EPS);
            assertEquals(PlayerState.NORMAL, player.getPlayerState());
        }

        @Test
        void afterBoost_speedLimitBackToMaxSpeed() {
            player.startBoost();
            player.update(PlayerConfig.BOOST_DURATION);
            player.update(PlayerConfig.BOOST_DECAY);

            player.setSpeed(10_000);

            assertEquals(PlayerConfig.MAX_SPEED, player.getSpeed(), EPS);
        }

        @Test
        void setStateNormal_whileBoosting_clampsSpeed() {
            player.startBoost();
            player.update(0.1);

            player.setState(PlayerState.NORMAL);

            assertEquals(PlayerConfig.MAX_SPEED, player.getSpeed(), EPS);
        }

        @Test
        void boostCooldown_decreasesWithUpdate() {
            player.startBoost();

            player.update(0.3);

            assertEquals(PlayerConfig.BOOST_COOLDOWN - 0.3, player.getBoostCooldown(), EPS);
        }

        @Test
        void shouldBoost_duringCooldown_false() {
            player.startBoost();
            player.update(PlayerConfig.BOOST_DURATION);
            player.update(PlayerConfig.BOOST_DECAY);

            assertEquals(PlayerState.NORMAL, player.getPlayerState());
            assertFalse(player.shouldBoost());
        }

        @Test
        void shouldBoost_afterCooldown_true() {
            player.startBoost();

            player.update(PlayerConfig.BOOST_COOLDOWN);

            assertTrue(player.shouldBoost());
            assertEquals(0.0, player.getBoostCooldown(), EPS);
        }
    }
}
