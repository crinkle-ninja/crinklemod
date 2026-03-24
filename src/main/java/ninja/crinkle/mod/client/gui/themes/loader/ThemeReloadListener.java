package ninja.crinkle.mod.client.gui.themes.loader;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.common.MinecraftForge;
import ninja.crinkle.mod.client.gui.addons.PauseScreenAddOn;
import ninja.crinkle.mod.client.gui.themes.Theme;
import ninja.crinkle.mod.client.gui.themes.ThemeRegistry;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.Map;

public class ThemeReloadListener extends SimplePreparableReloadListener<Map<ResourceLocation, ThemeLoader.Config>> {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static boolean addOnsRegistered = false;

    @Override
    protected @NotNull Map<ResourceLocation, ThemeLoader.Config> prepare(@NotNull ResourceManager pResourceManager,
                                                                         @NotNull ProfilerFiller pProfiler) {
        LOGGER.info("Preparing theme data...");
        pProfiler.push("prepare_themes");
        var themes = ThemeLoader.loadConfigs(pResourceManager);
        pProfiler.pop();
        return themes;
    }

    @Override
    protected void apply(@NotNull Map<ResourceLocation, ThemeLoader.Config> pThemeConfig,
                         @NotNull ResourceManager pResourceManager, @NotNull ProfilerFiller pProfiler) {
        LOGGER.info("Validating theme data and registering themes...");
        pProfiler.push("apply_themes");
        pThemeConfig.forEach((location, config) -> {
            var errors = config.validate();
            if (errors.isEmpty()) {
                Theme theme = Theme.fromConfig(config.theme());
                ThemeRegistry.INSTANCE.register(theme);
            } else {
                errors.forEach(error -> LOGGER.error("{}: {}", location, error.message()));
            }
        });
        if (!addOnsRegistered) {
            MinecraftForge.EVENT_BUS.register(new PauseScreenAddOn());
            addOnsRegistered = true;
        }
        pProfiler.pop();
    }
}
