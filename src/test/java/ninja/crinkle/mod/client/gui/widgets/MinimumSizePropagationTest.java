package ninja.crinkle.mod.client.gui.widgets;

import ninja.crinkle.mod.client.gui.managers.GuiManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Minimum size propagation")
class MinimumSizePropagationTest {
    private GuiManager manager;
    private Container root;

    @BeforeEach
    void setUp() {
        manager = GuiManager.create();
        root = new Container.Builder(manager).build();
    }

    @Test
    @DisplayName("VBox min height = sum of children + gaps, propagates up through nesting")
    void nestedVBoxPropagation() {
        VBoxContainer outer = new VBoxContainer.Builder(root).separation(10).build();
        root.add(outer);

        VBoxContainer inner = new VBoxContainer.Builder(outer).separation(5).build();
        outer.add(inner);

        TestWidget a = new TestWidget.Builder(inner).minSize(20, 30).build();
        TestWidget b = new TestWidget.Builder(inner).minSize(20, 40).build();
        inner.add(a);
        inner.add(b);

        TestWidget c = new TestWidget.Builder(outer).minSize(20, 50).build();
        outer.add(c);

        // inner min height = 30 + 40 + 5 = 75
        assertEquals(75, inner.getMinimumHeight());
        // outer min height = 75 + 50 + 10 = 135
        assertEquals(135, outer.getMinimumHeight());
    }

    @Test
    @DisplayName("HBox min width = sum of children + gaps, propagates up through nesting")
    void nestedHBoxPropagation() {
        HBoxContainer outer = new HBoxContainer.Builder(root).separation(10).build();
        root.add(outer);

        HBoxContainer inner = new HBoxContainer.Builder(outer).separation(5).build();
        outer.add(inner);

        TestWidget a = new TestWidget.Builder(inner).minSize(30, 20).build();
        TestWidget b = new TestWidget.Builder(inner).minSize(40, 20).build();
        inner.add(a);
        inner.add(b);

        TestWidget c = new TestWidget.Builder(outer).minSize(50, 20).build();
        outer.add(c);

        // inner min width = 30 + 40 + 5 = 75
        assertEquals(75, inner.getMinimumWidth());
        // outer min width = 75 + 50 + 10 = 135
        assertEquals(135, outer.getMinimumWidth());
    }

    @Test
    @DisplayName("MarginContainer adds margins to child min size")
    void marginContainerPropagation() {
        VBoxContainer vbox = new VBoxContainer.Builder(root).build();
        root.add(vbox);

        MarginContainer mc = new MarginContainer.Builder(vbox).margins(10, 20, 30, 40).build();
        vbox.add(mc);

        TestWidget child = new TestWidget.Builder(mc).minSize(50, 50).build();
        mc.add(child);

        // mc min width = 50 + 40 + 20 = 110, min height = 50 + 10 + 30 = 90
        assertEquals(110, mc.getMinimumWidth());
        assertEquals(90, mc.getMinimumHeight());
        // vbox passes through the max child min
        assertEquals(110, vbox.getMinimumWidth());
        assertEquals(90, vbox.getMinimumHeight());
    }
}
