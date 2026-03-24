package ninja.crinkle.mod.client.gui.events;


public class KeyEvent extends InputEvent {
    public static final Key<KeyEvent> KEY = new Key<>();
    private final int keyCode;
    private final int modifiers;
    private final boolean released;
    private final int scanCode;

    public KeyEvent(Key<?> key, EventNode source, int modifiers) {
        this(key, source, java.awt.event.KeyEvent.VK_UNDEFINED, 0, modifiers, false);
    }

    public KeyEvent(Key<?> key, EventNode source, int keyCode, int scanCode, int modifiers, boolean released) {
        super(key, source);
        this.keyCode = keyCode;
        this.scanCode = scanCode;
        this.modifiers = modifiers;
        this.released = released;
    }

    public KeyEvent(EventNode source, int keyCode, int scanCode, int modifiers, boolean released) {
        super(KEY, source);
        this.keyCode = keyCode;
        this.scanCode = scanCode;
        this.modifiers = modifiers;
        this.released = released;
    }

    public boolean isAltDown() {
        return Modifier.Alt.mask() == (modifiers & Modifier.Alt.mask());
    }

    public boolean isControlDown() {
        return Modifier.Control.mask() == (modifiers & Modifier.Control.mask());
    }

    public boolean isMetaDown() {
        return Modifier.Meta.mask() == (modifiers & Modifier.Meta.mask());
    }

    public boolean isShiftDown() {
        return Modifier.Shift.mask() == (modifiers & Modifier.Shift.mask());
    }

    public int keyCode() {
        return keyCode;
    }

    public int modifiers() {
        return modifiers;
    }

    public boolean pressed() {
        return !released();
    }

    public boolean released() {
        return released;
    }

    public int scanCode() {
        return scanCode;
    }

    @Override
    public String toString() {
        return "KeyEvent{" +
                "keyCode=" + keyCode +
                ", scanCode=" + scanCode +
                ", modifiers=" + modifiers +
                ", released=" + released +
                ", " + super.toString() +
                '}';
    }

    public enum Modifier {
        Alt(8),
        Meta(4),
        Control(2),
        Shift(1);

        private final int mask;

        Modifier(int mask) {
            this.mask = mask;
        }

        public int mask() {
            return mask;
        }
    }
}