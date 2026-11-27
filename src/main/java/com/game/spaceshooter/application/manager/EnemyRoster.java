package com.game.spaceshooter.application.manager;

import com.game.spaceshooter.application.port.CollisionQuery;
import com.game.spaceshooter.domain.entity.Enemy;
import com.game.spaceshooter.infrastructure.utils.CollisionUtils;

import java.awt.Shape;
import java.util.ArrayList;
import java.util.List;

public class EnemyRoster implements CollisionQuery {
    private final List<Enemy> enemies = new ArrayList<>();

    public synchronized void add(Enemy enemy) { enemies.add(enemy); }
    public synchronized void remove(Enemy enemy) { enemies.remove(enemy); }
    public synchronized List<Enemy> snapshot() { return new ArrayList<>(enemies); }

    @Override
    public synchronized Enemy checkCollided(Shape shape) {
        for (Enemy enemy : enemies) {
            if (CollisionUtils.intersects(shape, enemy.getBound())) return enemy;
        }
        return null;
    }
}
