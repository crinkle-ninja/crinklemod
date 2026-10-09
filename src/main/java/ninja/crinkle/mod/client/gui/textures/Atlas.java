package ninja.crinkle.mod.client.gui.textures;

import com.mojang.logging.LogUtils;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.TextureAtlasHolder;
import net.minecraft.resources.ResourceLocation;
import ninja.crinkle.mod.CrinkleMod;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.List;

public class Atlas extends TextureAtlasHolder {
    private static final Logger LOGGER = LogUtils.getLogger();

    public Atlas(TextureManager pTextureManager) {
        super(pTextureManager,
                new ResourceLocation(CrinkleMod.MODID, "textures/atlas/theme.png"),
                new ResourceLocation(CrinkleMod.MODID, "theme"));
    }

    @Override
    public @NotNull TextureAtlasSprite getSprite(@NotNull ResourceLocation pLocation) {
        return super.getSprite(pLocation);
    }

    public List<ResourceLocation> getSprites() {
        return this.textureAtlas.getTextureLocations().stream().toList();
    }
}
