package ninja.crinkle.mod.client.gui.events;

public interface Event {
    void cancelled(boolean cancelled);

    void consumed(boolean consumed);

    EventNode consumer();

    void consumer(EventNode consumer);

    boolean dispatched();

    void dispatched(boolean dispatched);

    Key<?> key();

    boolean propagate();

    EventNode source();

    default boolean success() {
        return !cancelled() && consumed();
    }

    boolean cancelled();

    boolean consumed();

    class Key<T extends Event> {
    }
}
