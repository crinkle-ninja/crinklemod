package ninja.crinkle.mod.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import ninja.crinkle.mod.CrinkleMod;

@Mod.EventBusSubscriber(modid = CrinkleMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class UndergarmentConfig {
    private static final String CONFIG_FILE_NAME = String.format("%s-undergarments.toml", CrinkleMod.MODID);
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    private static final ForgeConfigSpec.IntValue DEFAULT_MAX_LIQUIDS = BUILDER
            .comment("Default maximum liquids capacity for undergarments (used when a design does not specify stats).")
            .defineInRange("defaultMaxLiquids", 5, 1, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue DEFAULT_MAX_SOLIDS = BUILDER
            .comment("Default maximum solids capacity for undergarments (used when a design does not specify stats).")
            .defineInRange("defaultMaxSolids", 3, 1, Integer.MAX_VALUE);
    private static final ForgeConfigSpec SPEC = BUILDER.build();

    private static int defaultMaxLiquids = 5;
    private static int defaultMaxSolids = 3;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        if (!event.getConfig().getFileName().equals(CONFIG_FILE_NAME)) return;
        defaultMaxLiquids = DEFAULT_MAX_LIQUIDS.get();
        defaultMaxSolids = DEFAULT_MAX_SOLIDS.get();
    }

    public static int getDefaultMaxLiquids() {
        return defaultMaxLiquids;
    }

    public static int getDefaultMaxSolids() {
        return defaultMaxSolids;
    }

    public static void register() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, SPEC, CONFIG_FILE_NAME);
    }
}