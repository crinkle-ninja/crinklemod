package ninja.crinkle.mod.client.models;

import net.minecraft.resources.ResourceLocation;
import ninja.crinkle.mod.CrinkleMod;
import ninja.crinkle.mod.items.custom.DiaperArmorItem;
import software.bernie.geckolib.model.GeoModel;

public class DiaperArmorModel extends GeoModel<DiaperArmorItem> {
    @Override
    public ResourceLocation getModelResource(DiaperArmorItem animatable) {
        return ResourceLocation.fromNamespaceAndPath(CrinkleMod.MODID, "geo/armor/diaper.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(DiaperArmorItem animatable) {
        return animatable.getTexture();
    }

    @Override
    public ResourceLocation getAnimationResource(DiaperArmorItem animatable) {
        return CrinkleMod.loc("animations/armor/diaper.animation.json");
    }
}
