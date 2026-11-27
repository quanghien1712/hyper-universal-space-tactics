package com.game.spaceshooter.infrastructure.sound;

import com.game.spaceshooter.infrastructure.utils.ResourceLoadException;
import com.game.spaceshooter.infrastructure.utils.ResourceLoader;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class SoundTest {

    private static final String EXISTING = "/sound/Boost.wav";
    private static final String MISSING = "/sound/DoesNotExist.wav";

    private static class RecordingHandler extends Handler {
        private final List<LogRecord> records = new ArrayList<>();

        @Override
        public void publish(LogRecord record) { records.add(record); }

        @Override
        public void flush() {}

        @Override
        public void close() {}
    }

    private static void assertAllOperationsSafe(Sound sound) {
        assertDoesNotThrow(() -> {
            sound.setVolume(0f);
            sound.play();
            sound.loop();
            sound.stop();
            sound.reset();
        });
    }

    /** Clips can only be opened when the machine running the tests has an audio device. */
    private static void assumeAudioAvailable() {
        assumeTrue(new Sound(EXISTING, 1).getClipCount() > 0, "no audio device available");
    }

    @Test
    void constructor_nonPositivePoolSize_throws() {
        assertThrows(IllegalArgumentException.class, () -> new Sound(EXISTING, 0));
        assertThrows(IllegalArgumentException.class, () -> new Sound(EXISTING, -1));
    }

    @Test
    void missingFile_doesNotThrowAndPlayIsNoop() {
        Sound sound = assertDoesNotThrow(() -> new Sound(MISSING, 3));

        assertEquals(0, sound.getClipCount());
        assertAllOperationsSafe(sound);
    }

    @Test
    void missingFile_warnsThroughResourceLoader() {
        Logger logger = Logger.getLogger(ResourceLoader.class.getName());
        RecordingHandler handler = new RecordingHandler();
        logger.addHandler(handler);
        try {
            new Sound(MISSING, 3);
        } finally {
            logger.removeHandler(handler);
        }

        assertEquals(1, handler.records.size(), "loading stops after the first failure");
        LogRecord record = handler.records.get(0);
        assertEquals(Level.WARNING, record.getLevel());
        assertTrue(record.getMessage().contains(MISSING));
        assertTrue(record.getMessage().contains("sound will be muted"));
        assertInstanceOf(ResourceLoadException.class, record.getThrown());
    }

    @Test
    void relativePath_loadsNothing() {
        Sound sound = assertDoesNotThrow(() -> new Sound("sound/Boost.wav", 1));

        assertEquals(0, sound.getClipCount());
        assertAllOperationsSafe(sound);
    }

    @Test
    void existingFile_loadsOneClipPerPoolSlot() {
        assumeAudioAvailable();

        assertEquals(3, new Sound(EXISTING, 3).getClipCount());
    }

    @Test
    void existingFile_operationsDoNotThrow() {
        Sound sound = assertDoesNotThrow(() -> new Sound(EXISTING, 2));

        assertAllOperationsSafe(sound);
    }

    @Test
    void play_moreTimesThanPoolSize_doesNotThrow() {
        Sound sound = new Sound(EXISTING, 2);
        sound.setVolume(0f);

        assertDoesNotThrow(() -> {
            for (int i = 0; i < 5; i++) {
                sound.play();
            }
        });
        sound.stop();
    }

    @Test
    void setVolume_outOfRange_isClampedWithoutThrowing() {
        Sound sound = new Sound(EXISTING, 1);

        assertDoesNotThrow(() -> {
            sound.setVolume(-1f);
            sound.setVolume(2f);
            sound.setVolume(0.5f);
        });
    }
}
