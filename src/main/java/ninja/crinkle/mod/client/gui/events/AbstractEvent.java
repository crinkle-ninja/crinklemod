package ninja.crinkle.mod.client.gui.events;

import ninja.crinkle.mod.client.gui.events.listeners.EventListener;
import ninja.crinkle.mod.client.gui.events.sources.EventSource;
import ninja.crinkle.mod.client.gui.properties.Scope;

public abstract class AbstractEvent implements Event {
    private final Type type;
    private boolean cancelled;
    private boolean consumed;
    private boolean dispatched;
    private EventListener consumer;
    private final EventSource source;
    private final Scope scope;

    public AbstractEvent(Type type, Scope scope, EventSource source) {
        this.type = type;
        this.scope = scope;
        this.source = source;
    }

    public boolean cancelled() {
        return cancelled;
    }

    public void cancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public boolean consumed() {
        return consumed;
    }

    public void consumed(boolean consumed) {
        this.consumed = consumed;
    }

    @Override
    public EventListener consumer() {
        return consumer;
    }

    @Override
    public void consumer(EventListener consumer) {
        consumed(consumer != null);
        this.consumer = consumer;
    }

    @Override
    public boolean propagate() {
        return !cancelled;
    }

    @Override
    public Type type() {
        return type;
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
    public Scope scope() {
        return scope;
    }

    @Override
    public EventSource source() {
        return source;
    }

    @Override
    public String toString() {
        return "AbstractEvent{" +
                "type=" + type() +
                ", scope=" + scope() +
                ", cancelled=" + cancelled() +
                ", consumed=" + consumed() +
                ", dispatched=" + dispatched() +
                ", propagate=" + propagate() +
                ", consumer=" + consumer() +
                ", source=" + source().name() +
                "]}";
    }
}
