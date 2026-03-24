package ninja.crinkle.mod.client.gui.events;

import ninja.crinkle.mod.client.gui.widgets.AbstractWidget;

public class FocusLeftEvent extends FocusEvent {
    public static final Key<FocusLeftEvent> KEY = new Key<>();

    public FocusLeftEvent(EventNode source, boolean focused, AbstractWidget focusTarget) {
        super(KEY, source, focused, focusTarget);
    }

    @Override
    public String toString() {
        return "FocusLeftEvent{" + super.toString() + '}';
    }
}