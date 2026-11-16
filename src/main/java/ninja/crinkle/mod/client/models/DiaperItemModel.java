package ninja.crinkle.mod.client.models;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import ninja.crinkle.mod.items.custom.UndergarmentArmorItem;
import ninja.crinkle.mod.undergarment.DiaperDesign;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DiaperItemModel implements BakedModel {
    private final BakedModel defaultModel;
    private final Map<ResourceLocation, BakedModel> designModels = new ConcurrentHashMap<>();
    private final ItemOverrides overrides;

    public DiaperItemModel(BakedModel defaultModel) {
        this.defaultModel = defaultModel;
        this.overrides = new DesignOverrides();
    }

    public void registerDesignModel(ResourceLocation itemTexture, BakedModel model) {
        designModels.put(itemTexture, model);
    }

    @Override public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull RandomSource rand, @NotNull ModelData data, @Nullable RenderType renderType) {
        return defaultModel.getQuads(state, side, rand, data, renderType);
    }

    @SuppressWarnings("deprecation") @Override public @NotNull List<BakedQuad> getQuads(@Nullable BlockState pState, @Nullable Direction pDirection, @NotNull RandomSource pRandom) {
        return defaultModel.getQuads(pState, pDirection, pRandom);
    }

    @Override
    public boolean useAmbientOcclusion() {
        return defaultModel.useAmbientOcclusion();
    }

    @Override
    public boolean isGui3d() {
        return defaultModel.isGui3d();
    }

    @Override
    public boolean usesBlockLight() {
        return defaultModel.usesBlockLight();
    }

    @Override
    public boolean isCustomRenderer() {
        return defaultModel.isCustomRenderer();
    }

    @Override public @NotNull TextureAtlasSprite getParticleIcon() {
        return getParticleIcon(ModelData.EMPTY);
    }

    @SuppressWarnings("deprecation") @Override public @NotNull ItemTransforms getTransforms() {
        return defaultModel.getTransforms();
    }

    @Override public @NotNull TextureAtlasSprite getParticleIcon(@NotNull ModelData data) {
        return defaultModel.getParticleIcon(data);
    }

    @Override public @NotNull BakedModel applyTransform(@NotNull ItemDisplayContext transformType, @NotNull PoseStack poseStack, boolean applyLeftHandTransform) {
        return defaultModel.applyTransform(transformType, poseStack, applyLeftHandTransform);
    }

    @Override
    public @NotNull ItemOverrides getOverrides() {
        return overrides;
    }

    private class DesignOverrides extends ItemOverrides {
        @Override
        public BakedModel resolve(@NotNull BakedModel model, @NotNull ItemStack stack,
                                  @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed) {
            if (stack.getItem() instanceof UndergarmentArmorItem) {
                DiaperDesign design = UndergarmentArmorItem.getDesign(stack, true);
                if (design == null) {
                    // Fall back to server-side registry if client hasn't synced yet
                    design = UndergarmentArmorItem.getDesign(stack, false);
                }
                if (design != null) {
                    BakedModel designModel = designModels.get(design.itemTexture());
                    if (designModel != null) {
                        return designModel;
                    }
                }
            }
            return defaultModel;
        }
    }
}