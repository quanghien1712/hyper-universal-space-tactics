package com.game.spaceshooter.domain.movement;

import com.game.spaceshooter.domain.entity.DynamicEntity;
import com.game.spaceshooter.infrastructure.utils.Vector2D;

import java.util.function.Supplier;

public interface MovementBehavior {
    void move(DynamicEntity entity, double deltaTime);

    default void setTarget(Supplier<Vector2D> target) {}
}
