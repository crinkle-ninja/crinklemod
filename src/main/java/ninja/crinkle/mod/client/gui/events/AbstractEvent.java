package ninja.crinkle.mod.client.gui.events;


public abstract class AbstractEvent implements Event {
    private final Key<?> key;
    private final EventNode source;
    private boolean cancelled;
    private boolean consumed;
    private EventNode consumer;
    private boolean dispatched;

    public AbstractEvent(Key<?> key, EventNode source) {
        this.key = key;
        this.source = source;
    }

    public void cancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public void consumed(boolean consumed) {
        this.consumed = consumed;
    }

    @Override
    public EventNode consumer() {
        return consumer;
    }

    @Override
    public void consumer(EventNode consumer) {
        consumed(consumer != null);
        this.consumer = consumer;
    }

    @Override
    public boolean dispatched() {
        return dispatched;
    }

    @Override
    public void dispatched(boolean dispatched) {
        this.dispatched = dispatched;
    }

    @Override
    public Key<?> key() {
        return key;
    }

    @Override
    public boolean propagate() {
        return !cancelled;
    }

    @Override
    public EventNode source() {
        return source;
    }

    public boolean cancelled() {
        return cancelled;
    }

    public boolean consumed() {
        return consumed;
    }

    @Override
    public String toString() {
        return "AbstractEvent{" +
                "key=" + key() +
                ", cancelled=" + cancelled() +
                ", consumed=" + consumed() +
                ", dispatched=" + dispatched() +
                ", propagate=" + propagate() +
                ", consumer=" + consumer() +
                ", source=" + source().name() +
                "]}";
    }
}