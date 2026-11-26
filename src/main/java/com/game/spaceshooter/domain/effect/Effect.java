package com.game.spaceshooter.domain.effect;

import com.game.spaceshooter.infrastructure.pool.Poolable;
import com.game.spaceshooter.infrastructure.utils.Vector2D;

public abstract class Effect implements Poolable {
    private final Vector2D position;
    private final int width, height;
    private int aniIndex, aniTick;
    private final int aniSpeed;
    private final int totalFrame;
    private boolean active;
    private EffectType effectType;

    protected Effect(double x, double y, int width, int height, int aniSpeed, int totalFrame, EffectType type) {
        if (width <= 0 || height <= 0 || aniSpeed <= 0 || totalFrame <= 0 || type == null) {
            throw new IllegalArgumentException("Dimensions and animation timing must be positive; type required");
        }
        this.position = new Vector2D(x, y);
        this.width = width;
        this.height = height;
        this.aniSpeed = aniSpeed;
        this.totalFrame = totalFrame;
        this.effectType = type;
    }

    public void updateAnimation() {
        aniTick++;
        if (aniTick >= aniSpeed) {
            aniTick = 0;
            aniIndex++;
            if (aniIndex >= totalFrame) {
                aniIndex = 0;
                active = false;
            }
        }
    }

    @Override
    public void onRelease() {
        aniTick = aniIndex = 0;
        active = false;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public void setActive(boolean active) {
        this.active = active;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void setPosition(Vector2D pos) {
        if (pos == null) throw new IllegalArgumentException("position is required");
        position.copy(pos);
    }

    public Vector2D getPosition() {
        return position.copy();
    }

    public EffectType getEffectType() {
        return this.effectType;
    }

    public int getAniIndex() {
        return this.aniIndex;
    }

    public void setEffectType(EffectType effectType) {
        if (effectType == null) throw new IllegalArgumentException("effectType is required");
        this.effectType = effectType;
    }
}
