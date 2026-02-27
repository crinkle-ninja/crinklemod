package ninja.crinkle.mod.client.gui.properties;

public enum SizingOption {
    ShrinkBegin(0),
    Fill(1),
    Expand(2),
    ExpandFill(3),
    ShrinkCenter(4),
    ShrinkEnd(8)
    ;

    private final int flag;

    SizingOption(int flag_id) {
        flag = 1 << flag_id;
    }

    public int getFlag() {
        return flag;
    }
}
