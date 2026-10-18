package ninja.crinkle.mod.client.gui.managers;

import ninja.crinkle.mod.client.gui.widgets.AbstractWidget;

public class FocusManager {
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
}