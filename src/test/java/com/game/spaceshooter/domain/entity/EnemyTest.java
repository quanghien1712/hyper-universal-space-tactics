package com.game.spaceshooter.domain.entity;

import com.game.spaceshooter.domain.port.ProjectileFactory;
import com.game.spaceshooter.domain.projectiles.HomingBullet;
import com.game.spaceshooter.domain.projectiles.NormalBullet;
import com.game.spaceshooter.domain.projectiles.Projectile;
import com.game.spaceshooter.domain.projectiles.ProjectileType;
import com.game.spaceshooter.infrastructure.config.EnemyConfig;
import com.game.spaceshooter.infrastructure.utils.Vector2D;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EnemyTest {

    private static final double EPS = 1e-9;

    private static class RecordingFactory implements ProjectileFactory {
        private final List<ProjectileType> requested = new ArrayList<>();

        @Override
        public Projectile acquire(ProjectileType type) {
            requested.add(type);
            return type == ProjectileType.HOMING_BULLET ? new HomingBullet() : new NormalBullet();
        }
    }

    private static class TestEnemy extends Enemy {
        TestEnemy(int maxHealth, int damage, EnemyType type, int score) {
            super(0, 0, 20, 20, maxHealth, damage, type, score);
        }
    }

    private static class FixedTarget implements Damageable {
        private final Vector2D pos;

        FixedTarget(double x, double y) {
            this.pos = new Vector2D(x, y);
        }

        @Override
        public boolean isDead() { return false; }

        @Override
        public void takeDamage(int damage) {}

        @Override
        public Vector2D getPos() { return pos.copy(); }
    }

    private static void assertVector(double expectedX, double expectedY, Vector2D actual) {
        assertEquals(expectedX, actual.getX(), EPS, "x");
        assertEquals(expectedY, actual.getY(), EPS, "y");
    }

    @Nested
    class ConstructorTests {

        @Test
        void constructor_invalidArguments_throws() {
            assertThrows(IllegalArgumentException.class, () -> new TestEnemy(0, 1, EnemyType.DRONE, 1));
            assertThrows(IllegalArgumentException.class, () -> new TestEnemy(10, -1, EnemyType.DRONE, 1));
            assertThrows(IllegalArgumentException.class, () -> new TestEnemy(10, 1, EnemyType.DRONE, -1));
            assertThrows(IllegalArgumentException.class, () -> new TestEnemy(10, 1, null, 1));
        }

        @Test
        void constructor_zeroDamageAndScore_allowed() {
            TestEnemy enemy = new TestEnemy(10, 0, EnemyType.DRONE, 0);

            assertEquals(0, enemy.getDamage());
            assertEquals(0, enemy.getDefeatedScore());
        }

        @Test
        void constructor_startsWithFullHealthAndInactive() {
            TestEnemy enemy = new TestEnemy(80, 5, EnemyType.BASIC_ENEMY, 30);

            assertEquals(80, enemy.getMaxHealth());
            assertEquals(80, enemy.getHealth());
            assertFalse(enemy.isDead());
            assertFalse(enemy.isActive());
        }
    }

    @Nested
    class EnemyTypeTests {

        @Test
        void drone_usesDroneConfig() {
            Drone drone = new Drone();

            assertEquals(EnemyType.DRONE, drone.getEnemyType());
            assertEquals(EnemyConfig.DRONE_DEFAULT_HEALTH, drone.getMaxHealth());
            assertEquals(EnemyConfig.DRONE_DEFAULT_DAMAGE, drone.getDamage());
            assertEquals(EnemyConfig.DRONE_SCORE, drone.getDefeatedScore());
        }

        @Test
        void basicEnemy_usesBasicConfig() {
            BasicEnemy enemy = new BasicEnemy(new RecordingFactory());

            assertEquals(EnemyType.BASIC_ENEMY, enemy.getEnemyType());
            assertEquals(EnemyConfig.BASIC_DEFAULT_HEALTH, enemy.getMaxHealth());
            assertEquals(EnemyConfig.BASIC_DEFAULT_DAMAGE, enemy.getDamage());
            assertEquals(EnemyConfig.BASIC_SCORE, enemy.getDefeatedScore());
        }

        @Test
        void orbitEnemy_usesOrbitConfig() {
            OrbitEnemy enemy = new OrbitEnemy(new RecordingFactory());

            assertEquals(EnemyType.ORBITING_ENEMY, enemy.getEnemyType());
            assertEquals(EnemyConfig.ORBIT_DEFAULT_HEALTH, enemy.getMaxHealth());
            assertEquals(EnemyConfig.ORBIT_DEFAULT_DAMAGE, enemy.getDamage());
            assertEquals(EnemyConfig.ORBIT_SCORE, enemy.getDefeatedScore());
        }

        @Test
        void boss_usesBossConfig() {
            Boss boss = new Boss(new RecordingFactory());

            assertEquals(EnemyType.BOSS, boss.getEnemyType());
            assertEquals(EnemyConfig.BOSS_DEFAULT_HEALTH, boss.getMaxHealth());
            assertEquals(EnemyConfig.BOSS_DEFAULT_DAMAGE, boss.getDamage());
            assertEquals(EnemyConfig.BOSS_SCORE, boss.getDefeatedScore());
        }

        @Test
        void speed_isCappedByTypeMaxSpeed() {
            Drone drone = new Drone();
            BasicEnemy basic = new BasicEnemy(new RecordingFactory());
            OrbitEnemy orbit = new OrbitEnemy(new RecordingFactory());
            Boss boss = new Boss(new RecordingFactory());

            drone.setSpeed(10_000);
            basic.setSpeed(10_000);
            orbit.setSpeed(10_000);
            boss.setSpeed(10_000);

            assertEquals(EnemyConfig.DRONE_MAX_SPEED, drone.getSpeed(), EPS);
            assertEquals(EnemyConfig.BASIC_MAX_SPEED, basic.getSpeed(), EPS);
            assertEquals(EnemyConfig.ORBIT_MAX_SPEED, orbit.getSpeed(), EPS);
            assertEquals(EnemyConfig.BOSS_MAX_SPEED, boss.getSpeed(), EPS);
        }

        @Test
        void armedEnemies_nullFactory_throws() {
            assertThrows(IllegalArgumentException.class, () -> new BasicEnemy(null));
            assertThrows(IllegalArgumentException.class, () -> new OrbitEnemy(null));
            assertThrows(IllegalArgumentException.class, () -> new Boss(null));
        }
    }

    @Nested
    class HealthTests {

        @Test
        void takeDamage_reducesHealth() {
            TestEnemy enemy = new TestEnemy(100, 5, EnemyType.DRONE, 10);

            enemy.takeDamage(30);

            assertEquals(70, enemy.getHealth());
            assertFalse(enemy.isDead());
        }

        @Test
        void takeDamage_overkill_clampsToZeroAndDies() {
            TestEnemy enemy = new TestEnemy(100, 5, EnemyType.DRONE, 10);

            enemy.takeDamage(250);

            assertEquals(0, enemy.getHealth());
            assertTrue(enemy.isDead());
        }

        @Test
        void takeDamage_exactHealth_dies() {
            TestEnemy enemy = new TestEnemy(100, 5, EnemyType.DRONE, 10);

            enemy.takeDamage(100);

            assertTrue(enemy.isDead());
        }

        @Test
        void takeDamage_negative_throws() {
            TestEnemy enemy = new TestEnemy(100, 5, EnemyType.DRONE, 10);

            assertThrows(IllegalArgumentException.class, () -> enemy.takeDamage(-1));
            assertEquals(100, enemy.getHealth());
        }
    }

    @Nested
    class ShootTests {

        @Test
        void shoot_drone_hasNoWeapon_returnsNull() {
            Drone drone = new Drone();
            drone.setDirection(0, -1);
            drone.update(100);

            assertNull(drone.shoot());
        }

        @Test
        void shoot_duringCooldown_returnsNull() {
            RecordingFactory factory = new RecordingFactory();
            OrbitEnemy enemy = new OrbitEnemy(factory);
            enemy.setDirection(0, -1);

            assertNull(enemy.shoot());
            assertTrue(factory.requested.isEmpty());
        }

        @Test
        void shoot_afterCooldown_spawnsEnemyProjectileAtNose() {
            RecordingFactory factory = new RecordingFactory();
            BasicEnemy enemy = new BasicEnemy(factory);
            enemy.setPosition(100, 200);
            enemy.setDirection(0, -1);
            enemy.setCurrentAngle(0.5);
            enemy.update(10);

            Projectile projectile = enemy.shoot();

            assertNotNull(projectile);
            assertEquals(Projectile.Owner.ENEMY, projectile.getOwner());
            assertEquals(0.5, projectile.getCurrentAngle(), EPS);
            assertVector(100, 200 - enemy.getHeight() / 2.0, projectile.getPosition());
            assertVector(0, -1, projectile.getDirection());
        }

        @Test
        void shoot_weaponDecidesProjectileType() {
            RecordingFactory factory = new RecordingFactory();
            BasicEnemy basic = new BasicEnemy(factory);
            OrbitEnemy orbit = new OrbitEnemy(factory);
            Boss boss = new Boss(factory);
            for (Enemy enemy : List.of(basic, orbit, boss)) {
                enemy.setDirection(0, -1);
                enemy.update(10);
                assertNotNull(enemy.shoot());
            }

            assertEquals(List.of(ProjectileType.HOMING_BULLET, ProjectileType.NORMAL_BULLET,
                    ProjectileType.NORMAL_BULLET), factory.requested);
        }

        @Test
        void shoot_twiceInARow_secondIsOnCooldown() {
            Boss boss = new Boss(new RecordingFactory());
            boss.setDirection(0, -1);
            boss.update(10);

            assertNotNull(boss.shoot());
            assertNull(boss.shoot());
        }
    }

    @Nested
    class UpdateTests {

        @Test
        void update_negativeDeltaTime_throws() {
            Drone drone = new Drone();

            assertThrows(IllegalArgumentException.class, () -> drone.update(-1));
        }

        @Test
        void update_withoutTarget_staysInPlace() {
            Drone drone = new Drone();
            drone.setPosition(10, 20);

            drone.update(1);

            assertVector(10, 20, drone.getPosition());
        }

        @Test
        void update_withTarget_movesTowardTarget() {
            Drone drone = new Drone();
            drone.setTarget(new FixedTarget(0, -1000));

            drone.update(1);

            assertTrue(drone.getSpeed() > 0);
            assertTrue(drone.getPosition().getY() < 0);
        }

        @Test
        void setTarget_null_stopsFollowing() {
            Drone drone = new Drone();
            drone.setTarget(new FixedTarget(0, -1000));
            drone.setTarget(null);
            drone.setPosition(5, 5);

            drone.update(1);

            assertVector(5, 5, drone.getPosition());
        }

        @Test
        void getPos_matchesPosition() {
            Drone drone = new Drone();
            drone.setPosition(42, -7);

            assertVector(42, -7, drone.getPos());
        }
    }

    @Nested
    class PoolTests {

        @Test
        void setActive_togglesState() {
            Drone drone = new Drone();

            drone.setActive(true);
            assertTrue(drone.isActive());
            drone.setActive(false);
            assertFalse(drone.isActive());
        }

        @Test
        void onRelease_resetsStateForReuse() {
            Drone drone = new Drone();
            drone.setActive(true);
            drone.setPosition(300, 400);
            drone.setDirection(1, 0);
            drone.setCurrentAngle(2);
            drone.takeDamage(150);

            drone.onRelease();

            assertFalse(drone.isActive());
            assertEquals(drone.getMaxHealth(), drone.getHealth());
            assertFalse(drone.isDead());
            assertVector(0, 0, drone.getPosition());
            assertVector(0, 0, drone.getDirection());
            assertEquals(0, drone.getCurrentAngle(), EPS);
        }

        @Test
        void onRelease_clearsTarget() {
            Drone drone = new Drone();
            drone.setTarget(new FixedTarget(0, -1000));

            drone.onRelease();
            drone.update(1);

            assertVector(0, 0, drone.getPosition());
        }
    }
}
