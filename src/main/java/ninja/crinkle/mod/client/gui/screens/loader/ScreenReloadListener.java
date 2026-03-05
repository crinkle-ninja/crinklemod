package ninja.crinkle.mod.client.gui.screens.loader;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import ninja.crinkle.mod.client.gui.screens.ScreenRegistry;
import ninja.crinkle.mod.client.gui.screens.definition.ScreenDefinition;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.Map;

public class ScreenReloadListener extends SimplePreparableReloadListener<Map<ResourceLocation, ScreenLoader.Config>> {
    private static final Logger LOGGER = LogUtils.getLogger();

    @Override
    protected @NotNull Map<ResourceLocation, ScreenLoader.Config> prepare(@NotNull ResourceManager pResourceManager,
                                                                          @NotNull ProfilerFiller pProfiler) {
        LOGGER.info("Preparing screen data...");
        pProfiler.push("prepare_screens");
        var screens = ScreenLoader.loadConfigs(pResourceManager);
        pProfiler.pop();
        return screens;
    }

    @Override
    protected void apply(@NotNull Map<ResourceLocation, ScreenLoader.Config> pScreenConfigs,
                         @NotNull ResourceManager pResourceManager, @NotNull ProfilerFiller pProfiler) {
        LOGGER.info("Validating screen data and registering screens...");
        pProfiler.push("apply_screens");
        pScreenConfigs.forEach((location, config) -> {
            var errors = config.validate();
            if (errors.isEmpty()) {
                ScreenDefinition definition = ScreenDefinition.fromConfig(config.screen());
                ScreenRegistry.INSTANCE.register(definition);
                LOGGER.info("Registered screen definition: {}", definition.id());
            } else {
                errors.forEach(error -> LOGGER.error("{}: {}", location, error.message()));
            }
        });
        pProfiler.pop();
    }
}
