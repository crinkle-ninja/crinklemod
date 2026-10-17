package ninja.crinkle.mod.client.gui.actions;

public interface DragAction {
    default void onDragStopped() {}
    default void onDragStart() {}
    default void onDragMoved() {}
}
