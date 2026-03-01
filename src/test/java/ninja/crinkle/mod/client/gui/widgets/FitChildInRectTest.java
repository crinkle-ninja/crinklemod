package ninja.crinkle.mod.client.gui.widgets;

import ninja.crinkle.mod.client.gui.layouts.SizeFlags;
import ninja.crinkle.mod.client.gui.managers.GuiManager;
import ninja.crinkle.mod.client.gui.properties.Rect;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests fitChildInRect behavior via VBoxContainer (horizontal = cross-axis, so hSizeFlags
 * are resolved by fitChildInRect) and HBoxContainer (vertical = cross-axis, so vSizeFlags
 * are resolved by fitChildInRect).
 */
@DisplayName("fitChildInRect")
class FitChildInRectTest {
    private GuiManager manager;
    private Container root;

    @BeforeEach
    void setUp() {
        manager = GuiManager.create();
        root = new Container.Builder(manager).build();
    }

    @Nested
    @DisplayName("Horizontal cross-axis (via VBoxContainer)")
    class HorizontalAxis {
        @Test
        @DisplayName("FILL uses full allocated width")
        void fillUsesFullWidth() {
            VBoxContainer vbox = new VBoxContainer.Builder(root).build();
            root.add(vbox);
            TestWidget child = new TestWidget.Builder(vbox).minSize(30, 10)
                    .hSizeFlags(SizeFlags.Fill).build();
            vbox.add(child);
            vbox.setRect(new Rect(0, 0, 100, 50));

            assertEquals(100, child.rect().width());
        }

        @Test
        @DisplayName("SHRINK_BEGIN uses min width, aligned to start")
        void shrinkBegin() {
            VBoxContainer vbox = new VBoxContainer.Builder(root).build();
            root.add(vbox);
            TestWidget child = new TestWidget.Builder(vbox).minSize(30, 10)
                    .hSizeFlags(SizeFlags.ShrinkBegin).build();
            vbox.add(child);
            vbox.setRect(new Rect(10, 0, 100, 50));

            assertEquals(30, child.rect().width());
            assertEquals(10, child.rect().x());
        }

        @Test
        @DisplayName("SHRINK_CENTER uses min width, centered")
        void shrinkCenter() {
            VBoxContainer vbox = new VBoxContainer.Builder(root).build();
            root.add(vbox);
            TestWidget child = new TestWidget.Builder(vbox).minSize(30, 10)
                    .hSizeFlags(SizeFlags.ShrinkCenter).build();
            vbox.add(child);
            vbox.setRect(new Rect(0, 0, 100, 50));

            assertEquals(30, child.rect().width());
            assertEquals(35, child.rect().x(), "(100-30)/2 = 35");
        }

        @Test
        @DisplayName("SHRINK_END uses min width, aligned to end")
        void shrinkEnd() {
            VBoxContainer vbox = new VBoxContainer.Builder(root).build();
            root.add(vbox);
            TestWidget child = new TestWidget.Builder(vbox).minSize(30, 10)
                    .hSizeFlags(SizeFlags.ShrinkEnd).build();
            vbox.add(child);
            vbox.setRect(new Rect(0, 0, 100, 50));

            assertEquals(30, child.rect().width());
            assertEquals(70, child.rect().x(), "100 - 30 = 70");
        }
    }

    @Nested
    @DisplayName("Vertical cross-axis (via HBoxContainer)")
    class VerticalAxis {
        @Test
        @DisplayName("FILL uses full allocated height")
        void fillUsesFullHeight() {
            HBoxContainer hbox = new HBoxContainer.Builder(root).build();
            root.add(hbox);
            TestWidget child = new TestWidget.Builder(hbox).minSize(10, 20)
                    .vSizeFlags(SizeFlags.Fill).build();
            hbox.add(child);
            hbox.setRect(new Rect(0, 0, 100, 80));

            assertEquals(80, child.rect().height());
        }

        @Test
        @DisplayName("SHRINK_BEGIN uses min height, aligned to top")
        void shrinkBegin() {
            HBoxContainer hbox = new HBoxContainer.Builder(root).build();
            root.add(hbox);
            TestWidget child = new TestWidget.Builder(hbox).minSize(10, 20)
                    .vSizeFlags(SizeFlags.ShrinkBegin).build();
            hbox.add(child);
            hbox.setRect(new Rect(0, 10, 100, 80));

            assertEquals(20, child.rect().height());
            assertEquals(10, child.rect().y());
        }

        @Test
        @DisplayName("SHRINK_CENTER uses min height, centered")
        void shrinkCenter() {
            HBoxContainer hbox = new HBoxContainer.Builder(root).build();
            root.add(hbox);
            TestWidget child = new TestWidget.Builder(hbox).minSize(10, 20)
                    .vSizeFlags(SizeFlags.ShrinkCenter).build();
            hbox.add(child);
            hbox.setRect(new Rect(0, 0, 100, 80));

            assertEquals(20, child.rect().height());
            assertEquals(30, child.rect().y(), "(80-20)/2 = 30");
        }

        @Test
        @DisplayName("SHRINK_END uses min height, aligned to bottom")
        void shrinkEnd() {
            HBoxContainer hbox = new HBoxContainer.Builder(root).build();
            root.add(hbox);
            TestWidget child = new TestWidget.Builder(hbox).minSize(10, 20)
                    .vSizeFlags(SizeFlags.ShrinkEnd).build();
            hbox.add(child);
            hbox.setRect(new Rect(0, 0, 100, 80));

            assertEquals(20, child.rect().height());
            assertEquals(60, child.rect().y(), "80 - 20 = 60");
        }
    }

    @Nested
    @DisplayName("Combined axes (via CenterContainer-like setup)")
    class CombinedAxes {
        @Test
        @DisplayName("SHRINK_CENTER on both axes centers in both dimensions")
        void shrinkCenterBoth() {
            // Use MarginContainer with 0 margins — it calls fitChildInRect on both axes
            MarginContainer mc = new MarginContainer.Builder(root).margins(0).build();
            root.add(mc);
            TestWidget child = new TestWidget.Builder(mc).minSize(30, 20)
                    .hSizeFlags(SizeFlags.ShrinkCenter)
                    .vSizeFlags(SizeFlags.ShrinkCenter).build();
            mc.add(child);
            mc.setRect(new Rect(0, 0, 100, 80));

            assertEquals(new Rect(35, 30, 30, 20), child.rect());
        }

        @Test
        @DisplayName("FILL horizontal, SHRINK_END vertical")
        void fillHShrinkEndV() {
            MarginContainer mc = new MarginContainer.Builder(root).margins(0).build();
            root.add(mc);
            TestWidget child = new TestWidget.Builder(mc).minSize(30, 20)
                    .hSizeFlags(SizeFlags.Fill)
                    .vSizeFlags(SizeFlags.ShrinkEnd).build();
            mc.add(child);
            mc.setRect(new Rect(0, 0, 100, 80));

            assertEquals(100, child.rect().width());
            assertEquals(20, child.rect().height());
            assertEquals(60, child.rect().y());
        }
    }
}
