package ninja.crinkle.mod.client.gui.widgets;

import ninja.crinkle.mod.client.gui.managers.GuiManager;
import ninja.crinkle.mod.client.gui.properties.Rect;
import ninja.crinkle.mod.client.gui.properties.Sizing;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("HBoxContainer")
class HBoxContainerTest {
    private GuiManager manager;
    private Container root;

    @BeforeEach
    void setUp() {
        manager = GuiManager.create();
        root = new Container.Builder(manager).build();
    }

    @Nested
    @DisplayName("arrange")
    class ArrangeTest {
        @Test
        @DisplayName("cross-axis SHRINK_CENTER centers child vertically")
        void crossAxisCenter() {
            HBoxContainer hbox = new HBoxContainer.Builder(root).build();
            root.add(hbox);
            TestWidget child = new TestWidget.Builder(hbox).minSize(30, 20)
                    .verticalSizing(Sizing.ShrinkCenter).build();
            hbox.add(child);

            hbox.setRect(new Rect(0, 0, 200, 100));

            assertEquals(20, child.rect().height());
            assertEquals(40, child.rect().y(), "centered: (100-20)/2 = 40");
        }

        @Test
        @DisplayName("EXPAND children claim leftover space")
        void expanding() {
            HBoxContainer hbox = new HBoxContainer.Builder(root).separation(0).build();
            root.add(hbox);
            TestWidget fixed = new TestWidget.Builder(hbox).minSize(50, 10).build();
            TestWidget expanding = new TestWidget.Builder(hbox).minSize(0, 10)
                    .horizontalSizing(Sizing.Expand, Sizing.Fill).build();
            hbox.add(fixed);
            hbox.add(expanding);

            hbox.setRect(new Rect(0, 0, 200, 50));

            assertEquals(new Rect(0, 0, 50, 50), fixed.rect());
            assertEquals(new Rect(50, 0, 150, 50), expanding.rect());
        }

        @Test
        @DisplayName("multiple EXPAND children distribute by stretchRatio")
        void multipleExpanding() {
            HBoxContainer hbox = new HBoxContainer.Builder(root).separation(0).build();
            root.add(hbox);
            TestWidget a = new TestWidget.Builder(hbox).minSize(0, 10)
                    .horizontalSizing(Sizing.Expand, Sizing.Fill).stretchRatio(1.0f).build();
            TestWidget b = new TestWidget.Builder(hbox).minSize(0, 10)
                    .horizontalSizing(Sizing.Expand, Sizing.Fill).stretchRatio(2.0f).build();
            hbox.add(a);
            hbox.add(b);

            hbox.setRect(new Rect(0, 0, 300, 50));

            // a gets 100 (1/3 of 300), b gets 200 (2/3 of 300)
            assertEquals(100, a.rect().width());
            assertEquals(200, b.rect().width());
        }

        @Test
        @DisplayName("non-expanding children get min width, placed left to right")
        void nonExpanding() {
            HBoxContainer hbox = new HBoxContainer.Builder(root).separation(5).build();
            root.add(hbox);
            TestWidget a = new TestWidget.Builder(hbox).minSize(30, 10).build();
            TestWidget b = new TestWidget.Builder(hbox).minSize(40, 10).build();
            hbox.add(a);
            hbox.add(b);

            hbox.setRect(new Rect(0, 0, 200, 50));

            assertEquals(new Rect(0, 0, 30, 50), a.rect());
            assertEquals(new Rect(35, 0, 40, 50), b.rect());
        }

        @Test
        @DisplayName("separation gaps between children")
        void separationGaps() {
            HBoxContainer hbox = new HBoxContainer.Builder(root).separation(10).build();
            root.add(hbox);
            TestWidget a = new TestWidget.Builder(hbox).minSize(20, 10).build();
            TestWidget b = new TestWidget.Builder(hbox).minSize(20, 10).build();
            TestWidget c = new TestWidget.Builder(hbox).minSize(20, 10).build();
            hbox.add(a);
            hbox.add(b);
            hbox.add(c);

            hbox.setRect(new Rect(0, 0, 200, 50));

            assertEquals(0, a.rect().x());
            assertEquals(30, b.rect().x(), "20 + 10 gap");
            assertEquals(60, c.rect().x(), "20 + 10 + 20 + 10");
        }
    }

    @Nested
    @DisplayName("getMinimumSize")
    class MinimumSizeTest {
        @Test
        @DisplayName("empty container returns 0")
        void emptyContainer() {
            HBoxContainer hbox = new HBoxContainer.Builder(root).build();
            root.add(hbox);
            assertEquals(0, hbox.minimumWidth());
            assertEquals(0, hbox.minimumHeight());
        }

        @Test
        @DisplayName("height = max child min height")
        void minimumHeight() {
            HBoxContainer hbox = new HBoxContainer.Builder(root).build();
            root.add(hbox);
            TestWidget a = new TestWidget.Builder(hbox).minSize(30, 10).build();
            TestWidget b = new TestWidget.Builder(hbox).minSize(40, 25).build();
            hbox.add(a);
            hbox.add(b);

            assertEquals(25, hbox.minimumHeight());
        }

        @Test
        @DisplayName("width = sum of child min widths + separation gaps")
        void minimumWidth() {
            HBoxContainer hbox = new HBoxContainer.Builder(root).separation(5).build();
            root.add(hbox);
            TestWidget a = new TestWidget.Builder(hbox).minSize(30, 10).build();
            TestWidget b = new TestWidget.Builder(hbox).minSize(40, 10).build();
            hbox.add(a);
            hbox.add(b);

            assertEquals(75, hbox.minimumWidth(), "30 + 40 + 5 gap");
        }
    }
}
