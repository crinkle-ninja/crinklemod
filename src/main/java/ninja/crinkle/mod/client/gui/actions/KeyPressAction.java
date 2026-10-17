package ninja.crinkle.mod.client.gui.actions;

public interface KeyPressAction {
    default boolean onKeyPressed(int keyCode, int scanCode, int modifiers) { return false; }
}
