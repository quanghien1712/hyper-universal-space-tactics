package com.game.spaceshooter.presentation.input;

import com.game.spaceshooter.infrastructure.utils.Vector2D;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.swing.JPanel;
import java.awt.Component;
import java.awt.event.MouseEvent;

import static org.junit.jupiter.api.Assertions.*;

class MouseInputsTest {

    private static final double EPS = 1e-9;

    private final Component source = new JPanel();
    private MouseInputs inputs;

    @BeforeEach
    void setUp() {
        inputs = new MouseInputs();
    }

    private MouseEvent mouse(int id, int x, int y, int button) {
        return new MouseEvent(source, id, System.currentTimeMillis(), 0, x, y, 1, false, button);
    }

    private static void assertPos(double x, double y, Vector2D actual) {
        assertEquals(x, actual.getX(), EPS, "x");
        assertEquals(y, actual.getY(), EPS, "y");
    }

    @Test
    void initialState_originAndNotPressed() {
        assertPos(0, 0, inputs.getMousePos());
        assertFalse(inputs.isMousePressed());
        assertFalse(inputs.isMousePressed(MouseEvent.BUTTON1));
    }

    @Test
    void mousePressed_recordsButton() {
        inputs.mousePressed(mouse(MouseEvent.MOUSE_PRESSED, 10, 20, MouseEvent.BUTTON1));

        assertTrue(inputs.isMousePressed());
        assertTrue(inputs.isMousePressed(MouseEvent.BUTTON1));
        assertFalse(inputs.isMousePressed(MouseEvent.BUTTON3));
    }

    @Test
    void mousePressed_otherButton_recordsThatButton() {
        inputs.mousePressed(mouse(MouseEvent.MOUSE_PRESSED, 0, 0, MouseEvent.BUTTON3));

        assertTrue(inputs.isMousePressed(MouseEvent.BUTTON3));
        assertFalse(inputs.isMousePressed(MouseEvent.BUTTON1));
    }

    @Test
    void mousePressed_doesNotMovePosition() {
        inputs.mousePressed(mouse(MouseEvent.MOUSE_PRESSED, 300, 400, MouseEvent.BUTTON1));

        assertPos(0, 0, inputs.getMousePos());
    }

    @Test
    void mouseReleased_clearsPressed() {
        inputs.mousePressed(mouse(MouseEvent.MOUSE_PRESSED, 0, 0, MouseEvent.BUTTON1));
        inputs.mouseReleased(mouse(MouseEvent.MOUSE_RELEASED, 0, 0, MouseEvent.BUTTON1));

        assertFalse(inputs.isMousePressed());
        assertFalse(inputs.isMousePressed(MouseEvent.BUTTON1));
    }

    @Test
    void mouseMoved_updatesPosition() {
        inputs.mouseMoved(mouse(MouseEvent.MOUSE_MOVED, 120, 75, MouseEvent.NOBUTTON));
        assertPos(120, 75, inputs.getMousePos());

        inputs.mouseMoved(mouse(MouseEvent.MOUSE_MOVED, 5, 600, MouseEvent.NOBUTTON));
        assertPos(5, 600, inputs.getMousePos());
    }

    @Test
    void mouseMoved_doesNotSetPressed() {
        inputs.mouseMoved(mouse(MouseEvent.MOUSE_MOVED, 50, 50, MouseEvent.NOBUTTON));

        assertFalse(inputs.isMousePressed());
    }

    @Test
    void mouseDragged_setsPressed() {
        inputs.mouseDragged(mouse(MouseEvent.MOUSE_DRAGGED, 200, 150, MouseEvent.NOBUTTON));

        assertTrue(inputs.isMousePressed());
        assertPos(200, 150, inputs.getMousePos());
    }

    @Test
    void setMousePos_storesEventCoordinates() {
        inputs.setMousePos(mouse(MouseEvent.MOUSE_MOVED, 33, 44, MouseEvent.NOBUTTON));

        assertPos(33, 44, inputs.getMousePos());
    }

    @Test
    void getMousePos_returnsDefensiveCopy() {
        inputs.mouseMoved(mouse(MouseEvent.MOUSE_MOVED, 10, 10, MouseEvent.NOBUTTON));

        Vector2D pos = inputs.getMousePos();
        pos.copy(999, 999);

        assertPos(10, 10, inputs.getMousePos());
        assertNotSame(inputs.getMousePos(), inputs.getMousePos());
    }

    @Test
    void copyMousePosTo_writesIntoDestination() {
        inputs.mouseMoved(mouse(MouseEvent.MOUSE_MOVED, 64, 128, MouseEvent.NOBUTTON));
        Vector2D destination = new Vector2D(-1, -1);

        inputs.copyMousePosTo(destination);

        assertPos(64, 128, destination);
    }

    @Test
    void copyMousePosTo_destinationIsIndependent() {
        inputs.mouseMoved(mouse(MouseEvent.MOUSE_MOVED, 1, 2, MouseEvent.NOBUTTON));
        Vector2D destination = new Vector2D(0, 0);
        inputs.copyMousePosTo(destination);

        inputs.mouseMoved(mouse(MouseEvent.MOUSE_MOVED, 3, 4, MouseEvent.NOBUTTON));

        assertPos(1, 2, destination);
    }

    @Test
    void clickedEnteredExited_doNotChangeState() {
        inputs.mouseClicked(mouse(MouseEvent.MOUSE_CLICKED, 70, 80, MouseEvent.BUTTON1));
        inputs.mouseEntered(mouse(MouseEvent.MOUSE_ENTERED, 70, 80, MouseEvent.NOBUTTON));
        inputs.mouseExited(mouse(MouseEvent.MOUSE_EXITED, 70, 80, MouseEvent.NOBUTTON));

        assertFalse(inputs.isMousePressed());
        assertPos(0, 0, inputs.getMousePos());
    }
}
