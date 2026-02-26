package ninja.crinkle.mod.client.gui.events;

import ninja.crinkle.mod.client.gui.events.sources.EventSource;
import ninja.crinkle.mod.client.gui.layouts.Layout;
import ninja.crinkle.mod.client.gui.properties.Scope;
import ninja.crinkle.mod.client.gui.states.WidgetLayout;

public class LayoutChangedEvent extends AbstractEvent {
    private final WidgetLayout old;

    public LayoutChangedEvent(Type type, Scope scope, EventSource source, WidgetLayout old) {
        super(type, scope, source);
        this.old = old;
    }

    public WidgetLayout old() {
        return old;
    }
}
