package ninja.crinkle.mod.client.gui.events;

import ninja.crinkle.mod.client.gui.widgets.AbstractWidget;

public class DragStartedEvent extends DragEvent {
    public static final Key<DragStartedEvent> KEY = new Key<>();
    private final AbstractWidget widget;

    public DragStartedEvent(EventNode source, double x, double y, int button, double dragX, double dragY,
                            AbstractWidget widget) {
        super(KEY, source, x, y, button, dragX, dragY, null);
        this.widget = widget;
    }

    @Override
    public String toString() {
        return "DragStartedEvent{" +
                "widget=" + widget +
                ", " + super.toString() +
                '}';
    }

    public AbstractWidget widget() {
        return widget;
    }
}