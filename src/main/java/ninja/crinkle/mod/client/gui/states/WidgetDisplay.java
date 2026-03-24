package ninja.crinkle.mod.client.gui.states;

import ninja.crinkle.mod.client.gui.managers.DragManager;
import org.jetbrains.annotations.NotNull;

public record WidgetDisplay(boolean visible, float alpha, int zIndex) {
    public WidgetDisplay() {
        this(true, 1, DragManager.Z_MIN);
    }

    public WidgetDisplay {
        if (alpha < 0 || alpha > 1) {
            throw new IllegalArgumentException("Alpha must be between 0 and 1");
        }
        if (zIndex < 0) {
            throw new IllegalArgumentException("Z-Index must be greater than or equal to 0");
        }
    }

    @Override
    public @NotNull String toString() {
        return "WidgetDisplay{" +
                "visible=" + visible +
                ", alpha=" + alpha +
                ", zIndex=" + zIndex +
                '}';
    }

    public WidgetDisplay withAlpha(float alpha) {
        return new WidgetDisplay(visible, alpha, zIndex);
    }

    public WidgetDisplay withVisible(boolean visible) {
        return new WidgetDisplay(visible, alpha, zIndex);
    }

    public WidgetDisplay withZIndex(int zIndex) {
        assert zIndex >= DragManager.Z_MIN && zIndex <= DragManager.Z_MAX;
        return new WidgetDisplay(visible, alpha, zIndex);
    }
}
