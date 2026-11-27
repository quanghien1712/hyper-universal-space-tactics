package com.game.spaceshooter.application.manager;

import com.game.spaceshooter.application.model.Camera;
import com.game.spaceshooter.application.port.GameObjectPool;
import com.game.spaceshooter.application.port.ProjectileSpawner;
import com.game.spaceshooter.domain.entity.Enemy;
import com.game.spaceshooter.domain.entity.Player;
import com.game.spaceshooter.domain.port.GameEventListener;
import com.game.spaceshooter.domain.projectiles.Projectile;

import java.util.List;
import java.util.Objects;

public class EnemyManager {
    private final Player player;
    private final GameEventListener feedback;
    private final EnemyRoster enemies;
    private final ProjectileSpawner projectiles;
    private final GameObjectPool pool;
    private final Camera camera;
    private final WaveManager waveManager;

    public EnemyManager(Player player, Camera camera, EnemyRoster enemies,
                        ProjectileSpawner projectiles, GameEventListener feedback, GameObjectPool pool) {
        this.enemies = Objects.requireNonNull(enemies);
        this.projectiles = Objects.requireNonNull(projectiles);
        this.feedback = Objects.requireNonNull(feedback);
        this.pool = Objects.requireNonNull(pool);
        this.player = player;
        this.camera = camera;
        this.waveManager = new WaveManager(this::addActiveEnemy, camera, pool);
    }

    public void update(double deltaTime) {
        waveManager.update(deltaTime);
        for (Enemy current : enemies.snapshot()) {
            current.update(deltaTime);
            current.setBoundPos(camera.getScreenPos(current.getPosition()));
            if (current.getBound().intersects(player.getBound())) {
                current.takeDamage(current.getHealth());
                player.takeDamage(current.getDamage());
            }
            if (current.isDead()) {
                feedback.onEnemyDefeated(current.getPosition());
                current.setActive(false);
                player.addScore(current.getDefeatedScore());
                waveManager.enemyOnDefeated();
            }
            if (!current.isActive()) {
                enemies.remove(current);
                pool.release(current);
            } else {
                Projectile projectile = current.shoot();
                if (projectile == null) continue;
                projectile.setTarget(player);
                projectile.setBoundPos(camera.getScreenPos(projectile.getPosition()));
                projectiles.addActiveProjectile(projectile);
            }
        }
    }

    public List<Enemy> snapshot() {
        return enemies.snapshot();
    }

    public void addActiveEnemy(Enemy enemy) {
        enemy.setTarget(player);
        enemies.add(enemy);
    }

}
