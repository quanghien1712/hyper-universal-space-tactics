package com.game.spaceshooter.presentation.input;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashSet;
import java.util.Set;

public class KeyBoardInputs implements KeyListener {
    private final Set<Integer> pressedKey = new HashSet<>();
    private final Object lock = new Object();

    public void clearKeyPressed() {
        synchronized (lock) {
            pressedKey.clear();
        }
    }

    public boolean isKeyPressed(int keyCode) {
        synchronized (lock) {
            return pressedKey.contains(keyCode);
        }
    }

    @Override
    public void keyTyped(KeyEvent event) {}

    @Override
    public void keyPressed(KeyEvent event) {
        synchronized (lock) {
            pressedKey.add(event.getKeyCode());
        }
    }

    @Override
    public void keyReleased(KeyEvent event) {
        synchronized (lock) {
            pressedKey.remove(event.getKeyCode());
        }
    }
}
