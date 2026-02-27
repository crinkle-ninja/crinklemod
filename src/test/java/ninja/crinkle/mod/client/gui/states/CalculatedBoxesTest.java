package ninja.crinkle.mod.client.gui.states;

import ninja.crinkle.mod.client.gui.properties.*;
import ninja.crinkle.mod.client.gui.widgets.AbstractWidget;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("CalculatedBoxes Unit Tests")
class CalculatedBoxesTest {

    private Box createValidMockBox() {
        Box box = mock(Box.class);
        Position position = mock(Position.class);
        when(position.absolute()).thenReturn(true);
        when(box.position()).thenReturn(position);
        Size size = mock(Size.class);
        when(size.unit()).thenReturn(Size.Unit.Pixels);
        when(size.width()).thenReturn(10f);
        when(size.height()).thenReturn(10f);
        when(box.size()).thenReturn(size);
        return box;
    }

    @Nested
    @DisplayName("Constructor Validation Tests")
    class ConstructorValidationTests {
        @Test
        @DisplayName("Should throw IllegalArgumentException when borderBox is null")
        void constructorThrowsWhenBorderBoxIsNull() {
            Box paddingBox = createValidMockBox();
            Box backgroundBox = createValidMockBox();
            Box contentBox = createValidMockBox();

            assertThrows(IllegalArgumentException.class,
                    () -> new CalculatedBoxes(null, paddingBox, backgroundBox, contentBox),
                    "Expected constructor to throw IllegalArgumentException if borderBox is null");
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException if any Box has null position")
        void constructorThrowsWhenBoxPositionIsNull() {
            Box borderBox = mock(Box.class);
            Box paddingBox = createValidMockBox();
            Box backgroundBox = createValidMockBox();
            Box contentBox = createValidMockBox();
            when(borderBox.position()).thenReturn(null);
            when(borderBox.size()).thenReturn(Size.ofPixels(10, 10));

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> new CalculatedBoxes(borderBox, paddingBox, backgroundBox, contentBox),
                    "Expected constructor to throw when a Box has null position");

            assertTrue(exception.getMessage().contains("has no position"), "Error should indicate missing position");
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException if any Box is relative")
        void constructorThrowsIfBoxIsNotAbsolute() {
            Box borderBox = mock(Box.class);
            Position relativePosition = mock(Position.class);
            when(relativePosition.absolute()).thenReturn(false);
            when(borderBox.position()).thenReturn(relativePosition);
            when(borderBox.size()).thenReturn(Size.ofPixels(10, 10));
            Box paddingBox = createValidMockBox();
            Box backgroundBox = createValidMockBox();
            Box contentBox = createValidMockBox();

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> new CalculatedBoxes(borderBox, paddingBox, backgroundBox, contentBox),
                    "Expected constructor to throw when a Box position is relative");

            assertTrue(exception.getMessage().contains("is not absolute"), "Error should indicate non-absolute position");
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException if any Box size is not pixels")
        void constructorThrowsIfBoxSizeNotInPixels() {
            Box borderBox = mock(Box.class);
            Size invalidSize = mock(Size.class);
            when(borderBox.position()).thenReturn(Position.absolute(0, 0));
            when(borderBox.size()).thenReturn(invalidSize);
            when(invalidSize.unit()).thenReturn(Size.Unit.Percent);
            Box paddingBox = createValidMockBox();
            Box backgroundBox = createValidMockBox();
            Box contentBox = createValidMockBox();

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> new CalculatedBoxes(borderBox, paddingBox, backgroundBox, contentBox),
                    "Expected constructor to throw when a Box size unit is not pixels");

            assertTrue(exception.getMessage().contains("is not in pixels"), "Error should indicate invalid size unit");
        }
    }

    @Nested
    @DisplayName("Calculate Method Tests")
    class CalculateMethodTests {
        @Test
        @DisplayName("Should throw IllegalStateException if base box position is relative")
        void calculateThrowsIfBaseBoxPositionIsRelative() {
            AbstractWidget widget = mock(AbstractWidget.class);
            Box borderBox = mock(Box.class);
            Position relativePosition = mock(Position.class);
            when(relativePosition.relative()).thenReturn(true);
            when(borderBox.position()).thenReturn(relativePosition);

            IllegalStateException exception = assertThrows(IllegalStateException.class,
                    () -> CalculatedBoxes.calculate(widget, borderBox),
                    "Expected calculate to throw IllegalStateException if base box position is relative");

            assertTrue(exception.getMessage().contains("Cannot resolve boxes from a base box with a relative position"),
                    "Error should match expected for relative position");
        }

        @Test
        @DisplayName("Should calculate boxes correctly given valid widget with relative position and size")
        void calculateSuccessWithRelativePositionAndSize() {
            // Parent box setup
            Box parentBox = new Box(Position.absolute(0, 0), Size.ofPixels(100, 100));

            // Child AbstractWidget setup
            // Box properties
            Border border = Border.all(10);
            Border textureBorder = Border.ZERO;
            Padding padding = Padding.all(5);
            Margin margin = Margin.all(15);

            // Position
            Position childPosition = mock(Position.class);
            when(childPosition.relative()).thenReturn(true);
            when(childPosition.point()).thenReturn(new ImmutablePoint(0, 0));
            when(childPosition.positioning()).thenReturn(Positioning.Relative);

            // Size
            Size childSize = mock(Size.class);
            when(childSize.unit()).thenReturn(Size.Unit.Percent);
            when(childSize.width()).thenReturn(0.5f);
            when(childSize.height()).thenReturn(0.5f);

            // WidgetLayout
            WidgetLayout childLayout = mock(WidgetLayout.class);
            when(childLayout.position()).thenReturn(childPosition);
            when(childLayout.size()).thenReturn(childSize);
            when(childLayout.border()).thenReturn(border);
            when(childLayout.padding()).thenReturn(padding);
            when(childLayout.margin()).thenReturn(margin);

            // Widget
            AbstractWidget widget = mock(AbstractWidget.class);
            when(widget.textureBorder()).thenReturn(textureBorder);
            when(widget.layout()).thenReturn(childLayout);

            // Expected calculated boxes for border box
            int expectedBorderX = 0;
            int expectedBorderY = 0;
            int expectedBorderWidth = 100;
            int expectedBorderHeight = 100;

            // Expected calculated boxes for padding box
            int expectedPaddingX = 10;
            int expectedPaddingY = 10;
            int expectedPaddingWidth = 80;
            int expectedPaddingHeight = 80;

            // Expected calculated boxes for content box
            int expectedContentX = 15;
            int expectedContentY = 15;
            int expectedContentWidth = 70;
            int expectedContentHeight = 70;

            CalculatedBoxes calculatedBoxes = CalculatedBoxes.calculate(widget, parentBox);
            // Border
            assertEquals(expectedBorderX, calculatedBoxes.borderBox().topLeft().xInt());
            assertEquals(expectedBorderY, calculatedBoxes.borderBox().topLeft().yInt());
            assertEquals(expectedBorderWidth, calculatedBoxes.borderBox().size().width());
            assertEquals(expectedBorderHeight, calculatedBoxes.borderBox().size().height());

            // Padding
            assertEquals(expectedPaddingX, calculatedBoxes.paddingBox().topLeft().xInt());
            assertEquals(expectedPaddingY, calculatedBoxes.paddingBox().topLeft().yInt());
            assertEquals(expectedPaddingWidth, calculatedBoxes.paddingBox().size().width());
            assertEquals(expectedPaddingHeight, calculatedBoxes.paddingBox().size().height());

            // Content
            assertEquals(expectedContentX, calculatedBoxes.contentBox().topLeft().xInt());
            assertEquals(expectedContentY, calculatedBoxes.contentBox().topLeft().yInt());
            assertEquals(expectedContentWidth, calculatedBoxes.contentBox().size().width());
            assertEquals(expectedContentHeight, calculatedBoxes.contentBox().size().height());
        }
    }
}