package ninja.crinkle.mod.client.gui.events;

import ninja.crinkle.mod.client.gui.widgets.AbstractWidget;

public class DragStoppedEvent extends DragEvent {
    public static final Key<DragStoppedEvent> KEY = new Key<>();
    private final AbstractWidget widget;

    public DragStoppedEvent(EventNode source, double x, double y, int button, double dragX, double dragY,
                            AbstractWidget widget) {
        super(KEY, source, x, y, button, dragX, dragY, null);
        this.widget = widget;
    }

    @Override
    public String toString() {
        return "DragStoppedEvent{" +
                "widget=" + widget +
                ", " + super.toString() +
                '}';
    }

    public AbstractWidget widget() {
        return widget;
    }
}