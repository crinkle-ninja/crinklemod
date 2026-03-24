package ninja.crinkle.mod.client.gui.events;

import java.util.List;

public class DragEvent extends MouseEvent {
    public static final Key<DragEvent> KEY = new Key<>();
    private final double dragX;
    private final double dragY;
    private final List<EventNode> listeners;

    public DragEvent(EventNode source, double x, double y, int button, double dragX, double dragY,
                     List<EventNode> listeners) {
        super(KEY, source, x, y, button);
        this.dragX = dragX;
        this.dragY = dragY;
        this.listeners = listeners == null ? List.of() : List.copyOf(listeners);
    }

    public DragEvent(Key<?> key, EventNode source, double x, double y, int button, double dragX, double dragY,
                     List<EventNode> listeners) {
        super(key, source, x, y, button);
        this.dragX = dragX;
        this.dragY = dragY;
        this.listeners = listeners == null ? List.of() : List.copyOf(listeners);
    }

    public double dragX() {
        return dragX;
    }

    public double dragY() {
        return dragY;
    }

    public List<EventNode> listeners() {
        return listeners;
    }

    @Override
    public String toString() {
        return "DragEvent{" +
                "x=" + x() +
                ", y=" + y() +
                ", button=" + button() +
                ", dragX=" + dragX +
                ", dragY=" + dragY +
                ", " + super.toString() +
                '}';
    }
}