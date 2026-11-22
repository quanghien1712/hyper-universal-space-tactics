package com.game.spaceshooter.presentation.ui;

import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.awt.event.WindowAdapter;
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
        window.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                gamePanel.stopGameLoop();
            }
        });
        window.setVisible(true);
        window.setFocusable(true);
        window.requestFocusInWindow();
        window.addWindowFocusListener(new WindowFocusListener() {
            @Override
            public void windowGainedFocus(WindowEvent e) {

            }

            @Override
            public void windowLostFocus(WindowEvent e) {
                gamePanel.windowFocusLost();
            }
        });
    }
}
