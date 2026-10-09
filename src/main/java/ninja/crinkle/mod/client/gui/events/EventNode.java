package ninja.crinkle.mod.client.gui.events;

import ninja.crinkle.mod.client.gui.managers.EventManager;

public interface EventNode {
    EventManager eventManager();

    String name();

    default int priority() {
        return 0;
    }
}