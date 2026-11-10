package ninja.crinkle.mod.client.gui.managers;

import ninja.crinkle.mod.client.gui.widgets.Widget;

public class FocusManager {
    private Widget currentFocus;

    public void currentFocus(Widget currentFocus) {
        if (this.currentFocus != null) {
            this.currentFocus.focused(false);
        }
        this.currentFocus = currentFocus;
        if (currentFocus != null) {
            currentFocus.focused(true);
        }
    }

    public Widget currentFocus() {
        return currentFocus;
    }
}