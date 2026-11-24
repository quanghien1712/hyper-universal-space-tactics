package com.game.spaceshooter.domain.entity;

import com.game.spaceshooter.infrastructure.utils.Vector2D;

public interface Damageable {
    boolean isDead();
    void takeDamage(int damage);
    Vector2D getPos();
}
