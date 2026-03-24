package ninja.crinkle.mod.client.gui.events;

import ninja.crinkle.mod.client.gui.widgets.AbstractWidget;

public class TabIndexEvent extends FocusEvent {
    public static final Key<TabIndexEvent> KEY = new Key<>();
    private final int newIndex;
    private final int oldIndex;

    public TabIndexEvent(EventNode source, boolean focused, int oldIndex, int newIndex, AbstractWidget focusTarget) {
        super(KEY, source, focused, focusTarget);
        this.oldIndex = oldIndex;
        this.newIndex = newIndex;
    }

    public int newIndex() {
        return newIndex;
    }

    public int oldIndex() {
        return oldIndex;
    }

    @Override
    public String toString() {
        return "TabIndexEvent{" +
                "oldIndex=" + oldIndex +
                ", newIndex=" + newIndex +
                ", " + super.toString() +
                '}';
    }
}