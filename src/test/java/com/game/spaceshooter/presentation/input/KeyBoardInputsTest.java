package com.game.spaceshooter.presentation.input;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.swing.JPanel;
import java.awt.Component;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class KeyBoardInputsTest {

    private final Component source = new JPanel();
    private KeyBoardInputs inputs;

    @BeforeEach
    void setUp() {
        inputs = new KeyBoardInputs();
    }

    private KeyEvent key(int id, int keyCode) {
        return new KeyEvent(source, id, System.currentTimeMillis(), 0, keyCode, KeyEvent.CHAR_UNDEFINED);
    }

    private void press(int keyCode) {
        inputs.keyPressed(key(KeyEvent.KEY_PRESSED, keyCode));
    }

    private void release(int keyCode) {
        inputs.keyReleased(key(KeyEvent.KEY_RELEASED, keyCode));
    }

    @Test
    void noKeyPressedInitially() {
        assertFalse(inputs.isKeyPressed(KeyEvent.VK_W));
        assertFalse(inputs.isKeyPressed(KeyEvent.VK_SPACE));
    }

    @Test
    void keyPressedThenReleased_updatesState() {
        press(KeyEvent.VK_W);
        assertTrue(inputs.isKeyPressed(KeyEvent.VK_W));

        release(KeyEvent.VK_W);
        assertFalse(inputs.isKeyPressed(KeyEvent.VK_W));
    }

    @Test
    void multipleKeys_areTrackedIndependently() {
        press(KeyEvent.VK_W);
        press(KeyEvent.VK_D);

        assertTrue(inputs.isKeyPressed(KeyEvent.VK_W));
        assertTrue(inputs.isKeyPressed(KeyEvent.VK_D));
        assertFalse(inputs.isKeyPressed(KeyEvent.VK_A));

        release(KeyEvent.VK_W);
        assertFalse(inputs.isKeyPressed(KeyEvent.VK_W));
        assertTrue(inputs.isKeyPressed(KeyEvent.VK_D));
    }

    @Test
    void pressingSameKeyTwice_singleReleaseClearsIt() {
        press(KeyEvent.VK_S);
        press(KeyEvent.VK_S);
        release(KeyEvent.VK_S);

        assertFalse(inputs.isKeyPressed(KeyEvent.VK_S));
    }

    @Test
    void releasingKeyNotPressed_doesNothing() {
        press(KeyEvent.VK_A);
        release(KeyEvent.VK_D);

        assertTrue(inputs.isKeyPressed(KeyEvent.VK_A));
        assertFalse(inputs.isKeyPressed(KeyEvent.VK_D));
    }

    @Test
    void keyTyped_doesNotChangeState() {
        inputs.keyTyped(new KeyEvent(source, KeyEvent.KEY_TYPED, System.currentTimeMillis(), 0,
                KeyEvent.VK_UNDEFINED, 'w'));

        assertFalse(inputs.isKeyPressed(KeyEvent.VK_W));
        assertFalse(inputs.isKeyPressed(KeyEvent.VK_UNDEFINED));
    }

    @Test
    void clearKeyPressed_removesAllKeys() {
        press(KeyEvent.VK_W);
        press(KeyEvent.VK_A);
        press(KeyEvent.VK_SHIFT);

        inputs.clearKeyPressed();

        assertFalse(inputs.isKeyPressed(KeyEvent.VK_W));
        assertFalse(inputs.isKeyPressed(KeyEvent.VK_A));
        assertFalse(inputs.isKeyPressed(KeyEvent.VK_SHIFT));
    }

    @Test
    void keysCanBePressedAgainAfterClear() {
        press(KeyEvent.VK_W);
        inputs.clearKeyPressed();
        press(KeyEvent.VK_W);

        assertTrue(inputs.isKeyPressed(KeyEvent.VK_W));
    }

    @Test
    void concurrentWriteAndRead_doesNotThrow() throws InterruptedException {
        int iterations = 20_000;
        CountDownLatch start = new CountDownLatch(1);
        AtomicReference<Throwable> failure = new AtomicReference<>();

        Thread writer = new Thread(() -> {
            try {
                start.await();
                for (int i = 0; i < iterations; i++) {
                    int code = i % 200;
                    press(code);
                    release(code);
                }
            } catch (Throwable t) {
                failure.compareAndSet(null, t);
            }
        });
        Thread reader = new Thread(() -> {
            try {
                start.await();
                for (int i = 0; i < iterations; i++) {
                    inputs.isKeyPressed(i % 200);
                    if (i % 1000 == 0) inputs.clearKeyPressed();
                }
            } catch (Throwable t) {
                failure.compareAndSet(null, t);
            }
        });

        List<Thread> threads = new ArrayList<>(List.of(writer, reader));
        threads.forEach(Thread::start);
        start.countDown();
        for (Thread t : threads) t.join(10_000);

        assertNull(failure.get(), () -> "Concurrent access failed: " + failure.get());
        for (int code = 0; code < 200; code++) {
            assertFalse(inputs.isKeyPressed(code), "key " + code + " should be released");
        }
    }
}
