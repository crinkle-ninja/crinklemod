package ninja.crinkle.mod.client.gui.actors;

import ninja.crinkle.mod.client.gui.properties.Point;

public interface MouseActor extends Actor {
    default boolean onDoubleClick(Point point, MouseButton mouseButton) {
        return false;
    }
    default boolean onClick(Point point, MouseButton mouseButton) {
        return false;
    }

    default void onMoved(Point point) {}

    default boolean onPressed(Point point, MouseButton mouseButton) {
        return false;
    }

    default boolean onReleased(Point point, MouseButton mouseButton) {
        return false;
    }

    default boolean onScroll(Point mousePoint, double pDelta) {
        return false;
    }
}
