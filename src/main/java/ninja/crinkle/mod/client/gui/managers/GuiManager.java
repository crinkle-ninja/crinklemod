package ninja.crinkle.mod.client.gui.managers;

import ninja.crinkle.mod.client.gui.properties.Rect;
import ninja.crinkle.mod.client.gui.widgets.AbstractContainer;

import java.util.Optional;

public interface GuiManager {
    static GuiManager create() {
        return create(640, 480);
    }
    static GuiManager create(int width, int height) {
        return new DefaultGuiManager(width, height);
    }

    Rect size();

    DragManager dragManager();

    Optional<EventManager> eventManager();

    FocusManager focusManager();

    AbstractContainer root();
}
