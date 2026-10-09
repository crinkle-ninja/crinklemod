package ninja.crinkle.mod.client.gui.properties;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Rect")
class RectTest {

    @Test
    @DisplayName("negative dimensions throw IllegalArgumentException")
    void negativeDimensions() {
        assertThrows(IllegalArgumentException.class, () -> new Rect(0, 0, -1, 10));
        assertThrows(IllegalArgumentException.class, () -> new Rect(0, 0, 10, -1));
    }

    @Test
    @DisplayName("ZERO constant")
    void zeroConstant() {
        assertEquals(new Rect(0, 0, 0, 0), Rect.ZERO);
    }

    @Nested
    @DisplayName("contains")
    class ContainsTest {
        @Test
        @DisplayName("point at bottom-right boundary returns false (exclusive)")
        void bottomRightExclusive() {
            Rect rect = new Rect(10, 20, 50, 30);
            assertFalse(rect.contains(60, 50));
        }

        @Test
        @DisplayName("double variant works the same")
        void doubleVariant() {
            Rect rect = new Rect(10, 20, 50, 30);
            assertTrue(rect.contains(25.5, 35.5));
            assertFalse(rect.contains(60.0, 50.0));
        }

        @Test
        @DisplayName("point inside rect returns true")
        void inside() {
            Rect rect = new Rect(10, 20, 50, 30);
            assertTrue(rect.contains(25, 35));
        }

        @Test
        @DisplayName("point outside returns false")
        void outside() {
            Rect rect = new Rect(10, 20, 50, 30);
            assertFalse(rect.contains(5, 15));
        }

        @Test
        @DisplayName("point at top-left corner returns true")
        void topLeftCorner() {
            Rect rect = new Rect(10, 20, 50, 30);
            assertTrue(rect.contains(10, 20));
        }
    }

    @Nested
    @DisplayName("edges")
    class EdgesTest {
        @Test
        @DisplayName("bottom() returns y + height")
        void bottom() {
            assertEquals(50, new Rect(10, 20, 50, 30).bottom());
        }

        @Test
        @DisplayName("right() returns x + width")
        void right() {
            assertEquals(60, new Rect(10, 20, 50, 30).right());
        }
    }

    @Nested
    @DisplayName("shrink")
    class ShrinkTest {
        @Test
        @DisplayName("over-shrinking clamps to zero size")
        void overShrink() {
            Rect rect = new Rect(0, 0, 10, 10);
            Rect shrunk = rect.shrink(20);

            assertEquals(0, shrunk.width());
            assertEquals(0, shrunk.height());
        }

        @Test
        @DisplayName("shrinks rect by specified margins")
        void shrinkByMargins() {
            Rect rect = new Rect(0, 0, 100, 80);
            Rect shrunk = rect.shrink(10, 20, 30, 40);

            assertEquals(new Rect(40, 10, 40, 40), shrunk);
        }

        @Test
        @DisplayName("uniform shrink")
        void uniformShrink() {
            Rect rect = new Rect(0, 0, 100, 100);
            Rect shrunk = rect.shrink(10);

            assertEquals(new Rect(10, 10, 80, 80), shrunk);
        }
    }
}
