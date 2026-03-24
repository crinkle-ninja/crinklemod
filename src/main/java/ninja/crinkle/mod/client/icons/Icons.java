package ninja.crinkle.mod.client.icons;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import ninja.crinkle.mod.CrinkleMod;
import ninja.crinkle.mod.client.gui.textures.ThemeAtlas;

public enum Icons {
    // ICONS! EVERY LAST ONE!
    BACK(new ResourceLocation(CrinkleMod.MODID, "theme/default/icons/back")),
    CHECKMARK(new ResourceLocation(CrinkleMod.MODID, "theme/default/icons/checkmark")),
    DOWN(new ResourceLocation(CrinkleMod.MODID, "theme/default/icons/down")),
    GEAR(new ResourceLocation(CrinkleMod.MODID, "theme/default/icons/gear")),
    RESET(new ResourceLocation(CrinkleMod.MODID, "theme/default/icons/reset")),
    SAVE(new ResourceLocation(CrinkleMod.MODID, "theme/default/icons/save")),
    WARNING1(new ResourceLocation(CrinkleMod.MODID, "theme/default/icons/warning1")),
    WARNING2(new ResourceLocation(CrinkleMod.MODID, "theme/default/icons/warning2")),
    WARNING3(new ResourceLocation(CrinkleMod.MODID, "theme/default/icons/warning3")),
    WRENCH(new ResourceLocation(CrinkleMod.MODID, "theme/default/icons/wrench")),
    WETNESS_OUTLINED(new ResourceLocation(CrinkleMod.MODID, "theme/default/icons/wetness_outlined")),
    WETNESS_DANGER(new ResourceLocation(CrinkleMod.MODID, "theme/default/icons/wetness_danger")),
    MESSINESS_OUTLINED(new ResourceLocation(CrinkleMod.MODID, "theme/default/icons/messiness_outlined")),
    MESSINESS_DANGER(new ResourceLocation(CrinkleMod.MODID, "theme/default/icons/messiness_danger")),
    ;


    private final ResourceLocation location;

    Icons(ResourceLocation location) {
        this.location = location;
    }

    @SuppressWarnings("resource")
    public int height() {
        return getSprite().contents().height();
    }

    @SuppressWarnings("resource")
    public int width() {
        return getSprite().contents().width();
    }

    public TextureAtlasSprite getSprite() {
        return ThemeAtlas.getSprite(location());
    }

    public ResourceLocation location() {
        return location;
    }
}
