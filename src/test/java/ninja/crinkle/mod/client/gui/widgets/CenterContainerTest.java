package ninja.crinkle.mod.client.gui.widgets;

import ninja.crinkle.mod.client.gui.managers.GuiManager;
import ninja.crinkle.mod.client.gui.properties.Rect;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("CenterContainer")
class CenterContainerTest {
    private GuiManager manager;
    private Container root;

    @Test
    @DisplayName("child is centered at its minimum size")
    void childCentered() {
        CenterContainer cc = new CenterContainer.Builder(root).build();
        root.add(cc);
        TestWidget child = new TestWidget.Builder(cc).minSize(40, 20).build();
        cc.add(child);

        cc.setRect(new Rect(0, 0, 200, 100));

        // x = (200-40)/2 = 80, y = (100-20)/2 = 40
        assertEquals(new Rect(80, 40, 40, 20), child.rect());
    }

    @Test
    @DisplayName("child centered with container offset")
    void childCenteredWithOffset() {
        CenterContainer cc = new CenterContainer.Builder(root).build();
        root.add(cc);
        TestWidget child = new TestWidget.Builder(cc).minSize(40, 20).build();
        cc.add(child);

        cc.setRect(new Rect(100, 50, 200, 100));

        // x = 100 + (200-40)/2 = 180, y = 50 + (100-20)/2 = 90
        assertEquals(new Rect(180, 90, 40, 20), child.rect());
    }

    @Test
    @DisplayName("minimum size matches child minimum size")
    void minimumSize() {
        CenterContainer cc = new CenterContainer.Builder(root).build();
        root.add(cc);
        TestWidget child = new TestWidget.Builder(cc).minSize(60, 40).build();
        cc.add(child);

        assertEquals(60, cc.minimumWidth());
        assertEquals(40, cc.minimumHeight());
    }

    @BeforeEach
    void setUp() {
        manager = GuiManager.create();
        root = new Container.Builder(manager).build();
    }
}
