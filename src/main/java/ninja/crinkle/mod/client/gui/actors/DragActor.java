package ninja.crinkle.mod.client.gui.actors;

import ninja.crinkle.mod.client.gui.properties.Point;
import ninja.crinkle.mod.client.gui.widgets.AbstractWidget;

public interface DragActor extends Actor {
    default boolean onDrag(AbstractWidget widget, Point mousePoint, Point dragPoint, int button) {
        return false;
    }

    default void onDragStopped(AbstractWidget widget, Point point) {}
    default boolean onDragStarted(AbstractWidget widget, Point mousePoint, Point dragPoint, int button) {
        return false;
    }
}
