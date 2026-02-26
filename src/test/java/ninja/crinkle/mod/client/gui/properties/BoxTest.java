package ninja.crinkle.mod.client.gui.properties;

import ninja.crinkle.mod.client.gui.states.Positioning;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.ThrowingSupplier;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Box Properties")
class BoxTest {

    @Nested
    @DisplayName("Constructor")
    class ConstructorTest {
        @Test
        @DisplayName("should create a box from position and size")
        void constructor_fromPositionAndSize() {
            Position position = Position.absolute(10, 20);
            Size size = Size.ofPixels(30, 40);
            Box box = new Box(position, size);

            assertEquals(position, box.position(), "Position should match the input");
            assertEquals(size, box.size(), "Size should match the input");
        }

        @Test
        @DisplayName("should create a box from coordinates and dimensions")
        void constructor_fromCoordinates() {
            Box box = new Box(10, 20, 30, 40, Positioning.Absolute);

            assertEquals(10, box.position().point().x());
            assertEquals(20, box.position().point().y());
            assertEquals(30, box.size().width());
            assertEquals(40, box.size().height());
            assertTrue(box.position().absolute());
        }

        @Test
        @DisplayName("should throw an exception for negative dimensions")
        void constructor_throwsForNegativeDimensions() {
            Position position = Position.absolute(0, 0);
            assertThrows(IllegalArgumentException.class, () -> new Box(position, Size.ofPixels(-1, 10)));
            assertThrows(IllegalArgumentException.class, () -> new Box(position, Size.ofPixels(10, -1)));
        }
    }

    @Nested
    @DisplayName("Modification Methods")
    class ModificationMethodsTest {
        @Test
        @DisplayName("add() should correctly add integer values to position and size")
        void add_withIntegers() {
            Box originalBox = new Box(10, 20, 30, 40, Positioning.Absolute);
            Box addedBox = originalBox.add(5, 10, 15, 20);

            assertEquals(15, addedBox.position().point().x());
            assertEquals(30, addedBox.position().point().y());
            assertEquals(45, addedBox.size().width());
            assertEquals(60, addedBox.size().height());
        }

        @Test
        @DisplayName("add() should correctly offset the position using a Position object")
        void add_withPosition() {
            Position originalPosition = Position.absolute(10, 20);
            Position offsetPosition = Position.absolute(15, 10);
            Box originalBox = new Box(originalPosition, Size.ofPixels(30, 40));
            Box addedBox = originalBox.add(offsetPosition);

            assertEquals(25, addedBox.position().point().x());
            assertEquals(30, addedBox.position().point().y());
            assertEquals(originalBox.size(), addedBox.size());
        }

        @Test
        @DisplayName("subtract() should correctly subtract integer values from position and size")
        void subtract_withIntegers() {
            Box originalBox = new Box(10, 20, 30, 40, Positioning.Absolute);
            Box subtractedBox = originalBox.subtract(5, 10, 15, 20);

            assertEquals(5, subtractedBox.position().point().x());
            assertEquals(10, subtractedBox.position().point().y());
            assertEquals(15, subtractedBox.size().width());
            assertEquals(20, subtractedBox.size().height());
        }

        @Test
        @DisplayName("shrink() should correctly reduce the size based on a BoxProperty")
        void shrink_withBoxProperty() {
            Box originalBox = new Box(10, 20, 50, 60, Positioning.Absolute);
            BoxProperty shrinkProperty = BoxProperty.of(5, 10, 15, 5);
            Box shrunkBox = originalBox.shrink(shrinkProperty);

            assertEquals(35, shrunkBox.size().width());
            assertEquals(40, shrunkBox.size().height());

            BoxProperty zeroProperty = BoxProperty.of(0, 0, 0, 0);
            Box shrunkByZero = originalBox.shrink(zeroProperty);
            assertEquals(originalBox, shrunkByZero);

            BoxProperty invalidProperty = BoxProperty.of(50, 60, 100, 100);
            ThrowingSupplier<Box> originalBoxSupplier = () -> originalBox.shrink(invalidProperty);
            Box shrunkToZero = assertDoesNotThrow(originalBoxSupplier);
            assertEquals(0, shrunkToZero.size().width());
            assertEquals(0, shrunkToZero.size().height());
        }
    }

    @Nested
    @DisplayName("Geometric Queries")
    class GeometricQueriesTest {
        @Test
        @DisplayName("contains() should correctly determine if a point is inside")
        void contains_withPoint() {
            Box box = new Box(10, 20, 30, 40, Positioning.Absolute);

            assertTrue(box.contains(15, 25));
            assertFalse(box.contains(5, 25));
            assertTrue(box.contains(10.5, 20.5));
            assertFalse(box.contains(50.5, 60.5));
        }

        @Test
        @DisplayName("overlaps() should correctly determine if another box overlaps")
        void overlaps_withBox() {
            Box box1 = new Box(10, 20, 30, 40, Positioning.Absolute);
            Box box2 = new Box(20, 30, 30, 40, Positioning.Absolute);
            Box box3 = new Box(100, 100, 30, 40, Positioning.Absolute);

            assertTrue(box1.overlaps(box2));
            assertFalse(box1.overlaps(box3));
        }
    }

    @Nested
    @DisplayName("Coordinate Calculations")
    class CoordinateCalculationsTest {
        @Test
        @DisplayName("topLeft() should return the top-left point")
        void topLeft_returnsCorrectPoint() {
            Box box = new Box(10, 20, 30, 40, Positioning.Absolute);
            assertEquals(new ImmutablePoint(10, 20), box.topLeft());
        }

        @Test
        @DisplayName("topRight() should return the top-right point")
        void topRight_returnsCorrectPoint() {
            Box box = new Box(10, 20, 30, 40, Positioning.Absolute);
            assertEquals(new ImmutablePoint(40, 20), box.topRight());
        }

        @Test
        @DisplayName("bottomLeft() should return the bottom-left point")
        void bottomLeft_returnsCorrectPoint() {
            Box box = new Box(10, 20, 30, 40, Positioning.Absolute);
            assertEquals(new ImmutablePoint(10, 60), box.bottomLeft());
        }

        @Test
        @DisplayName("bottomRight() should return the bottom-right point")
        void bottomRight_returnsCorrectPoint() {
            Box box = new Box(10, 20, 30, 40, Positioning.Absolute);
            assertEquals(new ImmutablePoint(40, 60), box.bottomRight());
        }
    }

    @Nested
    @DisplayName("Equality Contract")
    class EqualityContractTest {
        @Test
        @DisplayName("equals() should be true for identical properties and false otherwise")
        void equals_verifiesContract() {
            Box box1 = new Box(10, 20, 30, 40, Positioning.Absolute);
            Box box2 = new Box(10, 20, 30, 40, Positioning.Absolute);
            Box box3 = new Box(15, 25, 35, 45, Positioning.Absolute);

            assertEquals(box1, box2);
            assertNotEquals(box1, box3);
        }
    }
}