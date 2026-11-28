package com.game.spaceshooter.presentation.ui;

import com.game.spaceshooter.presentation.input.MouseInputs;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import javax.swing.JPanel;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.*;

class ButtonTest {

    private static final int X = 100, Y = 100, WIDTH = 200, HEIGHT = 60;
    private static final int INSIDE_X = 150, INSIDE_Y = 120;
    private static final int OUTSIDE_X = 500, OUTSIDE_Y = 500;

    private final Component source = new JPanel();
    private final MouseInputs mouse = new MouseInputs();
    private final Button button = new Button(X, Y, WIDTH, HEIGHT, "Start");

    private MouseEvent event(int id, int x, int y) {
        return new MouseEvent(source, id, System.currentTimeMillis(), 0, x, y, 1, false, MouseEvent.BUTTON1);
    }

    private void moveTo(int x, int y) {
        mouse.mouseMoved(event(MouseEvent.MOUSE_MOVED, x, y));
        button.update(mouse);
    }

    private void dragTo(int x, int y) {
        mouse.mouseDragged(event(MouseEvent.MOUSE_DRAGGED, x, y));
        button.update(mouse);
    }

    private void press() {
        mouse.mousePressed(event(MouseEvent.MOUSE_PRESSED, 0, 0));
        button.update(mouse);
    }

    private void release() {
        mouse.mouseReleased(event(MouseEvent.MOUSE_RELEASED, 0, 0));
        button.update(mouse);
    }

    @Nested
    class Click {

        @Test
        void pressAndReleaseInside_isClicked() {
            moveTo(INSIDE_X, INSIDE_Y);
            press();
            assertFalse(button.isClicked(), "click only happens on release");

            release();

            assertTrue(button.isClicked());
        }

        @Test
        void click_lastsOnlyOneUpdate() {
            moveTo(INSIDE_X, INSIDE_Y);
            press();
            release();

            button.update(mouse);

            assertFalse(button.isClicked());
        }

        @Test
        void holdingPress_notClickedUntilRelease() {
            moveTo(INSIDE_X, INSIDE_Y);
            press();
            button.update(mouse);
            button.update(mouse);

            assertFalse(button.isClicked());
        }

        @Test
        void releaseOutside_notClicked() {
            moveTo(INSIDE_X, INSIDE_Y);
            press();
            dragTo(OUTSIDE_X, OUTSIDE_Y);

            release();

            assertFalse(button.isClicked());
        }

        @Test
        void pressOutsideReleaseInside_notClicked() {
            moveTo(OUTSIDE_X, OUTSIDE_Y);
            press();
            dragTo(INSIDE_X, INSIDE_Y);

            release();

            assertFalse(button.isClicked());
        }

        @Test
        void hoverWithoutPress_notClicked() {
            moveTo(INSIDE_X, INSIDE_Y);
            button.update(mouse);

            assertFalse(button.isClicked());
        }

        @Test
        void secondClick_afterFirst_isClickedAgain() {
            moveTo(INSIDE_X, INSIDE_Y);
            press();
            release();

            press();
            release();

            assertTrue(button.isClicked());
        }

        @Test
        void resetButton_cancelsPendingClick() {
            moveTo(INSIDE_X, INSIDE_Y);
            press();

            button.resetButton();
            release();

            assertFalse(button.isClicked());
        }
    }

    @Nested
    class Render {

        private final BufferedImage canvas = new BufferedImage(400, 300, BufferedImage.TYPE_INT_ARGB);

        @Test
        void render_normalAndHovered_doesNotThrow() {
            Graphics2D g2D = canvas.createGraphics();
            try {
                assertDoesNotThrow(() -> button.render(g2D));
                moveTo(INSIDE_X, INSIDE_Y);
                assertDoesNotThrow(() -> button.render(g2D));
            } finally {
                g2D.dispose();
            }
        }

        @Test
        void missingImage_usesFallbackAndRenders() {
            Button missing = assertDoesNotThrow(() -> new Button(X, Y, WIDTH, HEIGHT, "DoesNotExist"));
            Graphics2D g2D = canvas.createGraphics();
            try {
                assertDoesNotThrow(() -> missing.render(g2D));
            } finally {
                g2D.dispose();
            }
        }
    }
}
