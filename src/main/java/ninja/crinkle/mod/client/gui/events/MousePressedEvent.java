package ninja.crinkle.mod.client.gui.events;

import java.util.List;

public class MousePressedEvent extends MouseEvent {
    public static final Key<MousePressedEvent> KEY = new Key<>();
    private final List<EventNode> listeners;

    public MousePressedEvent(EventNode source, double x, double y, int button, List<EventNode> listeners) {
        super(KEY, source, x, y, button);
        this.listeners = listeners;
    }

    public List<EventNode> listeners() {
        return listeners;
    }

    @Override
    public String toString() {
        return "MousePressedEvent{" + super.toString() + '}';
    }
}