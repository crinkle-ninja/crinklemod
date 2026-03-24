package ninja.crinkle.mod.client.gui.events;

import java.util.List;

public class ClickEvent extends MouseEvent {
    public static final Key<ClickEvent> KEY = new Key<>();
    private final List<EventNode> listeners;
    private final boolean released;

    public ClickEvent(EventNode source, double x, double y, int button, boolean released, List<EventNode> listeners) {
        super(KEY, source, x, y, button);
        this.released = released;
        this.listeners = listeners;
    }

    public List<EventNode> listeners() {
        return listeners;
    }

    public boolean pressed() {
        return !released;
    }

    public boolean released() {
        return released;
    }

    @Override
    public String toString() {
        return "ClickEvent{" +
                "x=" + x() +
                ", y=" + y() +
                ", button=" + button() +
                ", released=" + released +
                ", " + super.toString() +
                '}';
    }
}