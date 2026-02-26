package ninja.crinkle.mod.client.gui.states;

import ninja.crinkle.mod.client.gui.properties.*;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("WidgetLayout Tests")
class WidgetLayoutTest {
    private WidgetLayout defaultLayout;

    @BeforeEach
    void setUp() {
        defaultLayout = new WidgetLayout();
    }

    @Nested
    @DisplayName("Equality and HashCode Contract")
    class EqualityAndHashCodeContractTest {
        @Test
        @DisplayName("should be equal to itself")
        void testEquals_sameObject_returnsTrue() {
            assertEquals(defaultLayout, defaultLayout);
        }

        @Test
        @DisplayName("should be equal to another instance with the same properties")
        void testEquals_equivalentObjects_returnsTrue() {
            WidgetLayout layout2 = new WidgetLayout();
            assertEquals(defaultLayout, layout2);
        }

        @Test
        @DisplayName("should not be equal to an instance with different properties")
        void testEquals_differentObjects_returnsFalse() {
            WidgetLayout layout2 = new WidgetLayout(Position.relative(5, 10), Size.ofPixels(50, 50), Margin.ZERO, Border.ZERO, Padding.ZERO);
            assertNotEquals(defaultLayout, layout2);
        }

        @Test
        @DisplayName("should have a consistent hash code")
        void testHashCode_isConsistent() {
            assertEquals(defaultLayout.hashCode(), defaultLayout.hashCode());
        }

        @Test
        @DisplayName("should have the same hash code for equivalent objects")
        void testHashCode_forEquivalentObjects() {
            WidgetLayout layout2 = new WidgetLayout();
            assertEquals(defaultLayout.hashCode(), layout2.hashCode());
        }
    }

    @Nested
    @DisplayName("Constructor")
    class ConstructorTest {
        @Test
        @DisplayName("should set Positioning to Absolute for an absolute Position")
        void constructor_withAbsolutePosition_setsAbsolutePositioning() {
            Position position = Position.absolute(10, 20);
            WidgetLayout layout = new WidgetLayout(position, Size.ofPixels(50, 50), Margin.ZERO, Border.ZERO, Padding.ZERO);
            assertEquals(Positioning.Absolute, layout.positioning());
        }

        @Test
        @DisplayName("should set Positioning to Relative for a relative Position")
        void constructor_withRelativePosition_setsRelativePositioning() {
            Position position = Position.relative(10, 20);
            WidgetLayout layout = new WidgetLayout(position, Size.ofPixels(50, 50), Margin.ZERO, Border.ZERO, Padding.ZERO);
            assertEquals(Positioning.Relative, layout.positioning());
        }
    }

    @Nested
    @DisplayName("Builder")
    class BuilderTest {
        @Test
        @DisplayName("build() should throw if position type mismatches positioning")
        void build_whenMismatchedPositionType_shouldThrowException() {
            WidgetLayout relativeLayout = new WidgetLayout(Position.relative(10, 20), Size.ZERO, Margin.ZERO, Border.ZERO, Padding.ZERO);
            Exception e = assertThrows(IllegalArgumentException.class, () ->
                    relativeLayout.toBuilder().position(Position.absolute(30, 40)).build());
            assertEquals("Positioning of position must match layout positioning", e.getMessage());
        }
    }
}