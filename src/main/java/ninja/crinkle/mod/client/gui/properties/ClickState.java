package ninja.crinkle.mod.client.gui.properties;

import ninja.crinkle.mod.client.gui.widgets.AbstractWidget;

public record ClickState(Point position, int button, long clickTime, AbstractWidget widget) {
    public ClickState {
        if (position == null) {
            throw new IllegalArgumentException("Position cannot be null");
        }

    }
}
