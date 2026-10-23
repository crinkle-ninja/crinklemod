package ninja.crinkle.mod.client.gui.actors;

public interface FocusActor extends Actor {
    default void onBlur() {
    }
    default void onFocus() {
    }
}
