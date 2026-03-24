package ninja.crinkle.mod.client.gui.events;

import ninja.crinkle.mod.client.gui.widgets.AbstractWidget;

public class FocusEvent extends AbstractEvent {
    public static final Key<FocusEvent> KEY = new Key<>();
    private final AbstractWidget focusTarget;
    private final boolean focused;

    public FocusEvent(EventNode source, boolean focused, AbstractWidget focusTarget) {
        super(KEY, source);
        this.focused = focused;
        this.focusTarget = focusTarget;
    }

    public FocusEvent(Key<?> key, EventNode source, boolean focused, AbstractWidget focusTarget) {
        super(key, source);
        this.focused = focused;
        this.focusTarget = focusTarget;
    }

    public AbstractWidget focusTarget() {
        return focusTarget;
    }

    public boolean focused() {
        return focused;
    }

    @Override
    public String toString() {
        return "FocusEvent{" +
                "focused=" + focused +
                ", focusTarget=" + focusTarget +
                ", " + super.toString() +
                '}';
    }
}