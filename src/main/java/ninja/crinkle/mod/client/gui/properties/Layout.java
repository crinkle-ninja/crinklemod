package ninja.crinkle.mod.client.gui.properties;

public record Layout(boolean clipContents, Size minimumSize, LayoutMode layoutMode, Transform transform) {
    public static Layout CONTAINER = new Layout(false, Size.ZERO, LayoutMode.Container, Transform.ZERO);
    public static Layout UNCONTROLLED = new Layout(false, Size.ZERO, LayoutMode.Uncontrolled, Transform.ZERO);
}
