package com.game.spaceshooter.application.manager;

import com.game.spaceshooter.application.model.Camera;
import com.game.spaceshooter.application.port.CollisionQuery;
import com.game.spaceshooter.application.port.GameObjectPool;
import com.game.spaceshooter.application.port.ProjectileSpawner;
import com.game.spaceshooter.domain.entity.Enemy;
import com.game.spaceshooter.domain.entity.Player;
import com.game.spaceshooter.domain.port.GameEventListener;
import com.game.spaceshooter.domain.projectiles.Projectile;
import com.game.spaceshooter.infrastructure.config.GameConfig;
import com.game.spaceshooter.infrastructure.utils.CollisionUtils;
import com.game.spaceshooter.infrastructure.utils.Vector2D;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ProjectileManager implements ProjectileSpawner {
    private final GameObjectPool pool;
    private final List<Projectile> activeProjectile;
    private final CollisionQuery collisions;
    private final GameEventListener feedback;
    private final Camera camera;
    private final Player player;
    private final Object lock = new Object();

    public ProjectileManager(Camera camera, CollisionQuery collisions,
                             Player player, GameEventListener feedback, GameObjectPool pool) {
        this.pool = Objects.requireNonNull(pool);
        this.activeProjectile = new ArrayList<>();
        this.camera = camera;
        this.collisions = Objects.requireNonNull(collisions);
        this.player = player;
        this.feedback = Objects.requireNonNull(feedback);
    }

    public void update(double deltaTime) {
        synchronized (lock) {
            for (int i = 0; i < activeProjectile.size(); ++i) {
                Projectile current = activeProjectile.get(i);
                current.update(deltaTime);
                current.setBoundPos(camera.getScreenPos(current.getPosition()));
                outOfScreen(current);
            }
            projectileVsOther();
            projectileVsProjectile();

            for (int i = 0; i < activeProjectile.size(); ++i) {
                Projectile current = activeProjectile.get(i);
                if (!current.isActive()) {
                    int last = activeProjectile.size() - 1;
                    activeProjectile.set(i, activeProjectile.get(last));
                    activeProjectile.set(last, current);
                    activeProjectile.remove(last);
                    current.setOwner(Projectile.Owner.NONE);
                    pool.release(current);
                    i--;
                }
            }
        }
    }

    private void outOfScreen(Projectile current) {
        Vector2D cameraPos = camera.getCameraPos();
        double margin = 1000;
        double left = cameraPos.getX() - margin;
        double right = cameraPos.getX() + GameConfig.PANEL_WIDTH + margin;
        double top = cameraPos.getY() - margin;
        double bottom = cameraPos.getY() + GameConfig.PANEL_HEIGHT + margin;
        Vector2D position = current.getPosition();
        if (position.getX() < left ||
              position.getX() > right ||
              position.getY() < top ||
              position.getY() > bottom) {
            current.setActive(false);
        }
    }

    private void projectileVsOther() {
        for (int i = 0; i < activeProjectile.size(); ++i) {
            Projectile current = activeProjectile.get(i);
            if (!current.isActive()) continue;
            if (current.getOwner() == Projectile.Owner.PLAYER) {
                Enemy enemy = collisions.checkCollided(current.getBound());
                if (enemy == null) continue;
                current.setActive(false);
                feedback.onProjectileHit(current);
                current.onHit(enemy);
            }
            else {
                if (CollisionUtils.intersects(current.getBound(), player.getBound())) {
                    current.setActive(false);
                    feedback.onProjectileHit(current);
                    current.onHit(player);
                }
            }
        }
    }


    private void projectileVsProjectile() {
        for (int i = 0; i < activeProjectile.size(); ++i) {
            Projectile current = activeProjectile.get(i);
            if (!current.isActive()) continue;
            for (int j = i + 1; j < activeProjectile.size(); ++j) {
                Projectile other = activeProjectile.get(j);
                if (!other.isActive()) continue;
                if (current.getOwner() != other.getOwner()
                      && CollisionUtils.intersects(current.getBound(), other.getBound())) {
                    current.setActive(false);
                    feedback.onProjectileHit(current);
                    other.setActive(false);
                    feedback.onProjectileHit(other);
                    break;
                }
            }
        }
    }

    public List<Projectile> snapshot() {
        synchronized (lock) {
            return new ArrayList<>(activeProjectile);
        }
    }

    @Override
    public void addActiveProjectile(Projectile projectile) {
        synchronized (lock) {
            activeProjectile.add(projectile);
        }
    }

}
