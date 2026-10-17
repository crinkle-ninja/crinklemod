package ninja.crinkle.mod.client.gui.actions;

import ninja.crinkle.mod.client.gui.properties.Point;

public interface MouseAction {
    default boolean onDoubleClick(Point point, int button) {
        return false;
    }
    default boolean onClick(Point point, int button) {
        return false;
    }

    default boolean onPressed(Point point, int button) {
        return false;
    }

    default boolean onReleased(Point point, int button) {
        return false;
    }
}
