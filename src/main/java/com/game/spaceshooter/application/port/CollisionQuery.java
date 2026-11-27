package com.game.spaceshooter.application.port;

import com.game.spaceshooter.domain.entity.Enemy;

import java.awt.Shape;

public interface CollisionQuery {
    Enemy checkCollided(Shape shape);
}
