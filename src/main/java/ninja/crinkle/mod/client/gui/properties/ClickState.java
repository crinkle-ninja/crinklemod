package ninja.crinkle.mod.client.gui.properties;

import ninja.crinkle.mod.client.gui.widgets.Widget;

public record ClickState(Point position, int button, long clickTime, Widget widget) {
    public ClickState {
        if (position == null) {
            throw new IllegalArgumentException("Position cannot be null");
        }

    }
}
