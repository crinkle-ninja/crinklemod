package ninja.crinkle.mod.client.gui.events;


public class LayoutChangedEvent extends AbstractEvent {
    public static final Key<LayoutChangedEvent> KEY = new Key<>();

    public LayoutChangedEvent(EventNode source) {
        super(KEY, source);
    }
}