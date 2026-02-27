package ninja.crinkle.mod.client.gui.properties;

public record Transform(Position position, Size size) {
    public static Transform ZERO = new Transform(Position.ABSOLUTE_ZERO, Size.ZERO);
}
