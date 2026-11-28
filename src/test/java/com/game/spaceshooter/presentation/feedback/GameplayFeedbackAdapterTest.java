package com.game.spaceshooter.presentation.feedback;

import com.game.spaceshooter.application.port.AudioService;
import com.game.spaceshooter.domain.effect.EffectType;
import com.game.spaceshooter.infrastructure.sound.SoundConstants;
import com.game.spaceshooter.infrastructure.utils.Vector2D;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GameplayFeedbackAdapterTest {
    private record SpawnedEffect(EffectType type, Vector2D position) {}

    private final RecordingAudio audio = new RecordingAudio();
    private final List<SpawnedEffect> effects = new ArrayList<>();
    private final GameplayFeedbackAdapter feedback = new GameplayFeedbackAdapter(
            audio, (type, position) -> effects.add(new SpawnedEffect(type, position)), () -> {});

    @Test
    void onPlayerShot_playsSfx() {
        feedback.onPlayerShot();

        assertEquals(List.of(SoundConstants.SFX_PLASMA), audio.sounds);
    }

    @Test
    void onEnemyDefeated_spawnsDeathEffectAndPlaysSfx() {
        Vector2D position = new Vector2D(100, 200);

        feedback.onEnemyDefeated(position);

        assertEquals(List.of(new SpawnedEffect(EffectType.DEATH, new Vector2D(100, 200))), effects);
        assertEquals(List.of(SoundConstants.SFX_EXPLOSION), audio.sounds);
    }

    private static class RecordingAudio implements AudioService {
        private final List<String> sounds = new ArrayList<>();

        @Override
        public void playSfx(String name) { sounds.add(name); }

        @Override
        public void playBgm(String name) {}

        @Override
        public void stop(String name) {}

        @Override
        public void resetAll() {}

        @Override
        public void stopAllSFX() {}

        @Override
        public void setSfxVolume(float percent) {}

        @Override
        public void setBgmVolume(float percent) {}

        @Override
        public void updateAllVolumes() {}
    }
}
