package ninja.crinkle.mod.config.sections.client;

import net.minecraftforge.common.ForgeConfigSpec;

public class MetabolismOverlay {
    public final ForgeConfigSpec.ConfigValue<Boolean> visible;
    public final ForgeConfigSpec.ConfigValue<Boolean> animated;

    public MetabolismOverlay(ForgeConfigSpec.Builder builder) {
        builder.comment("Settings for the metabolism overlay.")
                .push("metabolism");
        visible = builder
                .comment("Whether the overlay is visible.")
                .translation("config.crinklemod.overlay.visible")
                .define("visible", true);
        animated = builder
                .comment("Whether the overlay is animated.")
                .define("animated", true);
        builder.pop();
    }
}
