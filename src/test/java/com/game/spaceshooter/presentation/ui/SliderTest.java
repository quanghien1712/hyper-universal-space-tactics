package com.game.spaceshooter.presentation.ui;

import com.game.spaceshooter.presentation.input.MouseInputs;
import org.junit.jupiter.api.Test;

import javax.swing.JPanel;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.*;

class SliderTest {

    private static final float EPS = 1e-6f;
    private static final int X = 100, Y = 100, WIDTH = 480, HEIGHT = 70;
    /** Knob range computed by Slider: x + 7 - 4 and x + 7 + (width - 10) - 10. */
    private static final int MIN_KNOB_X = X + 3;
    private static final int MAX_KNOB_X = X + WIDTH - 13;
    private static final int INSIDE_Y = Y + HEIGHT / 2;

    private final Component source = new JPanel();
    private final MouseInputs mouse = new MouseInputs();
    private final Slider slider = new Slider(X, Y, WIDTH, HEIGHT);

    private MouseEvent event(int id, int x, int y) {
        return new MouseEvent(source, id, System.currentTimeMillis(), 0, x, y, 1, false, MouseEvent.BUTTON1);
    }

    private void dragTo(int x, int y) {
        mouse.mouseDragged(event(MouseEvent.MOUSE_DRAGGED, x, y));
        slider.update(mouse);
    }

    private void moveTo(int x, int y) {
        mouse.mouseMoved(event(MouseEvent.MOUSE_MOVED, x, y));
        slider.update(mouse);
    }

    private static float percentAt(int knobX) {
        return (float) (knobX - MIN_KNOB_X) / (MAX_KNOB_X - MIN_KNOB_X);
    }

    @Test
    void initialPercent_isHalf() {
        assertEquals(0.5f, slider.getFilledPercent(), EPS);
    }

    @Test
    void pressOnSlider_updatesPercent() {
        int knobX = MIN_KNOB_X + (MAX_KNOB_X - MIN_KNOB_X) / 4;

        dragTo(knobX, INSIDE_Y);

        assertEquals(0.25f, slider.getFilledPercent(), EPS);
    }

    @Test
    void pressBeyondEnd_clampsPercent() {
        dragTo(X + WIDTH - 1, INSIDE_Y);
        assertEquals(1f, slider.getFilledPercent(), EPS);

        dragTo(X, INSIDE_Y);
        assertEquals(0f, slider.getFilledPercent(), EPS);
    }

    @Test
    void dragAcrossSlider_followsMouse() {
        dragTo(MIN_KNOB_X + 100, INSIDE_Y);
        assertEquals(percentAt(MIN_KNOB_X + 100), slider.getFilledPercent(), EPS);

        dragTo(MIN_KNOB_X + 300, INSIDE_Y);
        assertEquals(percentAt(MIN_KNOB_X + 300), slider.getFilledPercent(), EPS);
    }

    @Test
    void moveWithoutPress_doesNotChangePercent() {
        moveTo(MIN_KNOB_X + 50, INSIDE_Y);

        assertEquals(0.5f, slider.getFilledPercent(), EPS);
    }

    @Test
    void pressOutsideSlider_doesNotChangePercent() {
        dragTo(MIN_KNOB_X + 50, Y + HEIGHT + 20);

        assertEquals(0.5f, slider.getFilledPercent(), EPS);
    }

    @Test
    void releaseThenMove_keepsLastPercent() {
        dragTo(MIN_KNOB_X + 100, INSIDE_Y);
        mouse.mouseReleased(event(MouseEvent.MOUSE_RELEASED, MIN_KNOB_X + 100, INSIDE_Y));

        moveTo(MIN_KNOB_X + 400, INSIDE_Y);

        assertEquals(percentAt(MIN_KNOB_X + 100), slider.getFilledPercent(), EPS);
    }

    @Test
    void render_atAnyKnobPosition_doesNotThrow() {
        BufferedImage canvas = new BufferedImage(700, 300, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2D = canvas.createGraphics();
        try {
            assertDoesNotThrow(() -> slider.render(g2D));
            dragTo(X, INSIDE_Y);
            assertDoesNotThrow(() -> slider.render(g2D));
            dragTo(X + WIDTH - 1, INSIDE_Y);
            assertDoesNotThrow(() -> slider.render(g2D));
        } finally {
            g2D.dispose();
        }
    }
}
