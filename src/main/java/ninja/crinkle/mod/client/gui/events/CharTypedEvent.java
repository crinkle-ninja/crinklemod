package ninja.crinkle.mod.client.gui.events;


public class CharTypedEvent extends KeyEvent {
    public static final Key<CharTypedEvent> KEY = new Key<>();
    private final char codePoint;

    public CharTypedEvent(EventNode source, char codePoint, int modifiers) {
        super(KEY, source, modifiers);
        this.codePoint = codePoint;
    }

    public char codePoint() {
        return codePoint;
    }

    @Override
    public String toString() {
        return "CharTypedEvent{" +
                "codePoint=" + codePoint +
                ", modifiers=" + modifiers() +
                '}';
    }
}