package com.game.spaceshooter.infrastructure.pool;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.function.Supplier;

public class ObjectPool <T extends Poolable> {
    private final Supplier<T> factory;
    private final Queue<T> available;

    public ObjectPool(Supplier<T> factory, int maxSize) {
        if (factory == null || maxSize < 0) {
            throw new IllegalArgumentException("Factory is required | initial size nonnegative");
        }
        this.factory = factory;
        this.available = new ArrayDeque<>();
        for (int i = 0; i < maxSize; ++i) {
            available.add(factory.get());
        }
    }

    public synchronized T acquire() {
        T obj = (available.isEmpty()) ? factory.get() : available.poll();
        obj.setActive(true);
        return obj;
    }

    public synchronized void release(T obj) {
        obj.onRelease();
        available.add(obj);
    }
}
