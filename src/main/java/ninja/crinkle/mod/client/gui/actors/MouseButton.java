package ninja.crinkle.mod.client.gui.actors;

public enum MouseButton {
    NONE(-1),
    LEFT(0),
    RIGHT(1),
    MIDDLE(2);
    private final int button;

    MouseButton(int button) {
        this.button = button;
    }

    public static MouseButton fromInt(int button) {
        for (MouseButton value : values()) {
            if (value.button == button) {
                return value;
            }
        }
        return NONE;
    }

    public int button() {
        return button;
    }
}
