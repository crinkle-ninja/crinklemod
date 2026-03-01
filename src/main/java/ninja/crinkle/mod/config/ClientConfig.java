package ninja.crinkle.mod.config;

import com.mojang.logging.LogUtils;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import ninja.crinkle.mod.CrinkleMod;
import ninja.crinkle.mod.client.gui.themes.ThemeRegistry;
import ninja.crinkle.mod.config.sections.client.LayoutSection;
import ninja.crinkle.mod.config.sections.client.OverlaySection;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

@Mod.EventBusSubscriber(modid = CrinkleMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientConfig {
    public static final ClientConfig INSTANCE;
    private static final ForgeConfigSpec CONFIG;

    static {
        Pair<ClientConfig, ForgeConfigSpec> pair = new ForgeConfigSpec.Builder().configure(ClientConfig::new);
        INSTANCE = pair.getLeft();
        CONFIG = pair.getRight();
    }

    private final ForgeConfigSpec.ConfigValue<Boolean> debug;
    private final LayoutSection layout;
    private final OverlaySection overlay;
    private final ForgeConfigSpec.ConfigValue<String> themeId;

    ClientConfig(ForgeConfigSpec.Builder builder) {
        builder.comment("Client-side configuration settings.")
                .push("client");
        themeId = builder
                .comment("The ID of the theme to use for the overlay.")
                .translation("config.crinklemod.themeId")
                .define("themeId", ThemeRegistry.DEFAULT_THEME_ID);
        debug = builder
                .comment("Enable debug mode.")
                .translation("config.crinklemod.debug")
                .define("debug", false);
        builder.pop();
        layout = new LayoutSection(builder);
        overlay = new OverlaySection(builder);
    }

    public static boolean debug() {
        return INSTANCE.debug.get();
    }

    public static Object get(List<String> path) {
        return CONFIG.get(path);
    }

    public static ForgeConfigSpec getSpec() {
        return CONFIG;
    }

    public static String themeId() {
        return INSTANCE.themeId.get();
    }

    public static String getTranslation(List<String> path) {
        return CONFIG.getLevelTranslationKey(path);
    }

    public static void register() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, CONFIG);
    }

    public static LayoutSection layout() {
        return INSTANCE.layout;
    }

    public static OverlaySection overlay() {
        return INSTANCE.overlay;
    }
}
