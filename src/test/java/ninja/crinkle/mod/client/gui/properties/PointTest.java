package ninja.crinkle.mod.client.gui.properties;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Point Properties")
class PointTest {

    @Nested
    @DisplayName("Accessors")
    class AccessorTest {
        @Test
        @DisplayName("x() should return the correct double value")
        void x_returnsDoubleValue() {
            assertEquals(10.5, Point.of(10.5, 20).x());
        }

        @Test
        @DisplayName("y() should return the correct double value")
        void y_returnsDoubleValue() {
            assertEquals(20.5, Point.of(10.5, 20.5).y());
        }

        @Test
        @DisplayName("xInt() should return the correct rounded integer value")
        void xInt_returnsRoundedInt() {
            assertEquals(11, Point.of(10.5, 20.5).xInt());
        }

        @Test
        @DisplayName("yInt() should return the correct rounded integer value")
        void yInt_returnsRoundedInt() {
            assertEquals(21, Point.of(10.5, 20.5).yInt());
        }
    }

    @Nested
    @DisplayName("Arithmetic Operations")
    class ArithmeticTest {
        @Test
        @DisplayName("should return a new point with added integer values")
        void add_withIntegers() {
            Point point = Point.of(10, 20);
            Point result = point.add(10, 20);
            assertEquals(Point.of(20, 40), result);
            assertNotSame(point, result);
        }

        @Test
        @DisplayName("should return a new point with added Point values")
        void add_withPoint() {
            Point point = Point.of(10, 20);
            Point result = point.add(Point.of(10, 20));
            assertEquals(Point.of(20, 40), result);
            assertNotSame(point, result);
        }

        @Test
        @DisplayName("should return a new point with subtracted integer values")
        void subtract_withIntegers() {
            Point point = Point.of(10, 20);
            Point result = point.subtract(10, 20);
            assertEquals(Point.ZERO, result);
            assertNotSame(point, result);
        }

        @Test
        @DisplayName("should return a new point with subtracted double values")
        void subtract_withDoubles() {
            Point point = Point.of(10, 10);
            Point result = point.subtract(5.5, 5.5);
            assertEquals(Point.of(4.5, 4.5), result);
            assertNotSame(point, result);
        }
    }

    @Nested
    @DisplayName("Equality and Comparison")
    class EqualityAndComparisonTest {
        @Test
        @DisplayName("equals() should correctly compare points and handle different types")
        void equals_verifiesContract() {
            Point point1 = Point.of(10, 20);
            Point point2 = Point.of(10, 20);
            Point point3 = Point.of(10, 30);
            Point point4 = Point.of(20, 20);

            assertEquals(point1, point2);
            assertNotEquals(point1, point3);
            assertNotEquals(point1, point4);
            assertNotEquals(null, point1);
            assertNotEquals(new Object(), point1);
        }

        @Test
        @DisplayName("compareTo() should correctly order points based on x and then y")
        void compareTo_ordersCorrectly() {
            Point p1 = Point.of(10, 20);
            Point p2_equal = Point.of(10, 20);
            Point p3_greaterY = Point.of(10, 30);
            Point p4_greaterX = Point.of(20, 20);

            assertEquals(0, p1.compareTo(p2_equal));
            assertEquals(-1, p1.compareTo(p3_greaterY));
            assertEquals(-1, p1.compareTo(p4_greaterX));
            assertEquals(1, p3_greaterY.compareTo(p1));
            assertEquals(1, p4_greaterX.compareTo(p1));
        }
    }

    @Nested
    @DisplayName("String Representation")
    class StringRepresentationTest {
        @Test
        @DisplayName("toString() should return the correct string format")
        void toString_returnsCorrectFormat() {
            assertEquals("Point{x=10.0, y=20.0}", Point.of(10, 20).toString());
        }
    }
}
