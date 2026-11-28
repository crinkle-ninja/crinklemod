package ninja.crinkle.mod.client.models;

import net.minecraft.resources.ResourceLocation;
import ninja.crinkle.mod.CrinkleMod;
import ninja.crinkle.mod.items.custom.UndergarmentArmorItem;
import software.bernie.geckolib.model.GeoModel;

public class DiaperArmorModel extends GeoModel<UndergarmentArmorItem> {
    public static final ResourceLocation DEFAULT_TEXTURE =
            CrinkleMod.loc("textures/armor/diaper_white.png");
    @Override
    public ResourceLocation getModelResource(UndergarmentArmorItem animatable) {
        return CrinkleMod.loc("geo/armor/diaper.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(UndergarmentArmorItem animatable) {
        return DEFAULT_TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(UndergarmentArmorItem animatable) {
        return CrinkleMod.loc("animations/armor/diaper.animation.json");
    }
}
