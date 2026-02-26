package ninja.crinkle.mod.client.gui.events;

import ninja.crinkle.mod.client.gui.events.listeners.EventListener;
import ninja.crinkle.mod.client.gui.events.sources.EventSource;
import ninja.crinkle.mod.client.gui.properties.Scope;

public interface Event {
    Type type();
    boolean dispatched();
    void dispatched(boolean dispatched);
    boolean cancelled();
    void cancelled(boolean cancelled);
    boolean consumed();
    void consumed(boolean consumed);
    boolean propagate();
    EventListener consumer();
    void consumer(EventListener consumer);
    EventSource source();
    Scope scope();

    default boolean success() {
        return !cancelled() && consumed();
    }


    enum Type {
        CharTyped,
        Click,
        Drag,
        Focus,
        Hover,
        Key,
        Move,
        Narrate,
        TabOrder,
        FocusEntered,
        FocusLeft,
        MousePressed,
        MouseReleased,
        DragStarted,
        DragStopped,
        Dropped,
        DoubleClick,
        Scroll,
        LayoutChanged,
    }

}
