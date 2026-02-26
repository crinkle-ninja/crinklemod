package ninja.crinkle.mod.client.gui.events.sources;

import ninja.crinkle.mod.client.gui.layouts.Layout;
import ninja.crinkle.mod.client.gui.states.WidgetLayout;

public interface LayoutSource extends EventSource {
    void layoutChanged(WidgetLayout old);
}
