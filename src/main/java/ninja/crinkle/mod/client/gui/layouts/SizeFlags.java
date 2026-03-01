package ninja.crinkle.mod.client.gui.layouts;

import java.util.EnumSet;

public enum SizeFlags {
    Fill(1),
    Expand(2),
    ShrinkBegin(0),
    ShrinkCenter(4),
    ShrinkEnd(8);

    private final int bit;

    SizeFlags(int bit) {
        this.bit = bit;
    }

    public int bit() {
        return bit;
    }

    public static EnumSet<SizeFlags> of(SizeFlags... flags) {
        EnumSet<SizeFlags> set = EnumSet.noneOf(SizeFlags.class);
        for (SizeFlags flag : flags) {
            set.add(flag);
        }
        return set;
    }

    public static final EnumSet<SizeFlags> DEFAULT = EnumSet.of(Fill);
}
