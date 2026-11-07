package ninja.crinkle.mod.client.renderers;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import ninja.crinkle.mod.client.models.DiaperArmorModel;
import ninja.crinkle.mod.items.custom.DiaperArmorItem;
import ninja.crinkle.mod.undergarment.Undergarment;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

public class DiaperArmorRenderer extends GeoArmorRenderer<DiaperArmorItem> {
    public DiaperArmorRenderer() {
        super(new DiaperArmorModel());
    }

    @Override public ResourceLocation getTextureLocation(DiaperArmorItem animatable) {
        if (this.currentEntity instanceof Player player) {
            return Undergarment.of(player).texture();
        }
        return super.getTextureLocation(animatable);
    }



    @Override
    protected void applyBoneVisibilityBySlot(EquipmentSlot currentSlot) {
        super.applyBoneVisibilityBySlot(currentSlot);
        setBoneVisible(body, true);
    }
}
