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
        root = Container.builder(manager).build();
    }

    @Test
    @DisplayName("VBox min height = sum of children + gaps, propagates up through nesting")
    void nestedVBoxPropagation() {
        VBoxContainer outer = VBoxContainer.builder(root).separation(10).build();
        root.add(outer);

        VBoxContainer inner = VBoxContainer.builder(outer).separation(5).build();
        outer.add(inner);

        TestWidget a = TestWidget.builder(inner).minSize(20, 30).build();
        TestWidget b = TestWidget.builder(inner).minSize(20, 40).build();
        inner.add(a);
        inner.add(b);

        TestWidget c = TestWidget.builder(outer).minSize(20, 50).build();
        outer.add(c);

        // inner min height = 30 + 40 + 5 = 75
        assertEquals(75, inner.getMinimumHeight());
        // outer min height = 75 + 50 + 10 = 135
        assertEquals(135, outer.getMinimumHeight());
    }

    @Test
    @DisplayName("HBox min width = sum of children + gaps, propagates up through nesting")
    void nestedHBoxPropagation() {
        HBoxContainer outer = HBoxContainer.builder(root).separation(10).build();
        root.add(outer);

        HBoxContainer inner = HBoxContainer.builder(outer).separation(5).build();
        outer.add(inner);

        TestWidget a = TestWidget.builder(inner).minSize(30, 20).build();
        TestWidget b = TestWidget.builder(inner).minSize(40, 20).build();
        inner.add(a);
        inner.add(b);

        TestWidget c = TestWidget.builder(outer).minSize(50, 20).build();
        outer.add(c);

        // inner min width = 30 + 40 + 5 = 75
        assertEquals(75, inner.getMinimumWidth());
        // outer min width = 75 + 50 + 10 = 135
        assertEquals(135, outer.getMinimumWidth());
    }

    @Test
    @DisplayName("MarginContainer adds margins to child min size")
    void marginContainerPropagation() {
        VBoxContainer vbox = VBoxContainer.builder(root).build();
        root.add(vbox);

        MarginContainer mc = MarginContainer.builder(vbox).margins(10, 20, 30, 40).build();
        vbox.add(mc);

        TestWidget child = TestWidget.builder(mc).minSize(50, 50).build();
        mc.add(child);

        // mc min width = 50 + 40 + 20 = 110, min height = 50 + 10 + 30 = 90
        assertEquals(110, mc.getMinimumWidth());
        assertEquals(90, mc.getMinimumHeight());
        // vbox passes through the max child min
        assertEquals(110, vbox.getMinimumWidth());
        assertEquals(90, vbox.getMinimumHeight());
    }
}
