package com.game.spaceshooter.presentation.state;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class StateManagerTest {

    private static class FakeState implements State {
        private final GameState type;
        private final boolean onTop;
        private final List<String> renderLog;
        private int updateCount;
        private double lastDeltaTime = Double.NaN;

        FakeState(GameState type, boolean onTop, List<String> renderLog) {
            this.type = type;
            this.onTop = onTop;
            this.renderLog = renderLog;
        }

        @Override
        public void update(double deltaTime) {
            updateCount++;
            lastDeltaTime = deltaTime;
        }

        @Override
        public void render(Graphics2D g2D) {
            renderLog.add(type.name());
        }

        @Override
        public boolean rendersOnTopOfGameplay() {
            return onTop;
        }
    }

    private static class FakeFactory implements StateFactory {
        private final List<String> renderLog = new ArrayList<>();
        private final Map<GameState, List<FakeState>> created = new EnumMap<>(GameState.class);
        private final Map<GameState, StateManager> managers = new EnumMap<>(GameState.class);

        @Override
        public State create(GameState gameState, StateManager stateManager) {
            boolean onTop = gameState == GameState.PAUSED || gameState == GameState.GAME_OVER;
            FakeState state = new FakeState(gameState, onTop, renderLog);
            created.computeIfAbsent(gameState, k -> new ArrayList<>()).add(state);
            managers.put(gameState, stateManager);
            return state;
        }

        FakeState latest(GameState gameState) {
            List<FakeState> list = created.get(gameState);
            return list.get(list.size() - 1);
        }

        int createdCount(GameState gameState) {
            return created.getOrDefault(gameState, List.of()).size();
        }
    }

    private FakeFactory factory;
    private StateManager manager;
    private Graphics2D g2D;

    @BeforeEach
    void setUp() {
        factory = new FakeFactory();
        manager = new StateManager(factory);
        g2D = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB).createGraphics();
    }

    @Nested
    class Construction {
        @Test
        void createsOneStatePerGameState() {
            for (GameState gameState : GameState.values()) {
                assertEquals(1, factory.createdCount(gameState), gameState.name());
            }
        }

        @Test
        void passesItselfToFactory() {
            for (GameState gameState : GameState.values()) {
                assertSame(manager, factory.managers.get(gameState), gameState.name());
            }
        }

        @Test
        void startsAtMenu() {
            manager.update(0.5);

            assertEquals(1, factory.latest(GameState.MENU).updateCount);
            assertEquals(0, factory.latest(GameState.PLAYING).updateCount);
        }

        @Test
        void nullFactory_throws() {
            assertThrows(NullPointerException.class, () -> new StateManager(null));
        }
    }

    @Nested
    class UpdateAndChangeState {
        @Test
        void update_delegatesDeltaTimeToCurrentState() {
            manager.update(0.016);

            FakeState menu = factory.latest(GameState.MENU);
            assertEquals(1, menu.updateCount);
            assertEquals(0.016, menu.lastDeltaTime, 1e-12);
        }

        @Test
        void changeState_switchesCurrentState() {
            manager.changeState(GameState.PLAYING);
            manager.update(1.0);

            assertEquals(1, factory.latest(GameState.PLAYING).updateCount);
            assertEquals(0, factory.latest(GameState.MENU).updateCount);
        }

        @Test
        void changeState_reusesExistingInstance() {
            manager.changeState(GameState.PLAYING);
            manager.update(1.0);
            manager.changeState(GameState.MENU);
            manager.changeState(GameState.PLAYING);
            manager.update(1.0);

            assertEquals(1, factory.createdCount(GameState.PLAYING));
            assertEquals(2, factory.latest(GameState.PLAYING).updateCount);
        }

        @Test
        void changeState_toEveryState_updatesOnlyThatState() {
            for (GameState target : GameState.values()) {
                manager.changeState(target);
                manager.update(1.0);
            }

            for (GameState gameState : GameState.values()) {
                assertEquals(1, factory.latest(gameState).updateCount, gameState.name());
            }
        }
    }

    @Nested
    class Render {
        @Test
        void render_normalState_rendersOnlyItself() {
            manager.render(g2D);

            assertEquals(List.of("MENU"), factory.renderLog);
        }

        @Test
        void render_playing_rendersOnlyPlaying() {
            manager.changeState(GameState.PLAYING);
            manager.render(g2D);

            assertEquals(List.of("PLAYING"), factory.renderLog);
        }

        @Test
        void render_overlayState_rendersPlayingFirstThenOverlay() {
            manager.changeState(GameState.PAUSED);
            manager.render(g2D);

            assertEquals(List.of("PLAYING", "PAUSED"), factory.renderLog);
        }

        @Test
        void render_gameOver_rendersPlayingBehind() {
            manager.changeState(GameState.GAME_OVER);
            manager.render(g2D);

            assertEquals(List.of("PLAYING", "GAME_OVER"), factory.renderLog);
        }
    }

    @Nested
    class ResetGame {
        @Test
        void resetGame_createsNewPlayingState() {
            FakeState oldPlaying = factory.latest(GameState.PLAYING);

            manager.resetGame();

            assertEquals(2, factory.createdCount(GameState.PLAYING));
            assertNotSame(oldPlaying, factory.latest(GameState.PLAYING));
        }

        @Test
        void resetGame_doesNotRecreateOtherStates() {
            manager.resetGame();

            assertEquals(1, factory.createdCount(GameState.MENU));
            assertEquals(1, factory.createdCount(GameState.PAUSED));
            assertEquals(1, factory.createdCount(GameState.GAME_OVER));
        }

        @Test
        void resetGame_thenChangeToPlaying_usesNewInstance() {
            FakeState oldPlaying = factory.latest(GameState.PLAYING);

            manager.resetGame();
            manager.changeState(GameState.PLAYING);
            manager.update(1.0);

            assertEquals(0, oldPlaying.updateCount);
            assertEquals(1, factory.latest(GameState.PLAYING).updateCount);
        }

        @Test
        void resetGame_overlayRendersNewPlayingBehind() {
            manager.changeState(GameState.PAUSED);
            manager.resetGame();
            manager.render(g2D);

            assertEquals(List.of("PLAYING", "PAUSED"), factory.renderLog);
        }

        @Test
        void resetGame_keepsCurrentState() {
            manager.resetGame();
            manager.update(1.0);

            assertEquals(1, factory.latest(GameState.MENU).updateCount);
        }
    }

    @Nested
    class ScoreAndRecord {
        @Test
        void initialScoreAndRecord_areZero() {
            assertEquals(0, manager.getScore());
            assertEquals(0, manager.getRecord());
        }

        @Test
        void setGameOverData_storesScoreAndRecord() {
            manager.setGameOverData(150);

            assertEquals(150, manager.getScore());
            assertEquals(150, manager.getRecord());
        }

        @Test
        void setGameOverData_zero_isAllowed() {
            assertDoesNotThrow(() -> manager.setGameOverData(0));
            assertEquals(0, manager.getScore());
        }

        @Test
        void setGameOverData_negative_throws() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> manager.setGameOverData(-1));
            assertTrue(ex.getMessage().contains("score"));
        }

        @Test
        void setGameOverData_negative_keepsPreviousValues() {
            manager.setGameOverData(80);

            assertThrows(IllegalArgumentException.class, () -> manager.setGameOverData(-5));

            assertEquals(80, manager.getScore());
            assertEquals(80, manager.getRecord());
        }

        @Test
        void record_keepsHighestScore() {
            manager.setGameOverData(100);
            manager.setGameOverData(40);

            assertEquals(40, manager.getScore());
            assertEquals(100, manager.getRecord());

            manager.setGameOverData(250);

            assertEquals(250, manager.getScore());
            assertEquals(250, manager.getRecord());
        }

        @Test
        void resetGame_doesNotClearRecord() {
            manager.setGameOverData(300);
            manager.resetGame();

            assertEquals(300, manager.getRecord());
        }
    }
}
