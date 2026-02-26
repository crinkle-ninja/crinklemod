package ninja.crinkle.mod.client.gui.managers;

import com.mojang.logging.LogUtils;
import ninja.crinkle.mod.client.gui.events.DragStartedEvent;
import ninja.crinkle.mod.client.gui.events.DragStoppedEvent;
import ninja.crinkle.mod.client.gui.events.DroppedEvent;
import ninja.crinkle.mod.client.gui.events.Event;
import ninja.crinkle.mod.client.gui.events.listeners.EventListener;
import ninja.crinkle.mod.client.gui.events.listeners.MouseListener;
import ninja.crinkle.mod.client.gui.events.sources.MouseSource;
import ninja.crinkle.mod.client.gui.widgets.AbstractWidget;
import org.slf4j.Logger;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class DragManager implements MouseListener, MouseSource {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final int Z_STEP = 10;
    public static final int Z_MIN = 0;
    public static final int Z_MAX = 5000;
    private final EventManager eventManager;
    private AbstractWidget current;
    private boolean dragging;

    public DragManager(EventManager eventManager) {
        this.eventManager = eventManager;
    }

    public AbstractWidget current() {
        return current;
    }

    public void current(AbstractWidget current) {
        this.current = current;
    }

    public boolean dragging() {
        return dragging;
    }

    public void dragging(boolean dragging) {
        this.dragging = dragging;
    }

    @Override
    public String name() {
        return "DragManager";
    }

    @Override
    public void onDragStarted(DragStartedEvent event) {
        if (event.cancelled()) return;
        drag(event.widget());
        event.consumer(this);
    }

    public void drag(AbstractWidget widget) {
        current(widget);
    }

    @Override
    public void onDragStopped(DragStoppedEvent event) {
        if (event.cancelled()) return;
        if (current() == null) return;
        drop(event, eventManager().map(m -> m.listeners(onlyOverlapping(current()))).orElse(List.of()));
        event.consumer(this);
    }

    public void drop(DragStoppedEvent event, List<EventListener> listeners) {
        if (current() == null) {
            return;
        }
        Event dropEvent = new DroppedEvent(event.scope(), this, event.x(), event.y(), event.button().button(),
                current(), listeners.stream().filter(onlyOverlapping(current())).toList());
        current(null);
        eventManager().ifPresent(m -> m.dispatchEvent(dropEvent));
    }

    @Override
    public Optional<EventManager> eventManager() {
        return Optional.ofNullable(eventManager);
    }

    private Predicate<EventListener> onlyOverlapping(EventListener other) {
        return (l) -> l instanceof AbstractWidget widget && other instanceof AbstractWidget otherWidget &&
                widget.cachedBoxes().borderBox().overlaps(otherWidget.cachedBoxes().borderBox());
    }
}
