package ninja.crinkle.mod.client.gui.properties;

import java.util.Collections;
import java.util.EnumSet;

public enum Sizing {
    Fill,
    Expand,
    ShrinkBegin,
    ShrinkCenter,
    ShrinkEnd;

    public static EnumSet<Sizing> of(Sizing... flags) {
        EnumSet<Sizing> set = EnumSet.noneOf(Sizing.class);
        Collections.addAll(set, flags);
        return set;
    }
}
