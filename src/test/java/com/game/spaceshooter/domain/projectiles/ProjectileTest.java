package com.game.spaceshooter.domain.projectiles;

import com.game.spaceshooter.domain.entity.Damageable;
import com.game.spaceshooter.infrastructure.utils.Vector2D;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProjectileTest {

    private static final double EPS = 1e-9;

    private static class TestProjectile extends Projectile {
        TestProjectile(int damage, ProjectileType type) {
            super(0, 0, 10, 10, damage, type);
        }
    }

    private static class FakeTarget implements Damageable {
        private final Vector2D pos;
        private int damageTaken;
        private int hitCount;
        private boolean dead;

        FakeTarget(double x, double y) {
            this.pos = new Vector2D(x, y);
        }

        @Override
        public boolean isDead() {
            return dead;
        }

        @Override
        public void takeDamage(int damage) {
            damageTaken += damage;
            hitCount++;
        }

        @Override
        public Vector2D getPos() {
            return pos.copy();
        }

        void moveTo(double x, double y) {
            pos.copy(x, y);
        }

        void kill() {
            dead = true;
        }
    }

    private static void assertVector(double expectedX, double expectedY, Vector2D actual) {
        assertEquals(expectedX, actual.getX(), EPS, "x");
        assertEquals(expectedY, actual.getY(), EPS, "y");
    }

    @Nested
    @DisplayName("Projectile base")
    class BaseTests {
        private Plasma projectile;

        @BeforeEach
        void setUp() {
            projectile = new Plasma();
        }

        @Test
        void constructor_negativeDamage_throws() {
            assertThrows(IllegalArgumentException.class, () -> new TestProjectile(-1, ProjectileType.PLASMA));
        }

        @Test
        void constructor_nullType_throws() {
            assertThrows(IllegalArgumentException.class, () -> new TestProjectile(5, null));
        }

        @Test
        void constructor_zeroDamage_isAllowed() {
            TestProjectile p = new TestProjectile(0, ProjectileType.NORMAL_BULLET);
            assertEquals(0, p.getDamage());
            assertEquals(ProjectileType.NORMAL_BULLET, p.getProjectileType());
        }

        @Test
        void newProjectile_isInactiveWithoutOwner() {
            assertFalse(projectile.isActive());
            assertEquals(Projectile.Owner.NONE, projectile.getOwner());
        }

        @Test
        void owner_isStored() {
            projectile.setOwner(Projectile.Owner.PLAYER);
            assertEquals(Projectile.Owner.PLAYER, projectile.getOwner());
            projectile.setOwner(Projectile.Owner.ENEMY);
            assertEquals(Projectile.Owner.ENEMY, projectile.getOwner());
        }

        @Test
        void setOwner_null_throws() {
            projectile.setOwner(Projectile.Owner.PLAYER);
            assertThrows(IllegalArgumentException.class, () -> projectile.setOwner(null));
            assertEquals(Projectile.Owner.PLAYER, projectile.getOwner());
        }

        @Test
        void setActive_updatesState() {
            projectile.setActive(true);
            assertTrue(projectile.isActive());
            projectile.setActive(false);
            assertFalse(projectile.isActive());
        }

        @Test
        void onHit_damagesTarget() {
            FakeTarget target = new FakeTarget(0, 0);
            projectile.onHit(target);
            assertEquals(projectile.getDamage(), target.damageTaken);
            assertEquals(1, target.hitCount);
        }

        @Test
        void onHit_eachHitDealsDamageAgain() {
            FakeTarget target = new FakeTarget(0, 0);
            projectile.onHit(target);
            projectile.onHit(target);
            assertEquals(2 * projectile.getDamage(), target.damageTaken);
        }

        @Test
        void onHit_doesNotDeactivateProjectile() {
            projectile.setActive(true);
            projectile.onHit(new FakeTarget(0, 0));
            assertTrue(projectile.isActive());
        }

        @Test
        void onHit_null_throws() {
            assertThrows(IllegalArgumentException.class, () -> projectile.onHit(null));
        }

        @Test
        void update_movesWithMovementStrategy() {
            projectile.setPosition(100, 100);
            projectile.setDirection(1, 0);
            projectile.update(0.01);
            assertVector(110, 100, projectile.getPosition());
        }

        @Test
        void onRelease_resetsStateAndInactive() {
            projectile.setPosition(300, 400);
            projectile.setDirection(0, -1);
            projectile.setSpeed(100);
            projectile.setCurrentAngle(1.5);
            projectile.setOwner(Projectile.Owner.ENEMY);
            projectile.setActive(true);

            projectile.onRelease();

            assertVector(0, 0, projectile.getPosition());
            assertVector(0, 0, projectile.getDirection());
            assertEquals(1000, projectile.getSpeed());
            assertEquals(0, projectile.getCurrentAngle());
            assertEquals(Projectile.Owner.NONE, projectile.getOwner());
            assertFalse(projectile.isActive());
        }

        @Test
        void onRelease_keepsDamageAndType() {
            projectile.onRelease();
            assertEquals(20, projectile.getDamage());
            assertEquals(ProjectileType.PLASMA, projectile.getProjectileType());
        }

        @Test
        void setTarget_onLinearProjectile_doesNotChangeMovement() {
            projectile.setPosition(100, 100);
            projectile.setDirection(1, 0);
            projectile.setTarget(new FakeTarget(100, -1000));
            projectile.update(0.01);
            assertVector(110, 100, projectile.getPosition());
        }
    }

    @Nested
    @DisplayName("Plasma & NormalBullet")
    class LinearProjectileTests {
        @Test
        void plasma_hasDefaultStats() {
            Plasma plasma = new Plasma();
            assertEquals(ProjectileType.PLASMA, plasma.getProjectileType());
            assertEquals(20, plasma.getDamage());
            assertEquals(30, plasma.getWidth());
            assertEquals(30, plasma.getHeight());
            assertEquals(1000, plasma.getSpeed());
            assertVector(0, 0, plasma.getPosition());
        }

        @Test
        void normalBullet_hasDefaultStats() {
            NormalBullet bullet = new NormalBullet();
            assertEquals(ProjectileType.NORMAL_BULLET, bullet.getProjectileType());
            assertEquals(15, bullet.getDamage());
            assertEquals(10, bullet.getWidth());
            assertEquals(40, bullet.getHeight());
            assertEquals(700, bullet.getSpeed());
            assertVector(0, 0, bullet.getPosition());
        }

        @Test
        void normalBullet_fliesStraightAlongDirection() {
            NormalBullet bullet = new NormalBullet();
            bullet.setDirection(0, -1);
            bullet.update(0.5);
            assertVector(0, -350, bullet.getPosition());
        }

        @Test
        void speed_isCappedAtDefault() {
            NormalBullet bullet = new NormalBullet();
            bullet.setSpeed(5000);
            assertEquals(700, bullet.getSpeed());
        }

        @Test
        void eachInstance_hasOwnPosition() {
            Plasma a = new Plasma();
            Plasma b = new Plasma();
            a.setDirection(1, 0);
            b.setDirection(-1, 0);
            a.update(0.1);
            b.update(0.1);
            assertVector(100, 0, a.getPosition());
            assertVector(-100, 0, b.getPosition());
        }
    }

    @Nested
    @DisplayName("HomingBullet")
    class HomingBulletTests {
        private HomingBullet bullet;

        @BeforeEach
        void setUp() {
            bullet = new HomingBullet();
        }

        @Test
        void homingBullet_hasDefaultStats() {
            assertEquals(ProjectileType.HOMING_BULLET, bullet.getProjectileType());
            assertEquals(5, bullet.getDamage());
            assertEquals(10, bullet.getWidth());
            assertEquals(60, bullet.getHeight());
            assertEquals(260, bullet.getSpeed());
        }

        @Test
        void update_withoutTarget_staysInPlace() {
            bullet.update(0.1);
            assertVector(0, 0, bullet.getPosition());
        }

        @Test
        void update_withLiveTarget_movesTowardTarget() {
            bullet.setTarget(new FakeTarget(0, -1000));
            bullet.update(0.1);
            assertVector(0, -26, bullet.getPosition());
        }

        @Test
        void update_targetToTheSide_turnsGradually() {
            bullet.setTarget(new FakeTarget(1000, 0));
            bullet.update(0.1);
            double angle = bullet.getCurrentAngle();
            assertTrue(angle > 0 && angle < Math.PI / 2, "turns toward target but not instantly");
            assertTrue(bullet.getPosition().getX() > 0);
        }

        @Test
        void update_followsTargetCurrentPosition() {
            FakeTarget target = new FakeTarget(0, -1000);
            bullet.setTarget(target);
            target.moveTo(1000, 0);
            bullet.update(0.1);
            assertTrue(bullet.getCurrentAngle() > 0);
            assertTrue(bullet.getPosition().getX() > 0);
        }

        @Test
        void update_deadTarget_deactivates() {
            FakeTarget target = new FakeTarget(0, -1000);
            target.kill();
            bullet.setActive(true);
            bullet.setTarget(target);
            bullet.update(0.1);
            assertVector(0, 0, bullet.getPosition());
            assertFalse(bullet.isActive());
        }

        @Test
        void update_withoutTarget_staysActive() {
            bullet.setActive(true);
            bullet.update(0.1);
            assertTrue(bullet.isActive());
        }

        @Test
        void update_targetDiesMidFlight_stopsAndDeactivates() {
            FakeTarget target = new FakeTarget(0, -1000);
            bullet.setActive(true);
            bullet.setTarget(target);
            bullet.update(0.1);
            Vector2D before = bullet.getPosition();
            target.kill();
            bullet.update(0.1);
            assertEquals(before, bullet.getPosition());
            assertFalse(bullet.isActive());
        }

        @Test
        void setTarget_null_stopsHoming() {
            bullet.setTarget(new FakeTarget(0, -1000));
            bullet.setTarget(null);
            bullet.update(0.1);
            assertVector(0, 0, bullet.getPosition());
        }

        @Test
        void onRelease_clearsTarget() {
            FakeTarget target = new FakeTarget(0, -1000);
            bullet.setTarget(target);
            bullet.setOwner(Projectile.Owner.ENEMY);
            bullet.setActive(true);

            bullet.onRelease();
            bullet.update(0.1);

            assertVector(0, 0, bullet.getPosition());
            assertEquals(Projectile.Owner.NONE, bullet.getOwner());
            assertFalse(bullet.isActive());
        }

        @Test
        void onHit_damagesTarget() {
            FakeTarget target = new FakeTarget(0, 0);
            bullet.onHit(target);
            assertEquals(5, target.damageTaken);
        }
    }
}
