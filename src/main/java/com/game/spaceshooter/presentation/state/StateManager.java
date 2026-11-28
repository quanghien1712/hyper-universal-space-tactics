package com.game.spaceshooter.presentation.state;

import java.awt.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class StateManager {
    private final StateFactory stateFactory;
    private volatile State currentState;
    private final Map<GameState, State> states = new ConcurrentHashMap<>();
    private int score, record;

    public StateManager(StateFactory stateFactory) {
        this.stateFactory = stateFactory;
        initState();
        currentState = states.get(GameState.MENU);
    }

    private void initState() {
        for (GameState gameState : GameState.values()) {
            states.put(gameState, stateFactory.create(gameState, this));
        }
    }

    public void resetGame() {
        states.put(GameState.PLAYING, stateFactory.create(GameState.PLAYING, this));
    }

    public void update(double deltaTime) {
        currentState.update(deltaTime);
    }

    public void render(Graphics2D g2D) {
        State renderingState = currentState;
        if (renderingState.rendersOnTopOfGameplay()) {
            states.get(GameState.PLAYING).render(g2D);
            renderingState.render(g2D);
        }
        else renderingState.render(g2D);
    }

    public void changeState(GameState gameState) {
        currentState = states.get(gameState);
    }

    public boolean haveOverlay() {
        return currentState instanceof PausedOverlay;
    }

    public void setGameOverData(int score) {
        if (score < 0) throw new IllegalArgumentException("score must be nonnegative");
        this.score = score;
        this.record = Math.max(score, record);
    }

    public int getScore() {
        return score;
    }

    public int getRecord() {
        return record;
    }
}
