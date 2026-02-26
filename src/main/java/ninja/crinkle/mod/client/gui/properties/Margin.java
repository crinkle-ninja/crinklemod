package ninja.crinkle.mod.client.gui.properties;

public record Margin(int top, int right, int bottom, int left) implements BoxProperty {
    public static final Margin ZERO = new Margin(0, 0, 0, 0);

    public Margin {
        if (top < 0 || left < 0 || right < 0 || bottom < 0) {
            throw new IllegalArgumentException("Margin values cannot be negative");
        }
    }

    public Margin(int all) {
        this(all, all, all, all);
    }

    public static Margin all(int all) {
        return new Margin(all);
    }
}
