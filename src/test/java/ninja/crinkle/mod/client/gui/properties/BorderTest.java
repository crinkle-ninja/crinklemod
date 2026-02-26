package ninja.crinkle.mod.client.gui.properties;

import net.minecraft.client.gui.GuiGraphics;
import ninja.crinkle.mod.client.color.Color;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@DisplayName("Border Properties")
class BorderTest {

    @Nested
    @DisplayName("Constructor")
    class ConstructorTest {
        @Test
        @DisplayName("should create a border with individual values")
        void constructor_withIndividualValues() {
            Border border = new Border(5, 10, 15, 20, Color.RED);

            assertEquals(5, border.top());
            assertEquals(10, border.right());
            assertEquals(15, border.bottom());
            assertEquals(20, border.left());
            assertEquals(Color.RED, border.color());
        }

        @Test
        @DisplayName("should create a border with all sides equal")
        void constructor_withAllSidesEqual() {
            Border border = new Border(10, Color.BLUE);

            assertEquals(10, border.top());
            assertEquals(10, border.right());
            assertEquals(10, border.bottom());
            assertEquals(10, border.left());
            assertEquals(Color.BLUE, border.color());
        }

        @Test
        @DisplayName("should create a border with vertical and horizontal values")
        void constructor_withVerticalAndHorizontalValues() {
            Border border = new Border(20, 15, Color.YELLOW);

            assertEquals(20, border.top());
            assertEquals(15, border.right());
            assertEquals(20, border.bottom());
            assertEquals(15, border.left());
            assertEquals(Color.YELLOW, border.color());
        }

        @Test
        @DisplayName("should throw an exception for negative values")
        void constructor_throwsExceptionForNegativeValues() {
            assertThrows(IllegalArgumentException.class, () -> new Border(-1, 10, 15, 20, Color.GREEN));
            assertThrows(IllegalArgumentException.class, () -> new Border(5, -10, 15, 20, Color.GREEN));
            assertThrows(IllegalArgumentException.class, () -> new Border(5, 10, -15, 20, Color.GREEN));
            assertThrows(IllegalArgumentException.class, () -> new Border(5, 10, 15, -20, Color.GREEN));
        }
    }

    @Nested
    @DisplayName("Static Factories and Constants")
    class FactoriesAndConstantsTest {
        @Test
        @DisplayName("all() should create a border with equal sides and default color")
        void all_createsBorderWithEqualSides() {
            Border border = Border.all(5);

            assertEquals(5, border.top());
            assertEquals(5, border.right());
            assertEquals(5, border.bottom());
            assertEquals(5, border.left());
            assertEquals(Color.BLACK, border.color());
        }

        @Test
        @DisplayName("ZERO constant should have all zero values and default color")
        void zero_hasZeroValues() {
            Border border = Border.ZERO;

            assertEquals(0, border.top());
            assertEquals(0, border.right());
            assertEquals(0, border.bottom());
            assertEquals(0, border.left());
            assertEquals(Color.BLACK, border.color());
        }
    }

    @Nested
    @DisplayName("Addition Methods")
    class AdditionTest {
        @Test
        @DisplayName("should add individual values to a border")
        void add_withIndividualValues() {
            Border border = new Border(5, 5, 5, 5, Color.RED);
            Border result = border.add(2, 3, 4, 1);

            assertEquals(7, result.top());
            assertEquals(8, result.right());
            assertEquals(9, result.bottom());
            assertEquals(6, result.left());
            assertEquals(Color.RED, result.color());
        }

        @Test
        @DisplayName("should add another border's values")
        void add_withAnotherBorder() {
            Border border1 = new Border(10, 10, 10, 10, Color.BLUE);
            Border border2 = new Border(5, 5, 5, 5, Color.GREEN);

            Border result = border1.add(border2);

            assertEquals(15, result.top());
            assertEquals(15, result.right());
            assertEquals(15, result.bottom());
            assertEquals(15, result.left());
            assertEquals(Color.BLUE, result.color());
        }
    }

    @Nested
    @DisplayName("Rendering")
    class RenderingTest {
        @Test
        @DisplayName("should render all four sides of the border")
        void render_callsGuiGraphicsFill() {
            GuiGraphics mockGuiGraphics = mock(GuiGraphics.class);
            Box mockBox = mock(Box.class);

            when(mockBox.topLeft()).thenReturn(new ImmutablePoint(0, 0));
            when(mockBox.topRight()).thenReturn(new ImmutablePoint(10, 0));
            when(mockBox.bottomLeft()).thenReturn(new ImmutablePoint(0, 10));
            when(mockBox.bottomRight()).thenReturn(new ImmutablePoint(10, 10));

            Border border = new Border(2, 2, 2, 2, Color.BLACK);
            int zIndex = 1;

            border.render(mockGuiGraphics, mockBox, zIndex);

            verify(mockGuiGraphics, times(4)).fill(anyInt(), anyInt(), anyInt(), anyInt(), eq(zIndex), eq(Color.BLACK.color()));
        }
    }
}