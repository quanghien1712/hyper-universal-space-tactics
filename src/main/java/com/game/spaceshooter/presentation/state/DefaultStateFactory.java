package com.game.spaceshooter.presentation.state;

import com.game.spaceshooter.application.core.GameEngine;
import com.game.spaceshooter.application.port.AudioService;
import com.game.spaceshooter.presentation.controller.GameFlowController;

import java.util.Objects;

public class DefaultStateFactory implements StateFactory {
    private final GameEngine gameEngine;
    private final AudioService audio;
    private GameFlowController actions;

    public DefaultStateFactory(GameEngine gameEngine, AudioService audio) {
        this.audio = Objects.requireNonNull(audio, "audio is required");
        this.gameEngine = Objects.requireNonNull(gameEngine, "gameEngine is required");
    }

    @Override
    public State create(GameState gameState, StateManager stateManager) {
        if (actions == null) actions = new GameFlowController(stateManager, audio);
        return switch (gameState) {
            case PLAYING -> new Playing(gameEngine, stateManager, actions, audio);
            case MENU -> new Menu(gameEngine, actions);
            case PAUSED -> new PausedOverlay(gameEngine, actions);
            case GAME_OVER -> new GameOver(gameEngine, actions, stateManager);
        };
    }
}
