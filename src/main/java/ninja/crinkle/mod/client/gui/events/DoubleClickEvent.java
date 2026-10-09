package ninja.crinkle.mod.client.gui.events;

import ninja.crinkle.mod.client.gui.screens.AbstractScreen;

import java.util.List;

public class DoubleClickEvent extends MouseEvent implements Event {
    public static final Key<DoubleClickEvent> KEY = new Key<>();
    private final List<EventNode> listeners;

    public DoubleClickEvent(AbstractScreen abstractScreen, double pMouseX, double pMouseY, int pButton,
                            List<EventNode> listeners) {
        super(KEY, abstractScreen, pMouseX, pMouseY, pButton);
        this.listeners = listeners;
    }

    public List<EventNode> listeners() {
        return listeners;
    }
}