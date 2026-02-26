package ninja.crinkle.mod.client.gui.states;

public enum Positioning {
    Relative, Absolute;

    public boolean isRelative() {
        return this == Relative;
    }

    public boolean isAbsolute() {
        return this == Absolute;
    }

}
