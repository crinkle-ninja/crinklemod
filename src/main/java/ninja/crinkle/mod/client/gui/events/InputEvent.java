package ninja.crinkle.mod.client.gui.events;


public abstract class InputEvent extends AbstractEvent {
    public InputEvent(Key<?> key, EventNode source) {
        super(key, source);
    }
}