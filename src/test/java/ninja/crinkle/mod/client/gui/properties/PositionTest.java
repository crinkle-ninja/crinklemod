package ninja.crinkle.mod.client.gui.properties;

import ninja.crinkle.mod.client.gui.states.Positioning;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Position Properties")
class PositionTest {

    @Nested
    @DisplayName("Static Factories")
    class StaticFactoriesTest {

        @Nested
        @DisplayName("absolute()")
        class AbsoluteFactoryTest {
            @Test
            @DisplayName("should create an absolute position from a Point")
            void absolute_fromPoint() {
                ImmutablePoint point = new ImmutablePoint(10, 20);
                Position position = Position.absolute(point);

                assertEquals(point, position.point());
                assertTrue(position.absolute());
            }

            @Test
            @DisplayName("should create an absolute position from coordinates")
            void absolute_fromCoordinates() {
                Position position = Position.absolute(10, 20);

                assertEquals(new ImmutablePoint(10, 20), position.point());
                assertTrue(position.absolute());
            }

            @Test
            @DisplayName("should throw an exception for a null Point")
            void absolute_throwsForNull() {
                assertThrows(IllegalArgumentException.class, () -> Position.absolute(null));
            }
        }

        @Nested
        @DisplayName("relative()")
        class RelativeFactoryTest {
            @Test
            @DisplayName("should create a relative position from a Point")
            void relative_fromPoint() {
                ImmutablePoint point = new ImmutablePoint(15, 25);
                Position position = Position.relative(point);

                assertEquals(point, position.point());
                assertTrue(position.relative());
            }

            @Test
            @DisplayName("should create a relative position from coordinates")
            void relative_fromCoordinates() {
                Position position = Position.relative(15, 25);

                assertEquals(new ImmutablePoint(15, 25), position.point());
                assertTrue(position.relative());
            }

            @Test
            @DisplayName("should throw an exception for a null Point")
            void relative_throwsForNull() {
                assertThrows(IllegalArgumentException.class, () -> Position.relative(null));
            }
        }
    }

    @Nested
    @DisplayName("Constructor")
    class ConstructorTest {
        @Test
        @DisplayName("should throw an exception for null arguments")
        void constructor_throwsForNulls() {
            assertThrows(IllegalArgumentException.class, () -> new Position(new ImmutablePoint(10, 20), null));
            assertThrows(IllegalArgumentException.class, () -> new Position(null, Positioning.Absolute));
            assertThrows(IllegalArgumentException.class, () -> new Position(null, null));
        }
    }

    @Nested
    @DisplayName("Operations")
    class OperationsTest {
        @Nested
        @DisplayName("offsetBy()")
        class OffsetByTest {
            @Test
            @DisplayName("should apply an offset from double values")
            void offsetBy_withDoubles() {
                Position initial = Position.absolute(10, 20);
                Position offset = initial.offsetBy(5.5, -2.5);

                assertEquals(new ImmutablePoint(15.5, 17.5), offset.point());
                assertTrue(offset.absolute());
            }

            @Test
            @DisplayName("should apply an offset from a Point")
            void offsetBy_withPoint() {
                Position initial = Position.relative(10, 20);
                Position offset = initial.offsetBy(new ImmutablePoint(3, -7));

                assertEquals(new ImmutablePoint(13, 13), offset.point());
                assertTrue(offset.relative());
            }

            @Test
            @DisplayName("should throw an exception for a null Point")
            void offsetBy_throwsForNull() {
                assertThrows(IllegalArgumentException.class, () -> Position.absolute(10, 20).offsetBy(null));
            }
        }

        @Test
        @DisplayName("withBase() should calculate a new position from a base Point")
        void withBase_recalculatesPosition() {
            ImmutablePoint base = new ImmutablePoint(5, 5);
            Position absolute = Position.absolute(10, 20);
            Position relative = Position.relative(10, 20);

            Position newAbsolute = absolute.withBase(base);
            assertEquals(new ImmutablePoint(15, 25), newAbsolute.point());
            assertEquals(Positioning.Absolute, newAbsolute.positioning());

            Position newRelative = relative.withBase(base);
            assertEquals(new ImmutablePoint(15, 25), newRelative.point());
            assertEquals(Positioning.Relative, newRelative.positioning());
        }
    }

    @Nested
    @DisplayName("Object Contract")
    class ObjectContractTest {
        @Test
        @DisplayName("equals() should correctly compare positions for equality")
        void equals_verifiesContract() {
            Position p1 = Position.absolute(10, 20);
            Position p2 = Position.absolute(10, 20);
            Position p3_differentPositioning = Position.relative(10, 20);
            Position p4_differentPoint = Position.absolute(15, 25);

            assertEquals(p1, p1); // Self
            assertEquals(p1, p2); // Equal
            assertNotEquals(p1, p3_differentPositioning);
            assertNotEquals(p1, p4_differentPoint);
            assertNotEquals(p1, null);
            assertNotEquals(p1, new Object());
        }

        @Test
        @DisplayName("toString() should return a formatted string representation")
        void toString_returnsFormattedString() {
            Position position = Position.absolute(10, 20);
            String expected = "Position[point=" + position.point() + ", positioning=" + position.positioning() + ']';
            assertEquals(expected, position.toString());
        }
    }
}