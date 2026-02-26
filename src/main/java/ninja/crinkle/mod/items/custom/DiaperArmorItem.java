package ninja.crinkle.mod.items.custom;

import com.mojang.datafixers.util.Either;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import ninja.crinkle.mod.client.models.DiaperArmorModel;
import ninja.crinkle.mod.client.renderers.DiaperArmorRenderer;
import ninja.crinkle.mod.client.textures.Textures;
import ninja.crinkle.mod.client.textures.generators.DiaperTextureGenerator;
import ninja.crinkle.mod.undergarment.Undergarment;
import ninja.crinkle.mod.util.MathUtil;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class DiaperArmorItem extends ArmorItem implements GeoItem {
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private final ResourceLocation defaultTexture;
    private ResourceLocation texture;

    public DiaperArmorItem(ArmorMaterial pMaterial, @NotNull DiaperVariant variant, @NotNull Properties pProperties) {
        super(pMaterial, ArmorItem.Type.LEGGINGS, pProperties.rarity(Rarity.EPIC).durability(1000));
        this.defaultTexture = variant.getTexture();
    }

    @Override
    public void initializeClient(@NotNull Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private DiaperArmorRenderer renderer = null;


            @Override
            public @NotNull HumanoidModel<?> getHumanoidArmorModel(LivingEntity livingEntity, ItemStack itemStack,
                                                                   EquipmentSlot equipmentSlot,
                                                                   HumanoidModel<?> original) {
                if (this.renderer == null) {
                    this.renderer = new DiaperArmorRenderer();
                }
                this.renderer.prepForRender(livingEntity, itemStack, equipmentSlot, original);
                return this.renderer;
            }
        });
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {
        controllerRegistrar.add(new AnimationController<>(this, "controller", 0,
                this::predicate));
    }

    private PlayState predicate(AnimationState<DiaperArmorItem> state) {
        Undergarment undergarment = Undergarment.of(state.getData(DataTickets.ITEMSTACK));
        int pctL = MathUtil.twenties((int) (undergarment.getLiquidsPercent() * 100));
        int pctS = MathUtil.twenties((int) (undergarment.getSolidsPercent() * 100));

        DiaperTextureGenerator.Data data =
                new DiaperTextureGenerator.Data(getDescriptionId(), new DiaperArmorModel(), undergarment);
        texture = Textures.getInstance().getTexture(defaultTexture, data);

        if (pctL == 0 && pctS == 0) {
            return PlayState.STOP;
        }
        String animationName = "animation.diaper.wet" + pctL + "mess" + pctS;
        return state.setAndContinue(RawAnimation.begin().thenPlay(animationName));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return geoCache;
    }

    public ResourceLocation getTexture() {
        return Optional.ofNullable(texture).orElse(defaultTexture);
    }
}
