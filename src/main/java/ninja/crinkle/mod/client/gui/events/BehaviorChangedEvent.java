package ninja.crinkle.mod.client.gui.events;

import ninja.crinkle.mod.client.gui.states.WidgetBehavior;

public class BehaviorChangedEvent extends AbstractEvent {
    public static final Key<BehaviorChangedEvent> KEY = new Key<>();
    private final WidgetBehavior current;
    private final WidgetBehavior previous;

    public BehaviorChangedEvent(EventNode source, WidgetBehavior current, WidgetBehavior previous) {
        super(KEY, source);
        this.previous = previous;
        this.current = current;
    }

    public WidgetBehavior current() {
        return current;
    }

    public WidgetBehavior previous() {
        return previous;
    }
}