package ninja.crinkle.mod.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import ninja.crinkle.mod.CrinkleMod;
import ninja.crinkle.mod.client.color.Color;
import ninja.crinkle.mod.client.gui.themes.ThemeRegistry;
import ninja.crinkle.mod.config.sections.client.LayoutSection;
import ninja.crinkle.mod.config.sections.client.MetabolismOverlay;
import ninja.crinkle.mod.config.sections.client.OverlaySection;
import org.apache.commons.lang3.tuple.Pair;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber(modid = CrinkleMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientConfig {
    public static final ClientConfig INSTANCE;
    private static final ForgeConfigSpec CONFIG;

    static {
        Pair<ClientConfig, ForgeConfigSpec> pair = new ForgeConfigSpec.Builder().configure(ClientConfig::new);
        INSTANCE = pair.getLeft();
        CONFIG = pair.getRight();
    }

    private static final List<String> DEFAULT_WET_COLORS = List.of(
            "#fefdda", "#fefdcb", "#fdfcc2", "#f9f7b8", "#f6f5af");
    private static final List<String> DEFAULT_MESS_COLORS = List.of(
            "#f4edcb", "#e2d9b1", "#d0c8a2", "#c9c19a", "#c2b98e");

    private final ForgeConfigSpec.ConfigValue<Boolean> debug;
    private final LayoutSection layout;
    private final OverlaySection overlay;
    private final ForgeConfigSpec.ConfigValue<String> themeId;
    private final ForgeConfigSpec.ConfigValue<List<? extends String>> wetColors;
    private final ForgeConfigSpec.ConfigValue<List<? extends String>> messColors;

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
        builder.comment("Undergarment overlay color settings.")
                .push("colors");
        wetColors = builder
                .comment("Gradient colors for wet overlay, sampled evenly by fullness (0% is always untouched).\n" +
                        "Samples left-to-right in 20% increments.")
                .defineListAllowEmpty(List.of("wetColors"), () -> DEFAULT_WET_COLORS,
                        o -> o instanceof String s && Color.of(s).isValid());
        messColors = builder
                .comment("Gradient colors for mess overlay, sampled evenly by fullness (0% is always untouched).\n" +
                        "Samples left-to-right in 20% increments.")
                .defineListAllowEmpty(List.of("messColors"), () -> DEFAULT_MESS_COLORS,
                        o -> o instanceof String s && s.startsWith("#"));
        builder.pop();
        builder.comment("Layout positions for widgets.")
                .push("layout");
        layout = new LayoutSection(builder
                .comment("JSON map of widget positions: { \"id\": { \"anchor\": \"TOP_LEFT\", \"x\": 0, \"y\": 0 }, ." +
                        ".. }")
                .define("positions", "{}"));
        builder.pop();
        builder.push("overlay");
        builder.comment("Settings for the metabolism overlay.")
                .push("metabolism");
        ForgeConfigSpec.ConfigValue<Boolean> visible = builder
                .comment("Whether the overlay is visible.")
                .translation("config.crinklemod.overlay.visible")
                .define("visible", true);
        ForgeConfigSpec.ConfigValue<Boolean> animated = builder
                .comment("Whether the overlay is animated.")
                .define("animated", true);
        builder.pop();
        overlay = new OverlaySection(new MetabolismOverlay(visible, animated));
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

    public static LayoutSection layout() {
        return INSTANCE.layout;
    }

    public static OverlaySection overlay() {
        return INSTANCE.overlay;
    }

    public static void register() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, CONFIG);
    }

    public static String themeId() {
        return INSTANCE.themeId.get();
    }

    public static Map<Integer, Color> wetFillColors() {
        return gradientToColorMap(INSTANCE.wetColors.get());
    }

    public static Map<Integer, Color> messFillColors() {
        return gradientToColorMap(INSTANCE.messColors.get());
    }

    private static Map<Integer, Color> gradientToColorMap(List<? extends String> gradient) {
        Map<Integer, Color> map = new LinkedHashMap<>();
        if (gradient.isEmpty()) return map;
        int step = 100 / gradient.size();
        for (int i = 0; i < gradient.size(); i++) {
            map.put(step * (i + 1), Color.of(gradient.get(i)));
        }
        return map;
    }


}
