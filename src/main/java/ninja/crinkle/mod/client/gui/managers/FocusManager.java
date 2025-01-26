package ninja.crinkle.mod.client.gui.managers;

import ninja.crinkle.mod.client.gui.events.FocusEnteredEvent;
import ninja.crinkle.mod.client.gui.events.FocusLeftEvent;
import ninja.crinkle.mod.client.gui.events.listeners.FocusListener;
import ninja.crinkle.mod.client.gui.events.sources.FocusSource;

public class FocusManager implements FocusListener {
    private FocusSource currentFocus;

    public void currentFocus(FocusSource currentFocus) {
        if (this.currentFocus != null) {
            this.currentFocus.focused(false);
        }
        this.currentFocus = currentFocus;
        if (currentFocus != null) {
            currentFocus.focused(true);
        }
    }

    public FocusSource currentFocus() {
        return currentFocus;
    }

    @Override
    public String name() {
        return FocusManager.class.getSimpleName();
    }
}
