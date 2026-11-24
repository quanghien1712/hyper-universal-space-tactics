package com.game.spaceshooter.infrastructure.pool;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

class ObjectPoolTest {

    private static class TestObject implements Poolable {
        private boolean active;
        private int releaseCount;

        @Override
        public void onRelease() {
            releaseCount++;
            active = false;
        }

        @Override
        public void setActive(boolean active) {
            this.active = active;
        }

        @Override
        public boolean isActive() {
            return active;
        }

        int getReleaseCount() {
            return releaseCount;
        }
    }

    private static class CountingFactory implements Supplier<TestObject> {
        private int created;

        @Override
        public TestObject get() {
            created++;
            return new TestObject();
        }

        int getCreated() {
            return created;
        }
    }

    @Test
    void constructor_nullFactory_throws() {
        assertThrows(IllegalArgumentException.class, () -> new ObjectPool<TestObject>(null, 0));
    }

    @Test
    void constructor_negativeSize_throws() {
        CountingFactory factory = new CountingFactory();
        assertThrows(IllegalArgumentException.class, () -> new ObjectPool<>(factory, -1));
        assertEquals(0, factory.getCreated());
    }

    @Test
    void constructor_preCreatesInitialObjects() {
        CountingFactory factory = new CountingFactory();
        ObjectPool<TestObject> pool = new ObjectPool<>(factory, 3);
        assertEquals(3, factory.getCreated());

        for (int i = 0; i < 3; i++) {
            pool.acquire();
        }
        assertEquals(3, factory.getCreated());
    }

    @Test
    void acquire_emptyPool_createsNew() {
        CountingFactory factory = new CountingFactory();
        ObjectPool<TestObject> pool = new ObjectPool<>(factory, 0);

        TestObject first = pool.acquire();
        TestObject second = pool.acquire();

        assertNotNull(first);
        assertNotSame(first, second);
        assertEquals(2, factory.getCreated());
    }

    @Test
    void acquire_setsActive() {
        ObjectPool<TestObject> pool = new ObjectPool<>(new CountingFactory(), 1);
        assertTrue(pool.acquire().isActive());
        assertTrue(pool.acquire().isActive());
    }

    @Test
    void release_thenAcquire_reusesSameInstance() {
        CountingFactory factory = new CountingFactory();
        ObjectPool<TestObject> pool = new ObjectPool<>(factory, 0);

        TestObject obj = pool.acquire();
        pool.release(obj);
        TestObject reused = pool.acquire();

        assertSame(obj, reused);
        assertTrue(reused.isActive());
        assertEquals(1, factory.getCreated());
    }

    @Test
    void release_callsOnReleaseAndDeactivates() {
        ObjectPool<TestObject> pool = new ObjectPool<>(new CountingFactory(), 0);
        TestObject obj = pool.acquire();

        pool.release(obj);

        assertEquals(1, obj.getReleaseCount());
        assertFalse(obj.isActive());
    }

    @Test
    void release_beyondInitialSize_isKept() {
        CountingFactory factory = new CountingFactory();
        ObjectPool<TestObject> pool = new ObjectPool<>(factory, 1);

        List<TestObject> firstBatch = List.of(pool.acquire(), pool.acquire(), pool.acquire());
        assertEquals(3, factory.getCreated());
        firstBatch.forEach(pool::release);

        Set<TestObject> secondBatch = Collections.newSetFromMap(new IdentityHashMap<>());
        for (int i = 0; i < 3; i++) {
            secondBatch.add(pool.acquire());
        }

        assertEquals(3, factory.getCreated());
        assertEquals(3, secondBatch.size());
        assertTrue(secondBatch.containsAll(firstBatch));
    }
}
