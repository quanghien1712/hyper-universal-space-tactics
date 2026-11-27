package com.game.spaceshooter.application.manager;

import com.game.spaceshooter.application.model.Camera;
import com.game.spaceshooter.application.port.CollisionQuery;
import com.game.spaceshooter.application.port.GameObjectPool;
import com.game.spaceshooter.domain.effect.Effect;
import com.game.spaceshooter.domain.effect.EffectType;
import com.game.spaceshooter.domain.entity.BasicEnemy;
import com.game.spaceshooter.domain.entity.Enemy;
import com.game.spaceshooter.domain.entity.EnemyType;
import com.game.spaceshooter.domain.entity.Player;
import com.game.spaceshooter.domain.port.GameEventListener;
import com.game.spaceshooter.domain.projectiles.HomingBullet;
import com.game.spaceshooter.domain.projectiles.NormalBullet;
import com.game.spaceshooter.domain.projectiles.Plasma;
import com.game.spaceshooter.domain.projectiles.Projectile;
import com.game.spaceshooter.domain.projectiles.ProjectileType;
import com.game.spaceshooter.infrastructure.config.EnemyConfig;
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

class ProjectileManagerTest {

    private static final double PLAYER_X = 300;
    private static final double PLAYER_Y = 300;
    private static final double MARGIN = 1000;

    private static class FakePool implements GameObjectPool {
        private final List<Projectile> released = new ArrayList<>();
        private final List<Projectile.Owner> ownersAtRelease = new ArrayList<>();

        @Override
        public Projectile acquire(ProjectileType type) {
            return switch (type) {
                case PLASMA -> new Plasma();
                case NORMAL_BULLET -> new NormalBullet();
                case HOMING_BULLET -> new HomingBullet();
            };
        }

        @Override
        public Enemy acquire(EnemyType type) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Effect acquire(EffectType type) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void release(Enemy enemy) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void release(Projectile projectile) {
            released.add(projectile);
            ownersAtRelease.add(projectile.getOwner());
        }

        @Override
        public void release(Effect effect) {
            throw new UnsupportedOperationException();
        }
    }

    private static class FakeCollisionQuery implements CollisionQuery {
        private final List<Shape> queried = new ArrayList<>();
        private Enemy hit;

        @Override
        public Enemy checkCollided(Shape shape) {
            queried.add(shape);
            return hit;
        }
    }

    private static class RecordingListener implements GameEventListener {
        private int playerDamaged;
        private final List<Projectile> hits = new ArrayList<>();
        private final List<Projectile.Owner> ownersAtHit = new ArrayList<>();
        private final List<Boolean> activeAtHit = new ArrayList<>();

        @Override
        public void onPlayerDamaged() {
            playerDamaged++;
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
            hits.add(projectile);
            ownersAtHit.add(projectile.getOwner());
            activeAtHit.add(projectile.isActive());
        }
    }

    private Camera camera;
    private FakeCollisionQuery collisions;
    private RecordingListener playerEvents;
    private Player player;
    private RecordingListener feedback;
    private FakePool pool;
    private ProjectileManager manager;

    @BeforeEach
    void setUp() {
        camera = new Camera();
        collisions = new FakeCollisionQuery();
        pool = new FakePool();
        playerEvents = new RecordingListener();
        player = new Player(PLAYER_X, PLAYER_Y, pool, playerEvents);
        feedback = new RecordingListener();
        manager = new ProjectileManager(camera, collisions, player, feedback, pool);
    }

    private Projectile spawn(Projectile projectile, Projectile.Owner owner, double x, double y) {
        projectile.setPosition(x, y);
        projectile.setOwner(owner);
        projectile.setActive(true);
        manager.addActiveProjectile(projectile);
        return projectile;
    }

    private Projectile playerPlasma(double x, double y) {
        return spawn(new Plasma(), Projectile.Owner.PLAYER, x, y);
    }

    private Projectile enemyBullet(double x, double y) {
        return spawn(new NormalBullet(), Projectile.Owner.ENEMY, x, y);
    }

    private static BasicEnemy enemyAt(double x, double y, Camera camera) {
        BasicEnemy enemy = new BasicEnemy(type -> new NormalBullet());
        enemy.setPosition(x, y);
        enemy.setBoundPos(camera.getScreenPos(enemy.getPosition()));
        enemy.setActive(true);
        return enemy;
    }

    @Nested
    @DisplayName("Constructor, addActiveProjectile và snapshot")
    class Basics {

        @Test
        void constructor_nullPool_throws() {
            assertThrows(NullPointerException.class,
                  () -> new ProjectileManager(camera, collisions, player, feedback, null));
        }

        @Test
        void constructor_nullCollisions_throws() {
            assertThrows(NullPointerException.class,
                  () -> new ProjectileManager(camera, null, player, feedback, pool));
        }

        @Test
        void constructor_nullFeedback_throws() {
            assertThrows(NullPointerException.class,
                  () -> new ProjectileManager(camera, collisions, player, null, pool));
        }

        @Test
        void newManager_hasNoProjectiles() {
            assertTrue(manager.snapshot().isEmpty());
        }

        @Test
        void addActiveProjectile_appearsInSnapshot() {
            Projectile a = playerPlasma(900, 500);
            Projectile b = enemyBullet(1000, 200);

            assertEquals(List.of(a, b), manager.snapshot());
        }

        @Test
        void snapshot_returnsCopy() {
            playerPlasma(900, 500);

            manager.snapshot().clear();

            assertEquals(1, manager.snapshot().size());
        }

        @Test
        void update_withoutProjectiles_doesNothing() {
            manager.update(0.1);

            assertTrue(manager.snapshot().isEmpty());
            assertTrue(pool.released.isEmpty());
            assertTrue(feedback.hits.isEmpty());
        }
    }

    @Nested
    @DisplayName("Cập nhật vị trí và bound")
    class Movement {

        @Test
        void update_movesProjectiles() {
            Projectile plasma = playerPlasma(900, 500);
            plasma.setDirection(1, 0);

            manager.update(0.1);

            assertEquals(new Vector2D(1000, 500), plasma.getPosition());
            assertTrue(plasma.isActive());
        }

        @Test
        void update_setsBoundAtScreenPosition() {
            camera.followPlayer(new Player(GameConfig.PANEL_WIDTH / 2.0 + GameConfig.DEAD_ZONE_WIDTH / 2.0 + 100,
                  GameConfig.PANEL_HEIGHT / 2.0, pool, playerEvents), 1.0);
            Projectile plasma = playerPlasma(900, 500);

            manager.update(0);

            Rectangle bound = plasma.getBound();
            assertEquals(800 - plasma.getWidth() / 2, bound.x);
            assertEquals(500 - plasma.getHeight() / 2, bound.y);
        }

        @Test
        void update_playerBullet_queriesCollisionWithItsBound() {
            Projectile plasma = playerPlasma(900, 500);

            manager.update(0);

            assertEquals(1, collisions.queried.size());
            assertEquals(plasma.getBound(), collisions.queried.getFirst());
        }

        @Test
        void update_enemyBullet_doesNotQueryEnemies() {
            enemyBullet(900, 500);

            manager.update(0);

            assertTrue(collisions.queried.isEmpty());
        }
    }

    @Nested
    @DisplayName("Đạn trúng kẻ địch / người chơi")
    class Hits {

        @Test
        void playerBullet_hitsEnemy_damagesAndDeactivates() {
            BasicEnemy enemy = enemyAt(900, 500, camera);
            collisions.hit = enemy;
            Projectile plasma = playerPlasma(900, 500);

            manager.update(0);

            assertEquals(EnemyConfig.BASIC_DEFAULT_HEALTH - plasma.getDamage(), enemy.getHealth());
            assertFalse(plasma.isActive());
            assertTrue(manager.snapshot().isEmpty());
            assertEquals(List.of(plasma), pool.released);
        }

        @Test
        void playerBullet_hit_notifiesBeforeRelease() {
            collisions.hit = enemyAt(900, 500, camera);
            Projectile plasma = playerPlasma(900, 500);

            manager.update(0);

            assertEquals(List.of(plasma), feedback.hits);
            assertEquals(Projectile.Owner.PLAYER, feedback.ownersAtHit.getFirst(), "owner still set when notified");
            assertFalse(feedback.activeAtHit.getFirst(), "deactivated before notifying");
        }

        @Test
        void playerBullet_noEnemy_staysActive() {
            Projectile plasma = playerPlasma(900, 500);

            manager.update(0);

            assertTrue(plasma.isActive());
            assertEquals(List.of(plasma), manager.snapshot());
            assertTrue(pool.released.isEmpty());
            assertTrue(feedback.hits.isEmpty());
        }

        @Test
        void playerBullet_overPlayer_doesNotHurtPlayer() {
            Projectile plasma = playerPlasma(PLAYER_X, PLAYER_Y);

            manager.update(0);

            assertTrue(plasma.isActive());
            assertEquals(PlayerConfig.MAX_HEALTH, player.getHealth());
        }

        @Test
        void playerBullet_withEnemyRoster_damagesOverlappingEnemy() {
            EnemyRoster roster = new EnemyRoster();
            BasicEnemy near = enemyAt(900, 500, camera);
            BasicEnemy far = enemyAt(1200, 100, camera);
            roster.add(near);
            roster.add(far);
            ProjectileManager rosterManager = new ProjectileManager(camera, roster, player, feedback, pool);
            Projectile plasma = new Plasma();
            plasma.setPosition(910, 505);
            plasma.setOwner(Projectile.Owner.PLAYER);
            plasma.setActive(true);
            rosterManager.addActiveProjectile(plasma);

            rosterManager.update(0);

            assertEquals(EnemyConfig.BASIC_DEFAULT_HEALTH - plasma.getDamage(), near.getHealth());
            assertEquals(EnemyConfig.BASIC_DEFAULT_HEALTH, far.getHealth());
            assertTrue(rosterManager.snapshot().isEmpty());
        }

        @Test
        void enemyBullet_hitsPlayer() {
            Projectile bullet = enemyBullet(PLAYER_X, PLAYER_Y);

            manager.update(0);

            assertEquals(PlayerConfig.MAX_HEALTH - bullet.getDamage(), player.getHealth());
            assertEquals(1, playerEvents.playerDamaged);
            assertEquals(List.of(bullet), feedback.hits);
            assertFalse(bullet.isActive());
            assertEquals(List.of(bullet), pool.released);
            assertTrue(manager.snapshot().isEmpty());
        }

        @Test
        void enemyBullet_missesPlayer_staysActive() {
            Projectile bullet = enemyBullet(PLAYER_X + 200, PLAYER_Y);

            manager.update(0);

            assertTrue(bullet.isActive());
            assertEquals(PlayerConfig.MAX_HEALTH, player.getHealth());
            assertTrue(feedback.hits.isEmpty());
        }

        @Test
        void enemyBullet_hitsPlayer_withMovedCamera() {
            camera.followPlayer(new Player(GameConfig.PANEL_WIDTH / 2.0 + GameConfig.DEAD_ZONE_WIDTH / 2.0 + 100,
                  GameConfig.PANEL_HEIGHT / 2.0, pool, playerEvents), 1.0);
            player.setBoundPos(camera.getScreenPos(player.getPosition()));
            Projectile bullet = enemyBullet(PLAYER_X, PLAYER_Y);

            manager.update(0);

            assertFalse(bullet.isActive(), "both bounds are compared in screen coordinates");
            assertEquals(PlayerConfig.MAX_HEALTH - bullet.getDamage(), player.getHealth());
        }

        @Test
        void eachBullet_hitsOnlyOnce() {
            BasicEnemy enemy = enemyAt(900, 500, camera);
            collisions.hit = enemy;
            Projectile plasma = playerPlasma(900, 500);

            manager.update(0);
            manager.update(0);

            assertEquals(EnemyConfig.BASIC_DEFAULT_HEALTH - plasma.getDamage(), enemy.getHealth());
            assertEquals(1, feedback.hits.size());
            assertEquals(1, pool.released.size());
        }
    }

    @Nested
    @DisplayName("Đạn va đạn")
    class BulletVsBullet {

        @Test
        void oppositeBullets_cancelEachOther() {
            Projectile mine = playerPlasma(900, 500);
            Projectile theirs = enemyBullet(905, 505);

            manager.update(0);

            assertFalse(mine.isActive());
            assertFalse(theirs.isActive());
            assertTrue(manager.snapshot().isEmpty());
            assertEquals(2, pool.released.size());
            assertTrue(pool.released.containsAll(List.of(mine, theirs)));
            assertEquals(List.of(mine, theirs), feedback.hits);
        }

        @Test
        void sameOwnerBullets_doNotCancel() {
            Projectile a = playerPlasma(900, 500);
            Projectile b = playerPlasma(905, 505);
            Projectile c = enemyBullet(1200, 100);
            Projectile d = enemyBullet(1202, 102);

            manager.update(0);

            assertTrue(a.isActive() && b.isActive() && c.isActive() && d.isActive());
            assertEquals(4, manager.snapshot().size());
        }

        @Test
        void oppositeBullets_notOverlapping_bothStay() {
            Projectile mine = playerPlasma(900, 500);
            Projectile theirs = enemyBullet(1000, 500);

            manager.update(0);

            assertTrue(mine.isActive());
            assertTrue(theirs.isActive());
        }

        @Test
        void oneBullet_cancelsOnlyOneOpposite() {
            Projectile mine = playerPlasma(900, 500);
            Projectile first = enemyBullet(902, 500);
            Projectile second = enemyBullet(898, 500);

            manager.update(0);

            assertFalse(mine.isActive());
            assertFalse(first.isActive());
            assertTrue(second.isActive());
            assertEquals(List.of(second), manager.snapshot());
        }
    }

    @Nested
    @DisplayName("Ra ngoài màn hình và thu hồi về pool")
    class Cleanup {

        @Test
        void outOfScreen_releasedToPool() {
            Projectile plasma = playerPlasma(-MARGIN - 1, 100);

            manager.update(0);

            assertFalse(plasma.isActive());
            assertEquals(List.of(plasma), pool.released);
            assertTrue(manager.snapshot().isEmpty());
            assertTrue(feedback.hits.isEmpty(), "leaving the screen is not a hit");
        }

        @Test
        void outOfScreen_eachSide() {
            Projectile left = playerPlasma(-MARGIN - 1, 100);
            Projectile right = playerPlasma(GameConfig.PANEL_WIDTH + MARGIN + 1, 100);
            Projectile top = enemyBullet(100, -MARGIN - 1);
            Projectile bottom = enemyBullet(100, GameConfig.PANEL_HEIGHT + MARGIN + 1);

            manager.update(0);

            assertEquals(4, pool.released.size());
            assertTrue(pool.released.containsAll(List.of(left, right, top, bottom)));
        }

        @Test
        void insideMargin_isKept() {
            Projectile left = playerPlasma(-MARGIN, 100);
            Projectile right = playerPlasma(GameConfig.PANEL_WIDTH + MARGIN, 100);
            Projectile top = enemyBullet(1200, -MARGIN);
            Projectile bottom = enemyBullet(1200, GameConfig.PANEL_HEIGHT + MARGIN);

            manager.update(0);

            assertTrue(pool.released.isEmpty());
            assertEquals(List.of(left, right, top, bottom), manager.snapshot());
        }

        @Test
        void outOfScreen_isRelativeToCamera() {
            camera.followPlayer(new Player(GameConfig.PANEL_WIDTH / 2.0 + GameConfig.DEAD_ZONE_WIDTH / 2.0 + 100,
                  GameConfig.PANEL_HEIGHT / 2.0, pool, playerEvents), 1.0);
            Projectile kept = playerPlasma(100 - MARGIN + 50, 100);
            Projectile gone = playerPlasma(100 - MARGIN - 50, 600);

            manager.update(0);

            assertEquals(List.of(gone), pool.released, "left edge moved to cameraX - margin = -900");
            assertEquals(List.of(kept), manager.snapshot());
        }

        @Test
        void bulletLeavingScreen_whileMoving_isReleased() {
            Projectile plasma = playerPlasma(GameConfig.PANEL_WIDTH + MARGIN - 10, 100);
            plasma.setDirection(1, 0);

            manager.update(0.1);

            assertEquals(List.of(plasma), pool.released);
        }

        @Test
        void inactiveProjectile_isReleasedWithoutHit() {
            Projectile homing = spawn(new HomingBullet(), Projectile.Owner.ENEMY, 900, 500);
            homing.setActive(false);

            manager.update(0);

            assertEquals(List.of(homing), pool.released);
            assertTrue(feedback.hits.isEmpty());
        }

        @Test
        void release_resetsOwnerToNone() {
            playerPlasma(-MARGIN - 1, 100);

            manager.update(0);

            assertEquals(List.of(Projectile.Owner.NONE), pool.ownersAtRelease);
        }

        @Test
        void removingSeveral_keepsTheOthers() {
            Projectile a = playerPlasma(900, 100);
            Projectile b = playerPlasma(-MARGIN - 1, 100);
            Projectile c = playerPlasma(1000, 100);
            Projectile d = playerPlasma(-MARGIN - 1, 200);
            Projectile e = playerPlasma(1100, 100);

            manager.update(0);

            List<Projectile> remaining = manager.snapshot();
            assertEquals(3, remaining.size());
            assertTrue(remaining.containsAll(List.of(a, c, e)));
            assertEquals(2, pool.released.size());
            assertTrue(pool.released.containsAll(List.of(b, d)));
        }

        @Test
        void releasedOnce_notReleasedAgain() {
            playerPlasma(-MARGIN - 1, 100);

            manager.update(0);
            manager.update(0);

            assertEquals(1, pool.released.size());
        }
    }
}
