package com.game.spaceshooter.infrastructure.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import static org.junit.jupiter.api.Assertions.*;

class CollisionUtilsTest {

    private static Shape rotated(Shape shape, double angle, double cx, double cy) {
        return AffineTransform.getRotateInstance(angle, cx, cy).createTransformedShape(shape);
    }

    private static void assertIntersects(boolean expected, Shape a, Shape b) {
        assertEquals(expected, CollisionUtils.intersects(a, b), "intersects(a, b)");
        assertEquals(expected, CollisionUtils.intersects(b, a), "intersects(b, a)");
    }

    @Nested
    @DisplayName("Rectangles")
    class RectangleTests {
        @Test
        void intersects_overlappingRectangles_returnsTrue() {
            assertIntersects(true, new Rectangle(0, 0, 10, 10), new Rectangle(5, 5, 10, 10));
        }

        @Test
        void intersects_separatedRectangles_returnsFalse() {
            assertIntersects(false, new Rectangle(0, 0, 10, 10), new Rectangle(20, 0, 10, 10));
        }

        @Test
        void intersects_touchingEdges_returnsFalse() {
            assertIntersects(false, new Rectangle(0, 0, 10, 10), new Rectangle(10, 0, 10, 10));
        }

        @Test
        void intersects_onePixelOverlap_returnsTrue() {
            assertIntersects(true, new Rectangle(0, 0, 10, 10), new Rectangle(9, 9, 10, 10));
        }

        @Test
        void intersects_containedRectangle_returnsTrue() {
            assertIntersects(true, new Rectangle(0, 0, 100, 100), new Rectangle(40, 40, 5, 5));
        }

        @Test
        void intersects_sameRectangle_returnsTrue() {
            Rectangle rect = new Rectangle(3, 4, 10, 20);
            assertTrue(CollisionUtils.intersects(rect, rect));
        }

        @Test
        void intersects_negativeCoordinates_works() {
            assertIntersects(true, new Rectangle(-20, -20, 15, 15), new Rectangle(-10, -10, 15, 15));
            assertIntersects(false, new Rectangle(-20, -20, 5, 5), new Rectangle(0, 0, 5, 5));
        }
    }

    @Nested
    @DisplayName("Precise shape check")
    class PreciseShapeTests {
        @Test
        void intersects_circlesWithOverlappingBoundsOnly_returnsFalse() {
            Shape a = new Ellipse2D.Double(0, 0, 10, 10);
            Shape b = new Ellipse2D.Double(9, 9, 10, 10);
            assertTrue(a.getBounds().intersects(b.getBounds()));
            assertIntersects(false, a, b);
        }

        @Test
        void intersects_overlappingCircles_returnsTrue() {
            assertIntersects(true, new Ellipse2D.Double(0, 0, 10, 10), new Ellipse2D.Double(6, 0, 10, 10));
        }

        @Test
        void intersects_rotatedRectangleCornerOfBounds_returnsFalse() {
            Shape diamond = rotated(new Rectangle(0, 0, 10, 10), Math.PI / 4, 5, 5);
            Shape corner = new Rectangle(-2, -2, 2, 2);
            assertTrue(diamond.getBounds().intersects(corner.getBounds()));
            assertIntersects(false, diamond, corner);
        }

        @Test
        void intersects_rotatedRectangleCenter_returnsTrue() {
            Shape diamond = rotated(new Rectangle(0, 0, 10, 10), Math.PI / 4, 5, 5);
            assertIntersects(true, diamond, new Rectangle(4, 4, 2, 2));
        }

        @Test
        void intersects_twoRotatedRectangles_returnsTrue() {
            Shape a = rotated(new Rectangle(0, 0, 20, 4), Math.PI / 6, 10, 2);
            Shape b = rotated(new Rectangle(8, -5, 4, 14), -Math.PI / 6, 10, 2);
            assertIntersects(true, a, b);
        }

        @Test
        void intersects_triangleAndRectangle_usesRealOutline() {
            Polygon triangle = new Polygon(new int[]{0, 10, 0}, new int[]{0, 0, 10}, 3);
            assertIntersects(true, triangle, new Rectangle(1, 1, 2, 2));
            assertIntersects(false, triangle, new Rectangle(7, 7, 3, 3));
        }
    }

    @Nested
    @DisplayName("Edge cases")
    class EdgeCaseTests {
        @Test
        void intersects_zeroSizeRectangle_returnsFalse() {
            assertIntersects(false, new Rectangle(0, 0, 10, 10), new Rectangle(5, 5, 0, 0));
        }

        @Test
        void intersects_lineWithoutArea_returnsFalse() {
            assertIntersects(false, new Rectangle(0, 0, 10, 10), new Line2D.Double(-5, 5, 15, 5));
        }

        @Test
        void intersects_doesNotModifyInputs() {
            Rectangle a = new Rectangle(0, 0, 10, 10);
            Rectangle b = new Rectangle(5, 5, 10, 10);
            CollisionUtils.intersects(a, b);
            assertEquals(new Rectangle(0, 0, 10, 10), a);
            assertEquals(new Rectangle(5, 5, 10, 10), b);
        }

        @Test
        void intersects_null_throws() {
            Rectangle rect = new Rectangle(0, 0, 10, 10);
            assertThrows(NullPointerException.class, () -> CollisionUtils.intersects(null, rect));
            assertThrows(NullPointerException.class, () -> CollisionUtils.intersects(rect, null));
        }

        @Test
        void constructor_isPrivate() throws NoSuchMethodException {
            Constructor<CollisionUtils> constructor = CollisionUtils.class.getDeclaredConstructor();
            assertTrue(Modifier.isPrivate(constructor.getModifiers()));
        }
    }
}
