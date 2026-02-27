package ninja.crinkle.mod.client.gui.properties;

public record Anchor(float left, float top, float right, float bottom) {
    // Presets
    public static final Anchor TOP_LEFT     = new Anchor(0, 0, 0, 0);
    public static final Anchor CENTER       = new Anchor(.5f, .5f, .5f, .5f);
    public static final Anchor FULL_RECT    = new Anchor(0, 0, 1, 1);
    public static final Anchor TOP_WIDE     = new Anchor(0, 0, 1, 0);
    public static final Anchor BOTTOM_WIDE  = new Anchor(0, 1, 1, 1);
    public static final Anchor LEFT_WIDE    = new Anchor(0, 0, 0, 1);
    public static final Anchor RIGHT_WIDE   = new Anchor(1, 0, 1, 1);
}