package ninja.crinkle.mod.client.gui.textures;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.TextureAtlasHolder;
import net.minecraft.resources.ResourceLocation;
import ninja.crinkle.mod.CrinkleMod;
import org.jetbrains.annotations.NotNull;

public class Atlas extends TextureAtlasHolder {

    public Atlas(TextureManager pTextureManager) {
        super(pTextureManager,
                CrinkleMod.loc("textures/atlas/theme.png"),
                CrinkleMod.loc("theme"));
    }

    @Override
    public @NotNull TextureAtlasSprite getSprite(@NotNull ResourceLocation pLocation) {
        return super.getSprite(pLocation);
    }
}
