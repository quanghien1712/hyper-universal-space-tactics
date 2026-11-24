package com.game.spaceshooter.domain.movement;

import com.game.spaceshooter.domain.entity.DynamicEntity;
import com.game.spaceshooter.infrastructure.config.EnemyConfig;
import com.game.spaceshooter.infrastructure.utils.Vector2D;

import java.util.function.Supplier;

public class OrbitMovement implements MovementBehavior{
    private Supplier<Vector2D> target;
    private final double moveRadius, acceleration, drag;
    private double orbitAngle = 0;

    public OrbitMovement(double moveRadius, double acceleration, double drag) {
        if (moveRadius < 0 || acceleration < 0 || drag < 0) {
            throw new IllegalArgumentException("Movement parameters must be nonnegative");
        }
        this.moveRadius = moveRadius;
        this.acceleration = acceleration;
        this.drag = drag;
    }

    @Override
    public void move(DynamicEntity entity, double deltaTime) {
        if (target == null) return;
        orbitAngle += EnemyConfig.ROTATION_SPEED * deltaTime;
        orbitAngle %= 2 * Math.PI;
        double moveAngle = orbitAngle - Math.PI / 2.0;
        double orbitX = Math.cos(moveAngle);
        double orbitY = Math.sin(moveAngle);
        entity.setCurrentAngle(orbitAngle);
        entity.setDirection(orbitX, orbitY);

        Vector2D dir = target.get().subtract(entity.getPosition());
        if (dir.squareLength() > moveRadius * moveRadius) {
            entity.accelerate(acceleration);
            dir.normalizeInPlace();
            double x = entity.getPosition().getX() +
                  dir.getX() * entity.getSpeed() * deltaTime;
            double y = entity.getPosition().getY() +
                  dir.getY() * entity.getSpeed() * deltaTime;
            entity.setPosition(x, y);
        } else entity.dragged(drag);
    }

    @Override
    public void setTarget(Supplier<Vector2D> supplier) {
        this.target = supplier;
    }
}
