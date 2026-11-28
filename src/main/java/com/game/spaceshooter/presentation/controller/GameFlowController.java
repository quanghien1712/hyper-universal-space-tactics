package com.game.spaceshooter.presentation.controller;

import com.game.spaceshooter.application.port.AudioService;
import com.game.spaceshooter.infrastructure.sound.SoundConstants;
import com.game.spaceshooter.presentation.state.GameState;
import com.game.spaceshooter.presentation.state.StateManager;

import java.util.Objects;

public final class GameFlowController {
    private final StateManager states;
    private final AudioService audio;

    public GameFlowController(StateManager states, AudioService audio) {
        this.states = Objects.requireNonNull(states, "states is required");
        this.audio = Objects.requireNonNull(audio, "audio is required");
    }

    public void playMenuMusic() {
        audio.playBgm(SoundConstants.BGM_MENU);
    }

    public void startNewGame() {
        states.resetGame();
        states.changeState(GameState.PLAYING);
        audio.stop(SoundConstants.BGM_MENU);
        audio.playBgm(SoundConstants.BGM_PLAYING);
    }

    public void returnToMenu() {
        states.changeState(GameState.MENU);
        audio.resetAll();
        playMenuMusic();
    }

    public void restartGame() {
        states.resetGame();
        states.changeState(GameState.PLAYING);
        audio.resetAll();
        audio.playBgm(SoundConstants.BGM_PLAYING);
    }

    public void pauseGame() {
        states.changeState(GameState.PAUSED);
        audio.stopAllSFX();
    }

    public void resumeGame() {
        states.changeState(GameState.PLAYING);
        audio.playBgm(SoundConstants.BGM_PLAYING);
        audio.updateAllVolumes();
    }

    public void showGameOver(int displayScore) {
        states.setGameOverData(displayScore);
        states.changeState(GameState.GAME_OVER);
    }

    public void applyVolumes(float sfx, float music) {
        audio.setSfxVolume(sfx);
        audio.setBgmVolume(music);
    }

    public void exitGame() {
        System.exit(0);
    }
}
