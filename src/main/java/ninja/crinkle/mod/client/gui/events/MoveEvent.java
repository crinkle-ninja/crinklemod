package ninja.crinkle.mod.client.gui.events;

import java.util.List;

public class MoveEvent extends MouseEvent {
    public static final Key<MoveEvent> KEY = new Key<>();
    private final List<EventNode> listeners;

    public MoveEvent(EventNode source, double x, double y, List<EventNode> listeners) {
        super(KEY, source, x, y, -1);
        this.listeners = listeners;
    }

    public List<EventNode> listeners() {
        return listeners;
    }

    @Override
    public String toString() {
        return "MoveEvent{" +
                "x=" + x() +
                ", y=" + y() +
                ", " + super.toString() +
                '}';
    }
}