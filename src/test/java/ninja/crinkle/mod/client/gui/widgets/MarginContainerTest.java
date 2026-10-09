package ninja.crinkle.mod.client.gui.widgets;

import ninja.crinkle.mod.client.gui.managers.GuiManager;
import ninja.crinkle.mod.client.gui.properties.Rect;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("MarginContainer")
class MarginContainerTest {
    private GuiManager manager;
    private Container root;

    @Test
    @DisplayName("child rect is shrunk by margins")
    void childShrunkByMargins() {
        MarginContainer mc = new MarginContainer.Builder(root).margins(10, 20, 30, 40).build();
        root.add(mc);
        TestWidget child = new TestWidget.Builder(mc).minSize(50, 50).build();
        mc.add(child);

        mc.setRect(new Rect(0, 0, 200, 200));

        // Inner rect: x=40, y=10, w=200-40-20=140, h=200-10-30=160
        // Child has FILL by default, so it takes the full inner rect
        assertEquals(new Rect(40, 10, 140, 160), child.rect());
    }

    @Test
    @DisplayName("minimum size includes margins")
    void minimumSizeIncludesMargins() {
        MarginContainer mc = new MarginContainer.Builder(root).margins(10, 20, 30, 40).build();
        root.add(mc);
        TestWidget child = new TestWidget.Builder(mc).minSize(50, 50).build();
        mc.add(child);

        // min width = 50 + 40 + 20 = 110, min height = 50 + 10 + 30 = 90
        assertEquals(110, mc.minimumWidth());
        assertEquals(90, mc.minimumHeight());
    }

    @BeforeEach
    void setUp() {
        manager = GuiManager.create();
        root = new Container.Builder(manager).build();
    }

    @Test
    @DisplayName("uniform margins")
    void uniformMargins() {
        MarginContainer mc = new MarginContainer.Builder(root).margins(15).build();
        root.add(mc);
        TestWidget child = new TestWidget.Builder(mc).minSize(10, 10).build();
        mc.add(child);

        mc.setRect(new Rect(0, 0, 100, 100));

        assertEquals(new Rect(15, 15, 70, 70), child.rect());
    }
}
