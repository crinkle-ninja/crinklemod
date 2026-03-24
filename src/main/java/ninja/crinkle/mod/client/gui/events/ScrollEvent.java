package ninja.crinkle.mod.client.gui.events;


public class ScrollEvent extends MouseEvent {
    public static final Key<ScrollEvent> KEY = new Key<>();
    private final double delta;

    public ScrollEvent(EventNode source, double x, double y, double delta) {
        super(KEY, source, x, y, -1);
        this.delta = delta;
    }

    public double delta() {
        return delta;
    }

    @Override
    public String toString() {
        return "ScrollEvent{" +
                "x=" + x() +
                ", y=" + y() +
                ", delta=" + delta +
                ", " + super.toString() +
                '}';
    }
}