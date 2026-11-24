package com.game.spaceshooter.domain.movement;

import com.game.spaceshooter.domain.entity.DynamicEntity;
import com.game.spaceshooter.infrastructure.utils.Vector2D;

public class LinearMovement implements MovementBehavior {
    public LinearMovement(){}

    @Override
    public void move(DynamicEntity entity, double deltaTime) {
        Vector2D normalizedDir = entity.getDirection().normalize();

        double x = entity.getPosition().getX() +
                   normalizedDir.getX() * entity.getSpeed() * deltaTime;
        double y = entity.getPosition().getY() +
                   normalizedDir.getY() * entity.getSpeed() * deltaTime;

        entity.setPosition(x, y);
    }

}
