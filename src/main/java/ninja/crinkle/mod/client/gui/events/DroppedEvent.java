package ninja.crinkle.mod.client.gui.events;

import ninja.crinkle.mod.client.gui.widgets.AbstractWidget;

import java.util.List;

public class DroppedEvent extends DragEvent {
    public static final Key<DroppedEvent> KEY = new Key<>();
    private final AbstractWidget widget;

    public DroppedEvent(EventNode source, double x, double y, int button, AbstractWidget widget,
                        List<EventNode> listeners) {
        super(KEY, source, x, y, button, 0, 0, listeners);
        this.widget = widget;
    }

    @Override
    public String toString() {
        return "DroppedEvent{" +
                "widget=" + widget.name() +
                ", widgetRect=" + widget.rect() +
                ", x=" + x() +
                ", y=" + y() +
                ", button=" + button() +
                ", source=" + source() +
                '}';
    }

    public AbstractWidget widget() {
        return widget;
    }
}