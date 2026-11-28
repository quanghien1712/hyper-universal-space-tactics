package com.game.spaceshooter.application.core;

import com.game.spaceshooter.application.port.AudioService;
import com.game.spaceshooter.infrastructure.config.GameConfig;
import com.game.spaceshooter.presentation.state.DefaultStateFactory;
import com.game.spaceshooter.presentation.state.StateManager;
import com.game.spaceshooter.presentation.ui.GamePanel;
import com.game.spaceshooter.presentation.ui.GameWindow;

import java.util.Objects;

public class GameEngine implements Runnable{
    private volatile boolean running = true;
    private final AudioService audio;
    private GamePanel gamePanel;
    private Thread gameThread;
    private StateManager stateManager;


    public GameEngine(AudioService audio) {
        this.audio = Objects.requireNonNull(audio, "audio is required");
        initClasses();
        startGameThread();
    }

    private void initClasses() {
        gamePanel = new GamePanel(this);
        stateManager = new StateManager(new DefaultStateFactory(this, audio));
        new GameWindow(gamePanel);
        gamePanel.initWindow();
    }

    private void startGameThread() {
        gameThread = new Thread(this, "shooting-game-loop");
        gameThread.start();
    }

    @Override
    public void run() {
        double timePerFrame = 1_000_000_000.0 / GameConfig.FPS;
        double timePerUpdate = 1_000_000_000.0 / GameConfig.UPS;
        long lastTime = System.nanoTime();
        double deltaF = 0;
        double deltaU = 0;
        try {
            while (running && !Thread.currentThread().isInterrupted()) {
                long currentTime = System.nanoTime();
                long elapsed = currentTime - lastTime;
                deltaF += elapsed / timePerFrame;
                deltaU += elapsed / timePerUpdate;
                lastTime = currentTime;
                if (deltaU >= 1) {
                    stateManager.update(timePerUpdate / 1_000_000_000.0);
                    deltaU--;
                }
                if (!running || Thread.currentThread().isInterrupted()) break;
                if (deltaF >= 1) {
                    gamePanel.repaint();
                    deltaF--;
                }
                if (deltaF < 1 && deltaU < 1) {
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        } finally {
            running = false;
        }
    }

    public void stop() {
        running = false;
        if (gameThread != null) gameThread.interrupt();
    }

    public StateManager getStateManager() {
        return stateManager;
    }

    public GamePanel getGamePanel() {
        return this.gamePanel;
    }
}
