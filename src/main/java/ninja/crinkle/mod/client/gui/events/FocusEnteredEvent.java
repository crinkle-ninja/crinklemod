package ninja.crinkle.mod.client.gui.events;

import ninja.crinkle.mod.client.gui.widgets.AbstractWidget;

public class FocusEnteredEvent extends FocusEvent {
    public static final Key<FocusEnteredEvent> KEY = new Key<>();

    public FocusEnteredEvent(EventNode source, boolean focused, AbstractWidget focusTarget) {
        super(KEY, source, focused, focusTarget);
    }

    @Override
    public String toString() {
        return "FocusEnteredEvent{" + super.toString() + '}';
    }
}