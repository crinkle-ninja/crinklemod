package ninja.crinkle.mod.client.gui.actions;

public interface FocusAction {
    default void onBlur() {
    }
    default void onFocus() {
    }
}
