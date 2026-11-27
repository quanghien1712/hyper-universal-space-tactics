package com.game.spaceshooter.application.manager;

import com.game.spaceshooter.application.port.EffectSpawner;
import com.game.spaceshooter.application.port.GameObjectPool;
import com.game.spaceshooter.domain.effect.Effect;
import com.game.spaceshooter.domain.effect.EffectType;
import com.game.spaceshooter.infrastructure.utils.Vector2D;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class EffectManager implements EffectSpawner {
    private final GameObjectPool pool;
    private final List<Effect> effects = new ArrayList<>();
    private final Object lock = new Object();

    public EffectManager(GameObjectPool pool) {
        this.pool = Objects.requireNonNull(pool);
    }

    public void update() {
        synchronized (lock) {
            for (int i = 0; i < effects.size(); ++i) {
                Effect effect = effects.get(i);
                effect.updateAnimation();
                if (!effect.isActive()) {
                    int last = effects.size() - 1;
                    effects.set(i, effects.get(last));
                    effects.remove(last);
                    pool.release(effect);
                    i--;
                }
            }
        }
    }

    public List<Effect> snapshot() {
        synchronized (lock) {
            return new ArrayList<>(effects);
        }
    }

    @Override
    public void spawn(EffectType type, Vector2D position) {
        Effect effect = pool.acquire(type);
        effect.setEffectType(type);
        effect.setPosition(position);
        addActiveEffect(effect);
    }

    public void addActiveEffect(Effect effect) {
        synchronized (lock) {
            effects.add(effect);
        }
    }
}
