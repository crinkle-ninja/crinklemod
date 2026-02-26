package ninja.crinkle.mod.client.gui.states;

import ninja.crinkle.mod.client.color.Color;
import ninja.crinkle.mod.client.gui.managers.GuiManager;
import ninja.crinkle.mod.client.gui.properties.*;
import ninja.crinkle.mod.client.gui.widgets.AbstractWidget;
import ninja.crinkle.mod.client.gui.widgets.TestWidget;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@DisplayName("BoxBuilder Tests")
class BoxBuilderTest {
    private AbstractWidget createTestWidget(Position position, Size size, Margin margin, Border border,
                                            Padding padding) {
        return TestWidget.builder(GuiManager.create())
                .position(position)
                .size(size)
                .margin(margin)
                .border(border)
                .padding(padding)
                .build();
    }

    private AbstractWidget createTestWidgetWithTextureBorder(Position position, Size size, Margin margin,
                                                             Border border, Padding padding, int textureBorder) {
        TestWidget widget = spy(TestWidget.builder(GuiManager.create())
                .position(position)
                .size(size)
                .margin(margin)
                .border(border)
                .padding(padding)
                .build());
        when(widget.textureBorder()).thenReturn(new Border(textureBorder, Color.TRANSPARENT));
        return widget;

    }

    @Nested
    @DisplayName("Box Property Method Tests")
    class BoxPropertyMethodTests {
        @Test
        @DisplayName("backgroundBox() should shrink the border box by the border")
        void shouldCalculateBackgroundBoxCorrectly() {
            Border border = Border.all(2);
            AbstractWidget widget = createTestWidget(Position.ABSOLUTE_ZERO, Size.ofPixels(50, 50), Margin.ZERO,
                    border, Padding.ZERO);
            Box backgroundBox = widget.calculateBoxes().backgroundBox();
            Box expectedBox = widget.calculateBoxes().borderBox().shrink(border);
            assertEquals(expectedBox, backgroundBox);
        }

        @Test
        @DisplayName("borderBox() should shrink the main box by the margin")
        void shouldCalculateBorderBoxCorrectly() {
            Margin margin = Margin.all(5);
            AbstractWidget widget = createTestWidget(Position.ABSOLUTE_ZERO, Size.ofPixels(50, 50), margin,
                    Border.ZERO, Padding.ZERO);
            Box borderBox = widget.calculateBoxes().borderBox();
            Box expectedBox = widget.calculateBoxes().borderBox().shrink(margin);
            assertEquals(expectedBox, borderBox);
        }

        @Test
        @DisplayName("contentBox() should shrink the padding box by the padding")
        void shouldCalculateContentBoxCorrectly() {
            Padding padding = Padding.all(4);
            AbstractWidget widget = createTestWidget(Position.ABSOLUTE_ZERO, Size.ofPixels(50, 50), Margin.ZERO,
                    Border.ZERO, padding);
            Box contentBox = widget.calculateBoxes().contentBox();
            Box expectedBox = widget.calculateBoxes().paddingBox().shrink(padding);
            assertEquals(expectedBox, contentBox);
        }

        @Test
        @DisplayName("paddingBox() should shrink the background box by the texture border")
        void shouldCalculatePaddingBoxCorrectly() {
            AbstractWidget widget = createTestWidgetWithTextureBorder(Position.absolute(0, 0), Size.ofPixels(50, 50),
                    Margin.ZERO, Border.ZERO, Padding.ZERO, 2);
            Box paddingBox = widget.calculateBoxes().paddingBox();
            Box expectedBox = widget.calculateBoxes().backgroundBox().shrink(widget.textureBorder());
            assertEquals(expectedBox, paddingBox);
        }

        @Test
        @DisplayName("box() should return the outermost box matching layout position and size")
        void shouldReturnOutermostBox() {
            AbstractWidget widget = createTestWidget(Position.absolute(5, 10), Size.ofPixels(50, 50), Margin.ZERO,
                    Border.ZERO, Padding.ZERO);
            Box box = widget.calculateBoxes().borderBox();
            assertEquals(new Box(widget.layout().position(), widget.layout().size()), box);
        }
    }

    @Nested
    @DisplayName("Resolution Method Tests")
    class ResolutionMethodTests {

        @Nested
        @DisplayName("With Parent Box")
        class WithParentBox {
            @Nested
            @DisplayName("Absolute Positioning")
            class AbsolutePositioning {
                @Test
                @DisplayName("should resolve correctly with percentage values")
                void shouldResolveAbsolutePositionWithPercentages() {
                    Margin margin = Margin.all(5);
                    Border border = Border.all(2);
                    Padding padding = Padding.all(4);
                    int textureBorder = 3;
                    AbstractWidget widget = createTestWidgetWithTextureBorder(
                            Position.absolute(10, 20), Size.ofPercent(0.5f, 0.25f), margin, border, padding,
                            textureBorder);
                    Box parentBox = new Box(0, 0, 200, 160, Positioning.Absolute);

                    CalculatedBoxes calculatedBoxes = widget.calculateBoxes(parentBox);

                    Box baseBox = new Box(widget.layout().position(), widget.layout().size().resolve(parentBox));
                    Box expectedBorderBox = baseBox.shrink(margin);
                    Box expectedBackgroundBox = expectedBorderBox.shrink(border);
                    Box expectedPaddingBox = expectedBorderBox.shrink(widget.textureBorder());
                    Box expectedContentBox = expectedPaddingBox.shrink(padding);

                    assertEquals(expectedBorderBox, calculatedBoxes.borderBox());
                    assertEquals(expectedBackgroundBox, calculatedBoxes.backgroundBox());
                    assertEquals(expectedPaddingBox, calculatedBoxes.paddingBox());
                    assertEquals(expectedContentBox, calculatedBoxes.contentBox());
                }

                @Test
                @DisplayName("should resolve correctly with pixel values")
                void shouldResolveAbsolutePositionWithPixels() {
                    Margin margin = Margin.all(5);
                    Border border = Border.all(2);
                    Padding padding = Padding.all(4);
                    int textureBorder = 3;
                    AbstractWidget widget = createTestWidgetWithTextureBorder(
                            Position.absolute(10, 20), Size.ofPixels(100, 80), margin, border, padding, textureBorder);
                    Box parentBox = new Box(0, 0, 800, 600, Positioning.Absolute);

                    CalculatedBoxes calculatedBoxes = widget.calculateBoxes(parentBox);

                    Box baseBox = new Box(widget.layout().position(), widget.layout().size().resolve(parentBox));
                    Box expectedBorderBox = baseBox.shrink(margin);
                    Box expectedBackgroundBox = expectedBorderBox.shrink(border);
                    Box expectedPaddingBox = expectedBorderBox.shrink(widget.textureBorder());
                    Box expectedContentBox = expectedPaddingBox.shrink(padding);

                    assertEquals(expectedBorderBox, calculatedBoxes.borderBox());
                    assertEquals(expectedBackgroundBox, calculatedBoxes.backgroundBox());
                    assertEquals(expectedPaddingBox, calculatedBoxes.paddingBox());
                    assertEquals(expectedContentBox, calculatedBoxes.contentBox());
                }
            }

            @Nested
            @DisplayName("Relative Positioning")
            class RelativePositioning {
                @Test
                @DisplayName("should resolve correctly with percentage values")
                void shouldResolveRelativePositionWithPercentages() {
                    Margin margin = Margin.all(5);
                    Border border = Border.all(2);
                    Padding padding = Padding.all(4);
                    int textureBorder = 3;
                    AbstractWidget widget = createTestWidgetWithTextureBorder(
                            Position.relative(10, 20), Size.ofPercent(0.5f, 0.25f), margin, border, padding,
                            textureBorder);
                    Box parentBox = new Box(50, 60, 200, 160, Positioning.Absolute);

                    CalculatedBoxes calculatedBoxes = widget.calculateBoxes(parentBox);

                    Position expectedPosition = Position.absolute(
                            parentBox.position().point().xInt() + widget.layout().position().point().xInt(),
                            parentBox.position().point().yInt() + widget.layout().position().point().yInt());
                    Box baseBox = new Box(expectedPosition, widget.layout().size().resolve(parentBox));
                    Box expectedBorderBox = baseBox.shrink(margin);
                    Box expectedBackgroundBox = expectedBorderBox.shrink(border);
                    Box expectedPaddingBox = expectedBorderBox.shrink(widget.textureBorder());
                    Box expectedContentBox = expectedPaddingBox.shrink(padding);

                    assertEquals(expectedBorderBox, calculatedBoxes.borderBox());
                    assertEquals(expectedBackgroundBox, calculatedBoxes.backgroundBox());
                    assertEquals(expectedPaddingBox, calculatedBoxes.paddingBox());
                    assertEquals(expectedContentBox, calculatedBoxes.contentBox());
                }

                @Test
                @DisplayName("should resolve correctly with pixel values")
                void shouldResolveRelativePositionWithPixels() {
                    Margin margin = Margin.all(5);
                    Border border = Border.all(2);
                    Padding padding = Padding.all(4);
                    int textureBorder = 3;
                    AbstractWidget widget = createTestWidgetWithTextureBorder(
                            Position.relative(10, 20), Size.ofPixels(100, 80), margin, border, padding, textureBorder);
                    Box parentBox = new Box(50, 60, 800, 600, Positioning.Absolute);

                    CalculatedBoxes calculatedBoxes = widget.calculateBoxes(parentBox);

                    Position expectedPosition = Position.absolute(
                            parentBox.position().point().xInt() + widget.layout().position().point().xInt(),
                            parentBox.position().point().yInt() + widget.layout().position().point().yInt());
                    Box baseBox = new Box(expectedPosition, widget.layout().size().resolve(parentBox));
                    Box expectedBorderBox = baseBox.shrink(margin);
                    Box expectedBackgroundBox = expectedBorderBox.shrink(border);
                    Box expectedPaddingBox = expectedBorderBox.shrink(widget.textureBorder());
                    Box expectedContentBox = expectedPaddingBox.shrink(padding);

                    assertEquals(expectedBorderBox, calculatedBoxes.borderBox());
                    assertEquals(expectedBackgroundBox, calculatedBoxes.backgroundBox());
                    assertEquals(expectedPaddingBox, calculatedBoxes.paddingBox());
                    assertEquals(expectedContentBox, calculatedBoxes.contentBox());
                }
            }
        }
    }
}