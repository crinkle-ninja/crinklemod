package ninja.crinkle.mod.client.gui.actors;

import ninja.crinkle.mod.client.gui.widgets.TabbedPanelContainer;

public interface TabActor extends Actor {
    default void onTabChanged(TabbedPanelContainer.Entry tab) {}
}
