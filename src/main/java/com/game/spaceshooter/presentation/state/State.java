package com.game.spaceshooter.presentation.state;

import java.awt.*;

public interface State {
    void update(double deltaTime);
    void render(Graphics2D g2D);
    default boolean rendersOnTopOfGameplay() {
        return false;
    }
}
