package ninja.crinkle.mod.client.gui.managers;

import ninja.crinkle.mod.client.gui.events.EventNode;
import ninja.crinkle.mod.client.gui.widgets.AbstractWidget;

public class FocusManager implements EventNode {
    private AbstractWidget currentFocus;

    public void currentFocus(AbstractWidget currentFocus) {
        if (this.currentFocus != null) {
            this.currentFocus.focused(false);
        }
        this.currentFocus = currentFocus;
        if (currentFocus != null) {
            currentFocus.focused(true);
        }
    }

    public AbstractWidget currentFocus() {
        return currentFocus;
    }

    @Override
    public EventManager eventManager() {
        return null;
    }

    @Override
    public String name() {
        return FocusManager.class.getSimpleName();
    }
}