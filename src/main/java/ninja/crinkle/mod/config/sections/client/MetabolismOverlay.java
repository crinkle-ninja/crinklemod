package ninja.crinkle.mod.config.sections.client;

import net.minecraftforge.common.ForgeConfigSpec;

public record MetabolismOverlay(ForgeConfigSpec.ConfigValue<Boolean> animated,
                                ForgeConfigSpec.ConfigValue<Boolean> visible) {
}
