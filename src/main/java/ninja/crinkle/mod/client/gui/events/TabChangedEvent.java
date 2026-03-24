package ninja.crinkle.mod.client.gui.events;

import ninja.crinkle.mod.client.gui.widgets.TabbedPanelContainer;

public class TabChangedEvent extends AbstractEvent {
    public static final Key<TabChangedEvent> KEY = new Key<>();
    private final TabbedPanelContainer.Entry currentTab;

    public TabChangedEvent(EventNode source, TabbedPanelContainer.Entry currentTab) {
        super(KEY, source);
        this.currentTab = currentTab;
    }

    public TabbedPanelContainer.Entry currentTab() {
        return currentTab;
    }
}