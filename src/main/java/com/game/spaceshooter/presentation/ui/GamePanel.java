package com.game.spaceshooter.presentation.ui;

import com.game.spaceshooter.application.core.GameEngine;
import com.game.spaceshooter.infrastructure.config.GameConfig;

import java.awt.*;
import javax.swing.JPanel;

public class GamePanel extends JPanel {
    private GameEngine gameEngine;

    public GamePanel(GameEngine gameEngine) {
        this.gameEngine = gameEngine;
        setDefaultPanel();
    }

    private void setDefaultPanel() {
        setPreferredSize(new Dimension(GameConfig.PANEL_WIDTH, GameConfig.PANEL_HEIGHT));
        setDoubleBuffered(true);
    }

    public void paintComponent(Graphics g) {
        Graphics2D g2D = (Graphics2D) g;
        super.paintComponent(g2D);
    }

    public void stopGameLoop() {
        gameEngine.stop();
    }

}
