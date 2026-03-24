package ninja.crinkle.mod.client.gui.events;


public class HoverEvent extends MouseEvent {
    public static final Key<HoverEvent> KEY = new Key<>();
    private final boolean hovered;

    public HoverEvent(EventNode source, double x, double y, boolean hovered) {
        super(KEY, source, x, y, -1);
        this.hovered = hovered;
    }

    public boolean hovered() {
        return hovered;
    }

    @Override
    public String toString() {
        return "HoverEvent{" +
                "x=" + x() +
                ", y=" + y() +
                ", hovered=" + hovered +
                ", " + super.toString() +
                '}';
    }
}