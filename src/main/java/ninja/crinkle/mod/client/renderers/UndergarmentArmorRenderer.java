package ninja.crinkle.mod.client.renderers;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import ninja.crinkle.mod.items.custom.UndergarmentArmorItem;
import ninja.crinkle.mod.undergarment.Undergarment;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

public class UndergarmentArmorRenderer extends GeoArmorRenderer<UndergarmentArmorItem> {
    public UndergarmentArmorRenderer(GeoModel<UndergarmentArmorItem> model) {
        super(model);
    }

    @Override public ResourceLocation getTextureLocation(UndergarmentArmorItem animatable) {
        return Undergarment.of(getCurrentEntity()).texture();
    }



    @Override
    protected void applyBoneVisibilityBySlot(EquipmentSlot currentSlot) {
        super.applyBoneVisibilityBySlot(currentSlot);
        setBoneVisible(body, true);
    }
}
