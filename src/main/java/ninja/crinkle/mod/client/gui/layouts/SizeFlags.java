package ninja.crinkle.mod.client.gui.layouts;

import java.util.EnumSet;

public enum SizeFlags {
    FILL(1),
    EXPAND(2),
    SHRINK_BEGIN(0),
    SHRINK_CENTER(4),
    SHRINK_END(8);

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

    public static final EnumSet<SizeFlags> DEFAULT = EnumSet.of(FILL);
}
