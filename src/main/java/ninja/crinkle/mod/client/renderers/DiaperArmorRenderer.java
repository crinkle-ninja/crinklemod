package ninja.crinkle.mod.client.renderers;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import ninja.crinkle.mod.client.models.DiaperArmorModel;
import ninja.crinkle.mod.items.custom.UndergarmentArmorItem;
import ninja.crinkle.mod.undergarment.Undergarment;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

public class DiaperArmorRenderer extends GeoArmorRenderer<UndergarmentArmorItem> {
    public DiaperArmorRenderer() {
        super(new DiaperArmorModel());
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
