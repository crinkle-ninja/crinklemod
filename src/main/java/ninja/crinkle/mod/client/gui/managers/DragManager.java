package ninja.crinkle.mod.client.gui.managers;

import com.mojang.logging.LogUtils;
import ninja.crinkle.mod.client.gui.events.*;
import ninja.crinkle.mod.client.gui.widgets.AbstractWidget;
import org.slf4j.Logger;

import java.util.List;
import java.util.function.Predicate;

public class DragManager implements EventNode {
    public static final int Z_MAX = 5000;
    public static final int Z_MIN = 0;
    public static final int Z_STEP = 10;
    private static final Logger LOGGER = LogUtils.getLogger();
    private final EventManager eventManager;
    private AbstractWidget current;
    private boolean dragging;

    public DragManager(EventManager eventManager) {
        this.eventManager = eventManager;
        eventManager.addListener(DragStartedEvent.KEY, EventManager.PRIORITY_OVERRIDE, this::onDragStarted);
        eventManager.addListener(DragStoppedEvent.KEY, EventManager.PRIORITY_OVERRIDE, this::onDragStopped);
    }

    public void onDragStarted(DragStartedEvent event) {
        if (event.cancelled()) return;
        drag(event.widget());
        dragging(true);
        event.consumer(this);
    }

    public void onDragStopped(DragStoppedEvent event) {
        if (event.cancelled()) return;
        if (current() == null) return;
        drop(event, eventManager().listeners(onlyOverlapping(current())));
        dragging(false);
        event.consumer(this);
    }

    public void drag(AbstractWidget widget) {
        current(widget);
    }

    public void dragging(boolean dragging) {
        this.dragging = dragging;
    }

    public AbstractWidget current() {
        return current;
    }

    public void drop(DragStoppedEvent event, List<EventNode> listeners) {
        if (current() == null) {
            return;
        }
        Event dropEvent = new DroppedEvent(this, event.x(), event.y(), event.button().button(),
                current(), listeners.stream().filter(onlyOverlapping(current())).toList());
        current(null);
        eventManager().dispatchEvent(dropEvent);
    }

    public EventManager eventManager() {
        return eventManager;
    }

    @Override
    public String name() {
        return "DragManager";
    }

    private Predicate<EventNode> onlyOverlapping(EventNode other) {
        return (l) -> {
            if (!(l instanceof AbstractWidget widget) || !(other instanceof AbstractWidget otherWidget))
                return false;
            ninja.crinkle.mod.client.gui.properties.Rect a = widget.rect();
            ninja.crinkle.mod.client.gui.properties.Rect b = otherWidget.rect();
            return a.x() < b.right() && a.right() > b.x() && a.y() < b.bottom() && a.bottom() > b.y();
        };
    }

    public void current(AbstractWidget current) {
        this.current = current;
    }

    public boolean dragging() {
        return dragging;
    }
}