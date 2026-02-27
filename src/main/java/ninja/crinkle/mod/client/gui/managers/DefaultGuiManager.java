package ninja.crinkle.mod.client.gui.managers;

import ninja.crinkle.mod.client.gui.layouts.Horizontal;
import ninja.crinkle.mod.client.gui.properties.Position;
import ninja.crinkle.mod.client.gui.properties.Scope;
import ninja.crinkle.mod.client.gui.properties.Size;
import ninja.crinkle.mod.client.gui.widgets.Container;

import java.util.Optional;

public class DefaultGuiManager implements GuiManager {
    private final Container root;
    private final DragManager dragManager;
    private final EventManager eventManager;
    private final FocusManager focusManager;

    public DefaultGuiManager(int width, int height) {
        this.eventManager = new EventManager(Scope.Screen);
        this.dragManager = new DragManager(this.eventManager);
        this.focusManager = new FocusManager();
        this.root = new Container.Builder(this)
                .name("root")
                .position(Position.absolute(0, 0))
                .size(Size.ofPixels(width, height))
                .layoutManager(Horizontal.builder().build())
                .build();
    }

    @Override
    public Size size() {
        return root().layout().size();
    }

    @Override
    public DragManager dragManager() {
        return dragManager;
    }

    @Override
    public Optional<EventManager> eventManager() {
        return Optional.of(eventManager);
    }

    @Override
    public FocusManager focusManager() {
        return focusManager;
    }

    @Override
    public Container root() {
        return root;
    }
}
