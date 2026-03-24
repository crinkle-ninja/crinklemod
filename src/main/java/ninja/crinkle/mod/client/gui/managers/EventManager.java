package ninja.crinkle.mod.client.gui.managers;

import com.mojang.logging.LogUtils;
import ninja.crinkle.mod.client.gui.events.Event;
import ninja.crinkle.mod.client.gui.events.EventNode;
import org.slf4j.Logger;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class EventManager {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final int PRIORITY_IGNORE = Integer.MAX_VALUE;
    public static final int PRIORITY_OVERRIDE = Integer.MIN_VALUE;
    public static final int PRIORITY_STEP = 10;
    private final Map<Event.Key<?>, List<PrioritizedListener<?>>> keyListeners = new HashMap<>();
    private final List<EventNode> listeners = new ArrayList<>();

    public void addListener(EventNode listener) {
        assert listener != null;
        if (listeners.contains(listener)) {
            LOGGER.warn("Attempted to add duplicate listener: {}", listener);
            return;
        }
        listeners.add(listener);
    }

    public <T extends Event> void addListener(Event.Key<T> key, int priority, Consumer<T> handler) {
        keyListeners.computeIfAbsent(key, k -> new ArrayList<>()).add(new PrioritizedListener<>(priority, handler));
        keyListeners.get(key).sort(Comparator.comparingInt(PrioritizedListener::priority));
    }

    public void clear() {
        listeners.clear();
        keyListeners.clear();
    }

    public void dispatchEvent(Event event) {
        dispatchEvent(event, listener -> true);
    }

    @SuppressWarnings("unchecked")
    public void dispatchEvent(Event event, Predicate<EventNode> predicate) {
        if (event.dispatched() || !event.propagate()) {
            return;
        }
        event.dispatched(true);
        var list = keyListeners.get(event.key());
        if (list != null) {
            for (var pl : list) {
                if (!event.propagate()) break;
                if (pl.priority() == PRIORITY_IGNORE) continue;
                ((Consumer<Event>) pl.handler()).accept(event);
            }
        }
    }

    public List<EventNode> listeners(Predicate<EventNode> predicate) {
        return listeners.stream().filter(predicate).toList();
    }

    public <T extends Event> void removeListener(Event.Key<T> key, Consumer<T> handler) {
        var list = keyListeners.get(key);
        if (list != null) {
            list.removeIf(pl -> pl.handler() == handler);
            if (list.isEmpty()) {
                keyListeners.remove(key);
            }
        }
    }

    public void removeListener(EventNode listener) {
        listeners.remove(listener);
    }

    public record PrioritizedListener<T extends Event>(int priority, Consumer<T> handler) {
    }
}