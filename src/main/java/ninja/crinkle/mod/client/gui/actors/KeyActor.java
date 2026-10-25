package ninja.crinkle.mod.client.gui.actors;

public interface KeyActor extends Actor {
    default boolean onCharTyped(char pCodePoint, int pModifiers) {
        return false;
    }

    default boolean onKeyPressed(int keyCode, int scanCode, int modifiers) { return false; }

    default boolean onKeyReleased(int pKeyCode, int pScanCode, int pModifiers) {
        return false;
    }
}
