package ninja.crinkle.mod.client.gui.events;

import ninja.crinkle.mod.client.gui.events.listeners.EventListener;
import ninja.crinkle.mod.client.gui.events.sources.EventSource;
import ninja.crinkle.mod.client.gui.properties.Scope;
import ninja.crinkle.mod.client.gui.widgets.AbstractWidget;

import java.util.List;

public class DroppedEvent extends DragEvent {
    private final AbstractWidget widget;
    public DroppedEvent(Scope scope, EventSource source, double x, double y, int button, AbstractWidget widget, List<EventListener> listeners) {
        super(Type.Dropped, scope, source, x, y, button, 0, 0, listeners);
        this.widget = widget;
    }

    public AbstractWidget widget() {
        return widget;
    }

    @Override
    public String toString() {
        return "DroppedEvent{" +
                "widget=" + widget.name() +
                ", widgetRect=" + widget.rect() +
                ", x=" + x() +
                ", y=" + y() +
                ", button=" + button() +
                ", scope=" + scope() +
                ", source=" + source() +
                '}';
    }
}
