package com.game.spaceshooter.presentation.ui;

import com.game.spaceshooter.infrastructure.config.GameConfig;

import java.awt.*;
import javax.swing.JPanel;

public class GamePanel extends JPanel {

    public GamePanel() {
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

}
