package ninja.crinkle.mod.client.gui.actors;

import ninja.crinkle.mod.client.gui.properties.Point;
import ninja.crinkle.mod.client.gui.widgets.Widget;

public interface DragActor extends Actor {
    default boolean onDrag(Widget widget, Point mousePoint, Point dragPoint, int button) {
        return false;
    }

    default void onDragStopped(Widget widget, Point point) {}
    @SuppressWarnings("unused") default boolean onDragStarted(Widget widget, Point mousePoint, Point dragPoint, int button) {
        return false;
    }
}
