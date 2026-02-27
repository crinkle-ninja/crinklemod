package ninja.crinkle.mod.client.gui.properties;

import java.util.EnumSet;

public record ContainerSizing(EnumSet<SizingOption> horizontal, EnumSet<SizingOption> vertical, float stretchRatio) {
    public static ContainerSizing DEFAULT = new ContainerSizing(EnumSet.of(SizingOption.Fill), EnumSet.of(SizingOption.Fill), 1.0f);
}
