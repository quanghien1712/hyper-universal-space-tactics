package com.game.spaceshooter.infrastructure.pool;

public interface Poolable {
    void onRelease();
    void setActive(boolean active);
    boolean isActive();
}
