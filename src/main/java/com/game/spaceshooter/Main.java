package com.game.spaceshooter;

import com.game.spaceshooter.presentation.ui.GamePanel;
import com.game.spaceshooter.presentation.ui.GameWindow;

public class Main {
    public static void main(String[] args) {
        System.setProperty("sun.java2d.uiScale", "1");
        System.setProperty("sun.java2d.opengl", "true");
        new GameWindow(new GamePanel());
    }
}
