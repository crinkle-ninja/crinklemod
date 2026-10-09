package ninja.crinkle.mod.client.gui.events;

public class TextChangedEvent extends AbstractEvent {
    public static final Key<TextChangedEvent> KEY = new Key<>();
    private final String previousValue;


    public TextChangedEvent(EventNode source, String previousValue) {
        super(KEY, source);
        this.previousValue = previousValue;
    }

    public String previousValue() {
        return previousValue;
    }
}
