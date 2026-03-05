package ninja.crinkle.mod.client.gui.textures;

import com.mojang.logging.LogUtils;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.TextureAtlasHolder;
import net.minecraft.resources.ResourceLocation;
import ninja.crinkle.mod.CrinkleMod;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class Atlas extends TextureAtlasHolder {
    private static final Logger LOGGER = LogUtils.getLogger();

    public Atlas(TextureManager pTextureManager) {
        super(pTextureManager,
                new ResourceLocation(CrinkleMod.MODID, "textures/atlas/theme.png"),
                new ResourceLocation(CrinkleMod.MODID, "theme"));
    }

    public List<ResourceLocation> getSprites() {
        return this.textureAtlas.getTextureLocations().stream().toList();
    }

    @Override
    public @NotNull CompletableFuture<Void> reload(@NotNull PreparationBarrier pPreparationBarrier,
            @NotNull ResourceManager pResourceManager, @NotNull ProfilerFiller pPreparationProfiler,
            @NotNull ProfilerFiller pApplyProfiler, @NotNull Executor pPreparationExecutor,
            @NotNull Executor pApplyExecutor) {
        return super.reload(pPreparationBarrier, pResourceManager, pPreparationProfiler, pApplyProfiler,
                pPreparationExecutor, pApplyExecutor)
                .thenRun(() -> {
                    ThemeAtlas.clearTextures();
                    getSprites().forEach(ThemeAtlas::register);
                    LOGGER.debug("Re-registered {} theme textures after atlas reload", getSprites().size());
                });
    }

    @Override
    public @NotNull TextureAtlasSprite getSprite(@NotNull ResourceLocation pLocation) {
        return super.getSprite(pLocation);
    }
}
