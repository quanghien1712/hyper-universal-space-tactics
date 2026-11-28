package com.game.spaceshooter;

import com.game.spaceshooter.application.core.GameEngine;
import com.game.spaceshooter.infrastructure.sound.SoundManager;

public class Main {
    public static void main(String[] args) {
        System.setProperty("sun.java2d.uiScale", "1");
        System.setProperty("sun.java2d.opengl", "true");
        new GameEngine(SoundManager.getInstance());
    }
}
