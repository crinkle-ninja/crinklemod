package ninja.crinkle.mod.client.gui.widgets;

import ninja.crinkle.mod.client.gui.managers.GuiManager;
import ninja.crinkle.mod.client.gui.properties.Rect;
import ninja.crinkle.mod.client.gui.properties.Sizing;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("VBoxContainer")
class VBoxContainerTest {
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
        @DisplayName("cross-axis SHRINK_END aligns child to right")
        void crossAxisEnd() {
            VBoxContainer vbox = new VBoxContainer.Builder(root).build();
            root.add(vbox);
            TestWidget child = new TestWidget.Builder(vbox).minSize(30, 20)
                    .horizontalSizing(Sizing.ShrinkEnd).build();
            vbox.add(child);

            vbox.setRect(new Rect(0, 0, 100, 200));

            assertEquals(30, child.rect().width());
            assertEquals(70, child.rect().x(), "aligned to end: 100 - 30 = 70");
        }

        @Test
        @DisplayName("EXPAND children claim leftover space")
        void expanding() {
            VBoxContainer vbox = new VBoxContainer.Builder(root).separation(0).build();
            root.add(vbox);
            TestWidget fixed = new TestWidget.Builder(vbox).minSize(10, 50).build();
            TestWidget expanding = new TestWidget.Builder(vbox).minSize(10, 0)
                    .verticalSizing(Sizing.Expand, Sizing.Fill).build();
            vbox.add(fixed);
            vbox.add(expanding);

            vbox.setRect(new Rect(0, 0, 100, 200));

            assertEquals(new Rect(0, 0, 100, 50), fixed.rect());
            assertEquals(new Rect(0, 50, 100, 150), expanding.rect());
        }

        @Test
        @DisplayName("non-expanding children get min height, placed top to bottom")
        void nonExpanding() {
            VBoxContainer vbox = new VBoxContainer.Builder(root).separation(5).build();
            root.add(vbox);
            TestWidget a = new TestWidget.Builder(vbox).minSize(10, 30).build();
            TestWidget b = new TestWidget.Builder(vbox).minSize(10, 40).build();
            vbox.add(a);
            vbox.add(b);

            vbox.setRect(new Rect(0, 0, 100, 200));

            assertEquals(new Rect(0, 0, 100, 30), a.rect());
            assertEquals(new Rect(0, 35, 100, 40), b.rect());
        }
    }

    @Nested
    @DisplayName("getMinimumSize")
    class MinimumSizeTest {
        @Test
        @DisplayName("height = sum of child min heights + separation gaps")
        void minimumHeight() {
            VBoxContainer vbox = new VBoxContainer.Builder(root).separation(5).build();
            root.add(vbox);
            TestWidget a = new TestWidget.Builder(vbox).minSize(10, 30).build();
            TestWidget b = new TestWidget.Builder(vbox).minSize(10, 40).build();
            vbox.add(a);
            vbox.add(b);

            assertEquals(75, vbox.minimumHeight(), "30 + 40 + 5 gap");
        }

        @Test
        @DisplayName("width = max child min width")
        void minimumWidth() {
            VBoxContainer vbox = new VBoxContainer.Builder(root).build();
            root.add(vbox);
            TestWidget a = new TestWidget.Builder(vbox).minSize(30, 10).build();
            TestWidget b = new TestWidget.Builder(vbox).minSize(50, 10).build();
            vbox.add(a);
            vbox.add(b);

            assertEquals(50, vbox.minimumWidth());
        }
    }
}
