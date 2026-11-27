package com.game.spaceshooter.application.port;

import com.game.spaceshooter.domain.effect.EffectType;
import com.game.spaceshooter.infrastructure.utils.Vector2D;

public interface EffectSpawner {
    void spawn(EffectType type, Vector2D position);
}
