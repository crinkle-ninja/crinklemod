package ninja.crinkle.mod.client.gui.properties;

public record AnchorOffset(int left, int top, int right, int bottom) {
    public static final AnchorOffset ZERO = new AnchorOffset(0, 0, 0, 0);
}