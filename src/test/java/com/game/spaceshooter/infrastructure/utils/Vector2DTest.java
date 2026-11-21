package com.game.spaceshooter.infrastructure.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class Vector2DTest {

    private static final double EPS = 1e-9;

    private static void assertVector(double expectedX, double expectedY, Vector2D actual) {
        assertEquals(expectedX, actual.getX(), EPS, "x");
        assertEquals(expectedY, actual.getY(), EPS, "y");
    }

    @Nested
    @DisplayName("Constructor & getters")
    class ConstructorTests {
        @Test
        void constructorStoresCoordinates() {
            Vector2D v = new Vector2D(3.5, -2.0);
            assertEquals(3.5, v.getX());
            assertEquals(-2.0, v.getY());
        }
    }

    @Nested
    @DisplayName("add")
    class AddTests {
        @Test
        void addVectorReturnsNewSum() {
            Vector2D a = new Vector2D(1, 2);
            Vector2D b = new Vector2D(3, -5);
            Vector2D r = a.add(b);
            assertVector(4, -3, r);
            assertNotSame(a, r);
            assertVector(1, 2, a);
            assertVector(3, -5, b);
        }

        @Test
        void addScalarAddsToBothComponents() {
            Vector2D a = new Vector2D(1, 2);
            Vector2D r = a.add(2.5);
            assertVector(3.5, 4.5, r);
            assertVector(1, 2, a);
        }

        @Test
        void addInPlaceMutatesThis() {
            Vector2D a = new Vector2D(1, 2);
            Vector2D b = new Vector2D(-4, 6);
            a.addInPlace(b);
            assertVector(-3, 8, a);
            assertVector(-4, 6, b);
        }

        @Test
        void addInPlaceWithSelfDoubles() {
            Vector2D a = new Vector2D(2, -3);
            a.addInPlace(a);
            assertVector(4, -6, a);
        }

        @Test
        void addNullVectorThrows() {
            Vector2D a = new Vector2D(1, 1);
            assertThrows(NullPointerException.class, () -> a.add((Vector2D) null));
            assertThrows(NullPointerException.class, () -> a.addInPlace(null));
        }
    }

    @Nested
    @DisplayName("subtract")
    class SubtractTests {
        @Test
        void subtractReturnsNewDifference() {
            Vector2D a = new Vector2D(5, 7);
            Vector2D b = new Vector2D(2, 10);
            Vector2D r = a.subtract(b);
            assertVector(3, -3, r);
            assertVector(5, 7, a);
        }

        @Test
        void subtractSelfGivesZero() {
            Vector2D a = new Vector2D(5, 7);
            assertVector(0, 0, a.subtract(a));
        }
    }

    @Nested
    @DisplayName("multiply")
    class MultiplyTests {
        @Test
        void multiplyReturnsScaledVector() {
            Vector2D a = new Vector2D(2, -3);
            assertVector(5, -7.5, a.multiply(2.5));
            assertVector(2, -3, a);
        }

        @Test
        void multiplyByZeroGivesZero() {
            assertVector(0, 0, new Vector2D(2, -3).multiply(0));
        }

        @Test
        void multiplyByNegativeFlipsDirection() {
            assertVector(-2, 3, new Vector2D(2, -3).multiply(-1));
        }

        @Test
        void multiplyInPlaceMutatesThis() {
            Vector2D a = new Vector2D(2, -3);
            a.multiplyInPlace(3);
            assertVector(6, -9, a);
        }
    }

    @Nested
    @DisplayName("length")
    class LengthTests {
        @Test
        void lengthOf345Triangle() {
            assertEquals(5, new Vector2D(3, 4).length(), EPS);
            assertEquals(5, new Vector2D(-3, -4).length(), EPS);
        }

        @Test
        void lengthOfZeroVector() {
            assertEquals(0, new Vector2D(0, 0).length(), EPS);
        }

        @Test
        void squareLength() {
            assertEquals(25, new Vector2D(3, -4).squareLength(), EPS);
            assertEquals(0, new Vector2D(0, 0).squareLength(), EPS);
        }

        @Test
        void squareLengthEqualsLengthSquared() {
            Vector2D v = new Vector2D(1.7, -2.3);
            assertEquals(v.length() * v.length(), v.squareLength(), EPS);
        }
    }

    @Nested
    @DisplayName("copy")
    class CopyTests {
        @Test
        void copyReturnsIndependentEqualVector() {
            Vector2D a = new Vector2D(1, 2);
            Vector2D c = a.copy();
            assertEquals(a, c);
            assertNotSame(a, c);
            c.copy(9, 9);
            assertVector(1, 2, a);
        }

        @Test
        void copyFromVectorOverwritesThis() {
            Vector2D a = new Vector2D(1, 2);
            Vector2D b = new Vector2D(-5, 8);
            a.copy(b);
            assertVector(-5, 8, a);
            b.copy(0, 0);
            assertVector(-5, 8, a);
        }

        @Test
        void copyFromNullThrowsIllegalArgument() {
            Vector2D a = new Vector2D(1, 2);
            IllegalArgumentException ex =
                    assertThrows(IllegalArgumentException.class, () -> a.copy((Vector2D) null));
            assertEquals("Vector is required", ex.getMessage());
            assertVector(1, 2, a);
        }

        @Test
        void copyFromCoordinatesOverwritesThis() {
            Vector2D a = new Vector2D(1, 2);
            a.copy(7.5, -1.25);
            assertVector(7.5, -1.25, a);
        }
    }

    @Nested
    @DisplayName("normalize")
    class NormalizeTests {
        @Test
        void normalizeReturnsUnitVectorSameDirection() {
            Vector2D a = new Vector2D(3, 4);
            Vector2D n = a.normalize();
            assertVector(0.6, 0.8, n);
            assertEquals(1, n.length(), EPS);
            assertVector(3, 4, a);
        }

        @Test
        void normalizeZeroVectorReturnsZero() {
            Vector2D a = new Vector2D(0, 0);
            Vector2D n = a.normalize();
            assertVector(0, 0, n);
            assertNotSame(a, n);
        }

        @Test
        void normalizeAlreadyUnitVector() {
            assertVector(0, -1, new Vector2D(0, -1).normalize());
        }

        @Test
        void normalizeInPlaceMutatesThis() {
            Vector2D a = new Vector2D(-6, 8);
            a.normalizeInPlace();
            assertVector(-0.6, 0.8, a);
            assertEquals(1, a.length(), EPS);
        }

        @Test
        void normalizeInPlaceZeroVectorStaysZero() {
            Vector2D a = new Vector2D(0, 0);
            a.normalizeInPlace();
            assertVector(0, 0, a);
            assertFalse(Double.isNaN(a.getX()) || Double.isNaN(a.getY()));
        }
    }

    @Nested
    @DisplayName("equals")
    class EqualsTests {
        @Test
        void equalWhenSameCoordinates() {
            assertEquals(new Vector2D(1.5, -2), new Vector2D(1.5, -2));
        }

        @Test
        void reflexive() {
            Vector2D a = new Vector2D(1, 2);
            assertEquals(a, a);
        }

        @Test
        void symmetric() {
            Vector2D a = new Vector2D(1, 2);
            Vector2D b = new Vector2D(1, 2);
            assertTrue(a.equals(b) && b.equals(a));
        }

        @Test
        void notEqualWhenDifferentX() {
            assertNotEquals(new Vector2D(1, 2), new Vector2D(1.0001, 2));
        }

        @Test
        void notEqualWhenDifferentY() {
            assertNotEquals(new Vector2D(1, 2), new Vector2D(1, 2.0001));
        }

        @Test
        void notEqualToNull() {
            assertFalse(new Vector2D(1, 2).equals(null));
        }

        @Test
        void notEqualToOtherType() {
            assertFalse(new Vector2D(1, 2).equals("Vector2D(1, 2)"));
        }

        @Test
        void positiveAndNegativeZeroAreNotEqual() {
            // Double.compare distinguishes 0.0 and -0.0
            assertNotEquals(new Vector2D(0.0, 0.0), new Vector2D(-0.0, 0.0));
        }

        @Test
        void nanEqualsNan() {
            // Double.compare treats NaN as equal to itself
            assertEquals(new Vector2D(Double.NaN, 0), new Vector2D(Double.NaN, 0));
        }
    }
}
