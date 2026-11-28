package com.game.spaceshooter.presentation.controller;

import com.game.spaceshooter.application.port.AudioService;
import com.game.spaceshooter.infrastructure.sound.SoundConstants;
import com.game.spaceshooter.presentation.state.GameState;
import com.game.spaceshooter.presentation.state.State;
import com.game.spaceshooter.presentation.state.StateFactory;
import com.game.spaceshooter.presentation.state.StateManager;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GameFlowControllerTest {

    private static class FakeState implements State {
        private int updateCount;

        @Override
        public void update(double deltaTime) { updateCount++; }

        @Override
        public void render(Graphics2D g2D) {}
    }

    private static class FakeFactory implements StateFactory {
        private final Map<GameState, List<FakeState>> created = new EnumMap<>(GameState.class);

        @Override
        public State create(GameState gameState, StateManager stateManager) {
            FakeState state = new FakeState();
            created.computeIfAbsent(gameState, k -> new ArrayList<>()).add(state);
            return state;
        }

        List<FakeState> created(GameState gameState) {
            return created.get(gameState);
        }

        FakeState latest(GameState gameState) {
            List<FakeState> states = created(gameState);
            return states.get(states.size() - 1);
        }
    }

    private static class RecordingAudio implements AudioService {
        private final List<String> calls = new ArrayList<>();

        @Override
        public void playSfx(String name) { calls.add("playSfx:" + name); }

        @Override
        public void playBgm(String name) { calls.add("playBgm:" + name); }

        @Override
        public void stop(String name) { calls.add("stop:" + name); }

        @Override
        public void resetAll() { calls.add("resetAll"); }

        @Override
        public void stopAllSFX() { calls.add("stopAllSFX"); }

        @Override
        public void setSfxVolume(float percent) { calls.add("setSfxVolume:" + percent); }

        @Override
        public void setBgmVolume(float percent) { calls.add("setBgmVolume:" + percent); }

        @Override
        public void updateAllVolumes() { calls.add("updateAllVolumes"); }
    }

    private final FakeFactory factory = new FakeFactory();
    private final StateManager states = new StateManager(factory);
    private final RecordingAudio audio = new RecordingAudio();
    private final GameFlowController flow = new GameFlowController(states, audio);

    /** Updates the StateManager once and returns the fake state that received the update. */
    private FakeState currentState() {
        Map<FakeState, Integer> before = new HashMap<>();
        for (GameState gameState : GameState.values()) {
            for (FakeState state : factory.created(gameState)) {
                before.put(state, state.updateCount);
            }
        }
        states.update(0);
        for (Map.Entry<FakeState, Integer> entry : before.entrySet()) {
            if (entry.getKey().updateCount > entry.getValue()) return entry.getKey();
        }
        return null;
    }

    @Test
    void constructor_nullArguments_throw() {
        assertThrows(NullPointerException.class, () -> new GameFlowController(null, audio));
        assertThrows(NullPointerException.class, () -> new GameFlowController(states, null));
    }

    @Test
    void playMenuMusic_playsMenuBgm() {
        flow.playMenuMusic();

        assertEquals(List.of("playBgm:" + SoundConstants.BGM_MENU), audio.calls);
    }

    @Nested
    class StartAndRestart {

        @Test
        void startNewGame_switchesToPlayingAndPlaysBgm() {
            flow.startNewGame();

            assertSame(factory.latest(GameState.PLAYING), currentState());
            assertEquals(List.of(
                    "stop:" + SoundConstants.BGM_MENU,
                    "playBgm:" + SoundConstants.BGM_PLAYING), audio.calls);
        }

        @Test
        void startNewGame_createsFreshPlayingState() {
            FakeState oldPlaying = factory.latest(GameState.PLAYING);

            flow.startNewGame();

            assertEquals(2, factory.created(GameState.PLAYING).size());
            assertNotSame(oldPlaying, factory.latest(GameState.PLAYING));
        }

        @Test
        void restartGame_createsFreshPlayingStateAndRestartsMusic() {
            flow.startNewGame();
            FakeState firstGame = factory.latest(GameState.PLAYING);
            audio.calls.clear();

            flow.restartGame();

            assertNotSame(firstGame, factory.latest(GameState.PLAYING));
            assertSame(factory.latest(GameState.PLAYING), currentState());
            assertEquals(List.of("resetAll", "playBgm:" + SoundConstants.BGM_PLAYING), audio.calls);
        }
    }

    @Nested
    class PauseAndResume {

        @Test
        void pauseGame_stopsSfx() {
            flow.startNewGame();
            audio.calls.clear();

            flow.pauseGame();

            assertSame(factory.latest(GameState.PAUSED), currentState());
            assertEquals(List.of("stopAllSFX"), audio.calls);
        }

        @Test
        void resumeGame_returnsToSamePlayingStateAndRefreshesVolumes() {
            flow.startNewGame();
            FakeState playing = factory.latest(GameState.PLAYING);
            flow.pauseGame();
            audio.calls.clear();

            flow.resumeGame();

            assertSame(playing, currentState(), "resuming must not start a new game");
            assertEquals(2, factory.created(GameState.PLAYING).size());
            assertEquals(List.of("playBgm:" + SoundConstants.BGM_PLAYING, "updateAllVolumes"), audio.calls);
        }

        @Test
        void applyVolumes_forwardsToAudio() {
            flow.applyVolumes(0.25f, 0.75f);

            assertEquals(List.of("setSfxVolume:0.25", "setBgmVolume:0.75"), audio.calls);
        }
    }

    @Nested
    class MenuAndGameOver {

        @Test
        void returnToMenu_switchesToMenuAndPlaysMenuMusic() {
            flow.startNewGame();
            audio.calls.clear();

            flow.returnToMenu();

            assertSame(factory.latest(GameState.MENU), currentState());
            assertEquals(List.of("resetAll", "playBgm:" + SoundConstants.BGM_MENU), audio.calls);
        }

        @Test
        void showGameOver_storesScore() {
            flow.showGameOver(120);

            assertSame(factory.latest(GameState.GAME_OVER), currentState());
            assertEquals(120, states.getScore());
            assertEquals(120, states.getRecord());
        }

        @Test
        void showGameOver_lowerScore_keepsRecord() {
            flow.showGameOver(120);

            flow.showGameOver(50);

            assertEquals(50, states.getScore());
            assertEquals(120, states.getRecord());
        }

        @Test
        void showGameOver_doesNotTouchAudio() {
            flow.showGameOver(10);

            assertTrue(audio.calls.isEmpty());
        }

        @Test
        void showGameOver_negativeScore_throws() {
            assertThrows(IllegalArgumentException.class, () -> flow.showGameOver(-1));
        }
    }
}
