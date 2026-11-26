package com.game.spaceshooter.domain.weapon;

import com.game.spaceshooter.domain.port.ProjectileFactory;
import com.game.spaceshooter.domain.projectiles.HomingBullet;
import com.game.spaceshooter.domain.projectiles.NormalBullet;
import com.game.spaceshooter.domain.projectiles.Plasma;
import com.game.spaceshooter.domain.projectiles.Projectile;
import com.game.spaceshooter.domain.projectiles.ProjectileType;
import com.game.spaceshooter.infrastructure.utils.Vector2D;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WeaponTest {

    private static final double EPS = 1e-9;
    private static final double FIRE_RATE = 4;
    private static final double FIRE_INTERVAL = 0.25;

    private static class TestWeapon extends Weapon {
        private int createCount;

        TestWeapon(double fireRate) {
            super(fireRate);
        }

        @Override
        public Projectile createProjectile(Vector2D playerPos, Vector2D aimDirection) {
            createCount++;
            Projectile projectile = new Plasma();
            projectile.setPosition(playerPos);
            projectile.setDirection(aimDirection);
            return projectile;
        }
    }

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

    private static void assertVector(double expectedX, double expectedY, Vector2D actual) {
        assertEquals(expectedX, actual.getX(), EPS, "x");
        assertEquals(expectedY, actual.getY(), EPS, "y");
    }

    private static Vector2D pos() {
        return new Vector2D(100, 200);
    }

    private static Vector2D dir() {
        return new Vector2D(0, -1);
    }

    @Nested
    @DisplayName("Constructor")
    class Constructor {

        @Test
        void constructor_nonPositiveFireRate_throws() {
            assertThrows(IllegalArgumentException.class, () -> new TestWeapon(0));
            assertThrows(IllegalArgumentException.class, () -> new TestWeapon(-1));
        }

        @Test
        void fireInterval_isInverseOfFireRate() {
            assertEquals(FIRE_INTERVAL, new TestWeapon(FIRE_RATE).getFireInterval(), EPS);
            assertEquals(2.5, new TestWeapon(0.4).getFireInterval(), EPS);
        }

        @Test
        void newWeapon_startsOnFullCooldown() {
            TestWeapon weapon = new TestWeapon(FIRE_RATE);

            assertEquals(weapon.getFireInterval(), weapon.getCooldownRemaining(), EPS);
        }
    }

    @Nested
    @DisplayName("update")
    class Update {

        private TestWeapon weapon;

        @BeforeEach
        void setUp() {
            weapon = new TestWeapon(FIRE_RATE);
        }

        @Test
        void update_reducesCooldownByDeltaTime() {
            weapon.update(0.1);

            assertEquals(FIRE_INTERVAL - 0.1, weapon.getCooldownRemaining(), EPS);
        }

        @Test
        void update_neverGoesBelowZero() {
            weapon.update(10);

            assertEquals(0.0, weapon.getCooldownRemaining(), EPS);
        }

        @Test
        void update_zeroDelta_keepsCooldown() {
            weapon.update(0);

            assertEquals(FIRE_INTERVAL, weapon.getCooldownRemaining(), EPS);
        }

        @Test
        void update_negativeDelta_throws() {
            assertThrows(IllegalArgumentException.class, () -> weapon.update(-0.01));
            assertEquals(FIRE_INTERVAL, weapon.getCooldownRemaining(), EPS);
        }
    }

    @Nested
    @DisplayName("fire")
    class Fire {

        private TestWeapon weapon;

        @BeforeEach
        void setUp() {
            weapon = new TestWeapon(FIRE_RATE);
        }

        @Test
        void fire_duringCooldown_returnsNull() {
            weapon.update(FIRE_INTERVAL / 2);

            assertNull(weapon.fire(pos(), dir()));
            assertEquals(0, weapon.createCount);
        }

        @Test
        void fire_duringCooldown_doesNotResetCooldown() {
            weapon.update(0.1);
            weapon.fire(pos(), dir());

            assertEquals(FIRE_INTERVAL - 0.1, weapon.getCooldownRemaining(), EPS);
        }

        @Test
        void fire_afterCooldown_returnsProjectile() {
            weapon.update(FIRE_INTERVAL);

            Projectile projectile = weapon.fire(pos(), dir());

            assertNotNull(projectile);
            assertEquals(1, weapon.createCount);
            assertVector(100, 200, projectile.getPosition());
            assertVector(0, -1, projectile.getDirection());
        }

        @Test
        void fire_afterCooldown_restartsCooldown() {
            weapon.update(FIRE_INTERVAL);
            weapon.fire(pos(), dir());

            assertEquals(FIRE_INTERVAL, weapon.getCooldownRemaining(), EPS);
            assertNull(weapon.fire(pos(), dir()));
            assertEquals(1, weapon.createCount);
        }

        @Test
        void fire_holdingTrigger_firesAtFireRate() {
            int shots = 0;
            double step = 0.03;
            for (int i = 0; i < 100; i++) {
                weapon.update(step);
                if (weapon.fire(pos(), dir()) != null) shots++;
            }

            // 0.25 s / 0.03 s -> fires every 9th step (excess time is not carried over)
            assertEquals(11, shots);
        }

        @Test
        void fire_nullArgs_throws() {
            weapon.update(FIRE_INTERVAL);

            assertThrows(IllegalArgumentException.class, () -> weapon.fire(null, dir()));
            assertThrows(IllegalArgumentException.class, () -> weapon.fire(pos(), null));
            assertThrows(IllegalArgumentException.class, () -> weapon.fire(null, null));
        }

        @Test
        void fire_nullArgs_doesNotConsumeCooldown() {
            weapon.update(FIRE_INTERVAL);

            assertThrows(IllegalArgumentException.class, () -> weapon.fire(null, dir()));

            assertEquals(0.0, weapon.getCooldownRemaining(), EPS);
            assertNotNull(weapon.fire(pos(), dir()));
        }
    }

    @Nested
    @DisplayName("PlasmaGun, RifleAssault, SmartGun")
    class ConcreteWeapons {

        private FakeProjectileFactory factory;

        @BeforeEach
        void setUp() {
            factory = new FakeProjectileFactory();
        }

        @Test
        void nullFactory_throws() {
            assertThrows(IllegalArgumentException.class, () -> new PlasmaGun(null));
            assertThrows(IllegalArgumentException.class, () -> new RifleAssault(null));
            assertThrows(IllegalArgumentException.class, () -> new SmartGun(null));
        }

        @Test
        void fireRates_matchEachWeapon() {
            assertEquals(1.0 / 7, new PlasmaGun(factory).getFireInterval(), EPS);
            assertEquals(1.0 / 6, new RifleAssault(factory).getFireInterval(), EPS);
            assertEquals(1.0 / 0.4, new SmartGun(factory).getFireInterval(), EPS);
        }

        @Test
        void plasmaGun_firesPlasma() {
            Weapon gun = new PlasmaGun(factory);
            gun.update(gun.getFireInterval());

            Projectile projectile = gun.fire(pos(), dir());

            assertEquals(List.of(ProjectileType.PLASMA), factory.requested);
            assertEquals(ProjectileType.PLASMA, projectile.getProjectileType());
        }

        @Test
        void rifleAssault_firesNormalBullet() {
            Weapon gun = new RifleAssault(factory);
            gun.update(gun.getFireInterval());

            Projectile projectile = gun.fire(pos(), dir());

            assertEquals(List.of(ProjectileType.NORMAL_BULLET), factory.requested);
            assertEquals(ProjectileType.NORMAL_BULLET, projectile.getProjectileType());
        }

        @Test
        void smartGun_firesHomingBullet() {
            Weapon gun = new SmartGun(factory);
            gun.update(gun.getFireInterval());

            Projectile projectile = gun.fire(pos(), dir());

            assertEquals(List.of(ProjectileType.HOMING_BULLET), factory.requested);
            assertEquals(ProjectileType.HOMING_BULLET, projectile.getProjectileType());
        }

        @Test
        void createProjectile_placesProjectileAtPlayerAndAim() {
            Weapon gun = new RifleAssault(factory);

            Projectile projectile = gun.createProjectile(new Vector2D(-30, 45), new Vector2D(1, 0));

            assertVector(-30, 45, projectile.getPosition());
            assertVector(1, 0, projectile.getDirection());
        }

        @Test
        void createProjectile_copiesVectors() {
            Weapon gun = new PlasmaGun(factory);
            Vector2D playerPos = new Vector2D(10, 20);
            Vector2D aim = new Vector2D(0, 1);

            Projectile projectile = gun.createProjectile(playerPos, aim);
            playerPos.copy(999, 999);
            aim.copy(-1, 0);

            assertVector(10, 20, projectile.getPosition());
            assertVector(0, 1, projectile.getDirection());
        }

        @Test
        void duringCooldown_factoryIsNotCalled() {
            Weapon gun = new SmartGun(factory);

            assertNull(gun.fire(pos(), dir()));
            assertTrue(factory.requested.isEmpty());
        }
    }
}
