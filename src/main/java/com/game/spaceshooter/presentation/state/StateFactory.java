package com.game.spaceshooter.presentation.state;

public interface StateFactory {
    State create(GameState gameState, StateManager stateManager);
}
