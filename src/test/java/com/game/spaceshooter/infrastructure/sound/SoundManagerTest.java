package com.game.spaceshooter.infrastructure.sound;

import com.game.spaceshooter.application.port.AudioService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class SoundManagerTest {

    private static final float EPS = 1e-6f;

    private final SoundManager manager = SoundManager.getInstance();

    @AfterEach
    void restoreSharedState() {
        manager.resetAll();
        manager.setSfxVolume(SoundConstants.DEFAULT_SFX_VOLUME);
        manager.setBgmVolume(SoundConstants.DEFAULT_BGM_VOLUME);
    }

    @Nested
    class Singleton {

        @Test
        void getInstance_returnsSameInstance() {
            assertSame(SoundManager.getInstance(), SoundManager.getInstance());
        }

        @Test
        void getInstance_fromManyThreads_returnsSameInstance() throws InterruptedException {
            int threads = 8;
            Set<SoundManager> instances = ConcurrentHashMap.newKeySet();
            CountDownLatch start = new CountDownLatch(1);
            ExecutorService executor = Executors.newFixedThreadPool(threads);
            for (int i = 0; i < threads; i++) {
                executor.submit(() -> {
                    start.await();
                    instances.add(SoundManager.getInstance());
                    return null;
                });
            }

            start.countDown();
            executor.shutdown();
            assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS));

            assertEquals(Set.of(manager), instances);
        }

        @Test
        void implementsAudioService() {
            assertInstanceOf(AudioService.class, manager);
        }
    }

    @Nested
    class Volume {

        @Test
        void volumes_startAtDefault() {
            assertEquals(SoundConstants.DEFAULT_SFX_VOLUME, manager.getSfxVolume(), EPS);
            assertEquals(SoundConstants.DEFAULT_BGM_VOLUME, manager.getBgmVolume(), EPS);
        }

        @Test
        void setSfxVolume_storesValue() {
            manager.setSfxVolume(0.3f);

            assertEquals(0.3f, manager.getSfxVolume(), EPS);
        }

        @Test
        void setBgmVolume_storesValue() {
            manager.setBgmVolume(0.8f);

            assertEquals(0.8f, manager.getBgmVolume(), EPS);
        }

        @Test
        void setSfxVolume_outOfRange_isClamped() {
            manager.setSfxVolume(5f);
            assertEquals(1f, manager.getSfxVolume(), EPS);

            manager.setSfxVolume(-1f);
            assertEquals(0f, manager.getSfxVolume(), EPS);
        }

        @Test
        void setBgmVolume_outOfRange_isClamped() {
            manager.setBgmVolume(5f);
            assertEquals(1f, manager.getBgmVolume(), EPS);

            manager.setBgmVolume(-1f);
            assertEquals(0f, manager.getBgmVolume(), EPS);
        }

        @Test
        void sfxAndBgmVolumes_areIndependent() {
            manager.setSfxVolume(0.1f);
            manager.setBgmVolume(0.9f);

            assertEquals(0.1f, manager.getSfxVolume(), EPS);
            assertEquals(0.9f, manager.getBgmVolume(), EPS);
        }

        @Test
        void updateAllVolumes_keepsStoredValues() {
            manager.setSfxVolume(0.2f);
            manager.setBgmVolume(0.7f);

            assertDoesNotThrow(manager::updateAllVolumes);

            assertEquals(0.2f, manager.getSfxVolume(), EPS);
            assertEquals(0.7f, manager.getBgmVolume(), EPS);
        }
    }

    @Nested
    class Playback {

        @Test
        void unknownSoundName_isIgnored() {
            assertDoesNotThrow(() -> {
                manager.playSfx("UnknownSfx");
                manager.playBgm("UnknownMusic");
                manager.stop("UnknownSfx");
            });
        }

        @Test
        void playKnownSounds_thenStop_doesNotThrow() {
            manager.setSfxVolume(0f);
            manager.setBgmVolume(0f);

            assertDoesNotThrow(() -> {
                manager.playSfx(SoundConstants.SFX_BOOST);
                manager.playBgm(SoundConstants.BGM_MENU);
                manager.stopAllSFX();
                manager.stop(SoundConstants.BGM_MENU);
            });
        }

        @Test
        void resetAll_doesNotThrow() {
            assertDoesNotThrow(manager::resetAll);
        }
    }
}
