package ninja.crinkle.mod.items.custom;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import ninja.crinkle.mod.CrinkleMod;
import ninja.crinkle.mod.client.models.DiaperArmorModel;
import ninja.crinkle.mod.client.renderers.DiaperArmorRenderer;
import ninja.crinkle.mod.undergarment.DiaperDesign;
import ninja.crinkle.mod.undergarment.DiaperDesignRegistry;
import ninja.crinkle.mod.undergarment.Undergarment;
import ninja.crinkle.mod.util.MathUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.function.Consumer;

public class DiaperArmorItem extends ArmorItem implements GeoItem {

    private static final String DESIGN_TAG = CrinkleMod.MODID + ".design";
    public static final ResourceLocation DEFAULT_TEXTURE = CrinkleMod.loc("armor/diaper_white");
    public static final DiaperArmorModel SHARED_MODEL = new DiaperArmorModel();
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    public DiaperArmorItem(ArmorMaterial pMaterial, @NotNull Properties pProperties) {
        super(pMaterial, ArmorItem.Type.LEGGINGS, pProperties.rarity(Rarity.EPIC).durability(1000));
    }

    public static ResourceLocation getDesignId(@NotNull ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains(DESIGN_TAG)) {
            return ResourceLocation.parse(tag.getString(DESIGN_TAG));
        }
        return DiaperDesignRegistry.PLAIN_ID;
    }

    public static void setDesignId(@NotNull ItemStack stack, @NotNull ResourceLocation designId) {
        stack.getOrCreateTag().putString(DESIGN_TAG, designId.toString());
    }

    @Nullable
    public static DiaperDesign getDesign(@NotNull ItemStack stack, boolean clientSide) {
        return DiaperDesignRegistry.getDesign(getDesignId(stack), clientSide).orElse(null);
    }

    @Override
    public @NotNull Component getName(@NotNull ItemStack stack) {
        DiaperDesign design = getDesign(stack, false);
        if (design != null) {
            return Component.translatable(design.displayName());
        }
        return super.getName(stack);
    }

    public ResourceLocation getTexture() {
        throw new IllegalCallerException();
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
        Undergarment undergarment = Undergarment.EMPTY;
        if (state.getData(DataTickets.ENTITY) instanceof Player p) {
            undergarment = Undergarment.of(p);
        }
        if (undergarment == Undergarment.EMPTY) return PlayState.STOP;

        int pctL = MathUtil.twenties((int) (undergarment.getLiquidsPercent() * 100));
        int pctS = MathUtil.twenties((int) (undergarment.getSolidsPercent() * 100));

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
}