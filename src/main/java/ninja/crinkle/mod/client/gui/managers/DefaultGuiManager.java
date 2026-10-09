package ninja.crinkle.mod.client.gui.managers;

import ninja.crinkle.mod.client.gui.properties.Rect;
import ninja.crinkle.mod.client.gui.widgets.Container;

public class DefaultGuiManager implements GuiManager {
    private final DragManager dragManager;
    private final EventManager eventManager;
    private final FocusManager focusManager;
    private final Container root;

    public DefaultGuiManager(int width, int height) {
        this.eventManager = new EventManager();
        this.dragManager = new DragManager(this.eventManager);
        this.focusManager = new FocusManager();
        this.root = new Container.Builder(this)
                .name("root")
                .minSize(width, height)
                .build();
        this.root.setRect(new Rect(0, 0, width, height));
    }

    @Override
    public DragManager dragManager() {
        return dragManager;
    }

    @Override
    public EventManager eventManager() {
        return eventManager;
    }

    @Override
    public FocusManager focusManager() {
        return focusManager;
    }

    @Override
    public Container root() {
        return root;
    }

    @Override
    public Rect size() {
        return root().rect();
    }
}
