package com.game.spaceshooter.presentation.ui;

import javax.swing.JFrame;

public class GameWindow {
    private JFrame window;

    public GameWindow(GamePanel gamePanel) {
        window = new JFrame();

        window.setTitle("Hyper Universal Space Tactics");
        window.setResizable(false);
        window.add(gamePanel);

        window.pack();
        window.setLocationRelativeTo(null);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setVisible(true);
        window.setFocusable(true);
        window.requestFocusInWindow();
    }
}
