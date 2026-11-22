package com.game.spaceshooter.presentation.ui;

import com.game.spaceshooter.application.core.GameEngine;
import com.game.spaceshooter.infrastructure.config.GameConfig;
import com.game.spaceshooter.presentation.input.KeyBoardInputs;
import com.game.spaceshooter.presentation.input.MouseInputs;

import java.awt.*;
import javax.swing.JPanel;

public class GamePanel extends JPanel {
    private MouseInputs mouseInputs;
    private KeyBoardInputs keyboardInputs;
    private GameEngine gameEngine;

    public GamePanel(GameEngine gameEngine) {
        this.gameEngine = gameEngine;
        setDefaultPanel();
        mouseInputs = new MouseInputs();
        keyboardInputs = new KeyBoardInputs();
        addKeyListener(keyboardInputs);
        addMouseListener(mouseInputs);
        addMouseMotionListener(mouseInputs);
    }

    private void setDefaultPanel() {
        setPreferredSize(new Dimension(GameConfig.PANEL_WIDTH, GameConfig.PANEL_HEIGHT));
        setDoubleBuffered(true);
    }

    public void paintComponent(Graphics g) {
        Graphics2D g2D = (Graphics2D) g;
        super.paintComponent(g2D);
    }

    public void windowFocusLost() {
        keyboardInputs.clearKeyPressed();
    }

    public void stopGameLoop() {
        gameEngine.stop();
    }

    public MouseInputs getMouseInputs() {
        return mouseInputs;
    }

    public KeyBoardInputs getKeyboardInputs() {
        return keyboardInputs;
    }

}
