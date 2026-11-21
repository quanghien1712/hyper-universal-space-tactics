package com.game.spaceshooter.application.core;

import com.game.spaceshooter.infrastructure.config.GameConfig;
import com.game.spaceshooter.presentation.ui.GamePanel;
import com.game.spaceshooter.presentation.ui.GameWindow;

public class GameEngine implements Runnable{
    private volatile boolean running = true;
    private GamePanel gamePanel;
    private Thread gameThread;


    public GameEngine() {
        initClasses();
        startGameThread();
    }

    private void initClasses() {
        gamePanel = new GamePanel(this);
        new GameWindow(gamePanel);
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

    public GamePanel getGamePanel() {
        return this.gamePanel;
    }
}
