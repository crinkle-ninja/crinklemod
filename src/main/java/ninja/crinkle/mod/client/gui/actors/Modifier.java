package ninja.crinkle.mod.client.gui.actors;

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
