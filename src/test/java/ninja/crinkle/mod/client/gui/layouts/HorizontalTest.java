package ninja.crinkle.mod.client.gui.layouts;

import ninja.crinkle.mod.client.gui.properties.*;
import ninja.crinkle.mod.client.gui.states.CalculatedBoxes;
import ninja.crinkle.mod.client.gui.states.WidgetLayout;
import ninja.crinkle.mod.client.gui.widgets.AbstractWidget;
import ninja.crinkle.mod.client.gui.widgets.Container;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("Horizontal Layout Tests")
class HorizontalTest {

    @Nested
    @DisplayName("arrange() Method Tests")
    class ArrangeMethodTests {
        @Test
        @DisplayName("Should arrange a single child correctly")
        void shouldArrangeSingleChild() {
            // Arrange
            Horizontal horizontal = mock(Horizontal.class);
            doCallRealMethod().when(horizontal).arrange(any(Container.class));

            CalculatedBoxes parentCalculatedBoxes = mock(CalculatedBoxes.class);
            when(parentCalculatedBoxes.contentBox()).thenReturn(new Box(Position.absolute(10, 10), Size.ofPixels(100, 100)));

            Container parent = mock(Container.class);
            when(parent.cachedBoxes()).thenReturn(parentCalculatedBoxes);

            WidgetLayout childLayout = mock(WidgetLayout.class);
            when(childLayout.position()).thenReturn(Position.relative(0, 0));
            when(childLayout.size()).thenReturn(Size.ofPercent(0.5f, 0.5f));
            when(childLayout.margin()).thenReturn(Margin.all(5));
            when(childLayout.border()).thenReturn(Border.all(5));
            when(childLayout.padding()).thenReturn(Padding.all(5));

            AbstractWidget child = mock(AbstractWidget.class);
            when(child.calculateBoxes()).thenCallRealMethod();
            when(child.calculateBoxes(any(Box.class))).thenCallRealMethod();
            when(child.cachedBoxes()).thenCallRealMethod();
            doCallRealMethod().when(child).cachedBoxes(any(CalculatedBoxes.class));
            when(child.layout()).thenReturn(childLayout);
            when(child.textureBorder()).thenReturn(Border.ZERO);

            when(parent.children()).thenReturn(List.of(child));
            when(parent.children(notNull())).thenCallRealMethod();

            // Expectations
            int expectedChildX = 10;
            int expectedChildY = 10;
            int expectedChildWidth = 50;
            int expectedChildHeight = 50;

            horizontal.arrange(parent);

            verify(child, times(1)).cachedBoxes(any(CalculatedBoxes.class));

            assertEquals(expectedChildX, child.cachedBoxes().borderBox().topLeft().xInt());
            assertEquals(expectedChildY, child.cachedBoxes().borderBox().topLeft().yInt());
            assertEquals(expectedChildWidth, child.cachedBoxes().borderBox().size().width());
            assertEquals(expectedChildHeight, child.cachedBoxes().borderBox().size().height());
        }

        @Test
        @DisplayName("Should arrange multiple children correctly")
        void shouldArrangeMultipleChildren() {
            // Arrange
            Horizontal horizontal = mock(Horizontal.class);
            doCallRealMethod().when(horizontal).arrange(any(Container.class));

            CalculatedBoxes parentCalculatedBoxes = mock(CalculatedBoxes.class);
            when(parentCalculatedBoxes.contentBox()).thenReturn(new Box(Position.absolute(10, 10), Size.ofPixels(100, 100)));

            Container parent = mock(Container.class);
            when(parent.cachedBoxes()).thenReturn(parentCalculatedBoxes);

            WidgetLayout child1Layout = mock(WidgetLayout.class);
            when(child1Layout.position()).thenReturn(Position.relative(0, 0));
            when(child1Layout.size()).thenReturn(Size.ofPercent(0.6f, 0.6f));
            when(child1Layout.margin()).thenReturn(Margin.all(5));
            when(child1Layout.border()).thenReturn(Border.all(5));
            when(child1Layout.padding()).thenReturn(Padding.all(5));

            WidgetLayout child2Layout = mock(WidgetLayout.class);
            when(child2Layout.position()).thenReturn(Position.relative(0, 0));
            when(child2Layout.size()).thenReturn(Size.ofPercent(0.4f, 0.4f));
            when(child2Layout.margin()).thenReturn(Margin.all(5));
            when(child2Layout.border()).thenReturn(Border.all(5));
            when(child2Layout.padding()).thenReturn(Padding.all(5));

            AbstractWidget child1 = mock(AbstractWidget.class);
            when(child1.calculateBoxes()).thenCallRealMethod();
            when(child1.calculateBoxes(any(Box.class))).thenCallRealMethod();
            when(child1.cachedBoxes()).thenCallRealMethod();
            doCallRealMethod().when(child1).cachedBoxes(any(CalculatedBoxes.class));
            when(child1.layout()).thenReturn(child1Layout);
            when(child1.textureBorder()).thenReturn(Border.ZERO);

            AbstractWidget child2 = mock(AbstractWidget.class);
            when(child2.calculateBoxes()).thenCallRealMethod();
            when(child2.calculateBoxes(any(Box.class))).thenCallRealMethod();
            when(child2.cachedBoxes()).thenCallRealMethod();
            doCallRealMethod().when(child2).cachedBoxes(any(CalculatedBoxes.class));
            when(child2.layout()).thenReturn(child2Layout);
            when(child2.textureBorder()).thenReturn(Border.ZERO);

            when(parent.children()).thenReturn(List.of(child1, child2));
            when(parent.children(notNull())).thenCallRealMethod();

            // Expectations
            int expectedChild1X = 15;
            int expectedChild1Y = 15;
            int expectedChild1Width = 60;
            int expectedChild1Height = 60;
            int expectedChild2X = 75;
            int expectedChild2Y = 15;
            int expectedChild2Width = 40;
            int expectedChild2Height = 40;
            horizontal.arrange(parent);

            verify(child1, times(1)).cachedBoxes(any(CalculatedBoxes.class));

            assertEquals(expectedChild1X, child1.cachedBoxes().borderBox().topLeft().xInt());
            assertEquals(expectedChild1Y, child1.cachedBoxes().borderBox().topLeft().yInt());
            assertEquals(expectedChild1Width, child1.cachedBoxes().borderBox().size().width());
            assertEquals(expectedChild1Height, child1.cachedBoxes().borderBox().size().height());

            assertEquals(expectedChild2X, child2.cachedBoxes().borderBox().topLeft().xInt());
            assertEquals(expectedChild2Y, child2.cachedBoxes().borderBox().topLeft().yInt());
            assertEquals(expectedChild2Width, child2.cachedBoxes().borderBox().size().width());
            assertEquals(expectedChild2Height, child2.cachedBoxes().borderBox().size().height());
        }

        @Test
        @DisplayName("Should handle empty container without errors")
        void shouldHandleEmptyContainer() {
        }
    }
}