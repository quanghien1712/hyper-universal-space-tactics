package com.game.spaceshooter.domain.entity;

import com.game.spaceshooter.domain.movement.MovementBehavior;
import com.game.spaceshooter.infrastructure.config.GameConfig;
import com.game.spaceshooter.infrastructure.utils.Vector2D;

import java.awt.*;
import java.util.Objects;

public abstract class DynamicEntity {
    private final Vector2D position;
    private final int width;
    private final int height;
    private final Rectangle bound;
    private MovementBehavior movement;
    private final Vector2D direction;
    private double speed, rotateSpeed;
    private double maxSpeed;
    private double currentAngle; // move angle, not view angle
    private double targetAngle;

    public abstract void update(double deltaTime);

    public DynamicEntity(double x, double y, int width, int height) {
        if (width <= 0 || height <= 0) throw new IllegalArgumentException("Dimensions must be positive");
        this.position = new Vector2D(x, y);
        this.width = width;
        this.height = height;
        this.bound = new Rectangle((int)x - width / 2, (int)y - height / 2, width, height);
        this.direction = new Vector2D(0, 0);
    }

    public void updateRotation(double deltaTime) {
        requireNonNegative(deltaTime, "deltaTime");
        double angleDiff = targetAngle - currentAngle;
        while (angleDiff > Math.PI) angleDiff -= 2 * Math.PI;
        while (angleDiff < -Math.PI) angleDiff += 2 * Math.PI;
        if (Math.abs(angleDiff) < GameConfig.ROTATION_THRESHOLD) {
            setCurrentAngle(targetAngle);
        } else {
            setCurrentAngle(currentAngle + angleDiff * rotateSpeed * deltaTime);
        }
        double moveAngle = currentAngle - Math.PI / 2.0;
        double dx = Math.cos(moveAngle);
        double dy = Math.sin(moveAngle);
        setDirection(dx, dy);
    }

    public Rectangle getBound() {
        return new Rectangle(bound);
    }

    public void setBoundPos(Vector2D screenPos) {
        Objects.requireNonNull(screenPos, "screenPos is required");
        bound.setLocation((int)screenPos.getX() - width / 2, (int)screenPos.getY() - height / 2);
    }

    public Vector2D getPosition() {
        return position.copy();
    }

    public void setPosition(Vector2D newPos) {
        Objects.requireNonNull(newPos, "position is required");
        position.copy(newPos);
    }

    public void setPosition(double x, double y) {
        position.copy(x, y);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    protected void setMovement(MovementBehavior movement) {
        Objects.requireNonNull(movement, "movement is required");
        this.movement = movement;
    }

    protected MovementBehavior getMovement() { return movement; }

    protected void setMaxSpeed(double maxSpeed) {
        requireNonNegative(maxSpeed, "maxSpeed");
        this.maxSpeed = maxSpeed;
        speed = Math.min(speed, getSpeedLimit());
    }
    protected void setRotationSpeed(double rotateSpeed) {
        requireNonNegative(rotateSpeed, "rotationSpeed");
        this.rotateSpeed = rotateSpeed;
    }
    protected double getSpeedLimit() { return maxSpeed; }

    public void move(double deltaTime) {
        requireNonNegative(deltaTime, "deltaTime");
        this.movement.move(this, deltaTime);
    }

    public void setCurrentAngle(double angle) {
        this.currentAngle = angle;
    }

    public void setTargetAngle(double targetAngle) {
        this.targetAngle = targetAngle;
    }

    public double getCurrentAngle() {
        return currentAngle;
    }

    public double getSpeed() {
        return this.speed;
    }

    public Vector2D getDirection() {
        return direction.copy();
    }

    public void accelerate(double deltaSpeed) {
        requireNonNegative(deltaSpeed, "acceleration");
        setSpeed(Math.min(getSpeedLimit(), speed + deltaSpeed));
    }

    public void dragged(double deltaSpeed) {
        requireNonNegative(deltaSpeed, "drag");
        setSpeed(Math.max(0, speed - deltaSpeed));
    }

    public void setSpeed(double speed) {
        requireNonNegative(speed, "speed");
        this.speed = Math.min(speed, getSpeedLimit());
    }

    public void setDirection(Vector2D direction) {
        if (direction == null) throw new IllegalArgumentException("direction is required");
        this.direction.copy(direction);
    }

    public void setDirection(double x, double y) {
        direction.copy(x, y);
    }

    protected static void requireNonNegative(double value, String name) {
        if (value < 0) {
            throw new IllegalArgumentException(name + " must be nonnegative");
        }
    }
}
