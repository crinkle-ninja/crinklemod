package ninja.crinkle.mod.client.models;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import ninja.crinkle.mod.items.custom.DiaperArmorItem;
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

    @Override
    public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction direction,
                                             @NotNull RandomSource random) {
        return defaultModel.getQuads(state, direction, random);
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

    @Override
    public @NotNull TextureAtlasSprite getParticleIcon() {
        return defaultModel.getParticleIcon();
    }

    @Override
    public @NotNull ItemTransforms getTransforms() {
        return defaultModel.getTransforms();
    }

    @Override
    public @NotNull ItemOverrides getOverrides() {
        return overrides;
    }

    private class DesignOverrides extends ItemOverrides {
        @Override
        public BakedModel resolve(@NotNull BakedModel model, @NotNull ItemStack stack,
                                  @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed) {
            if (stack.getItem() instanceof DiaperArmorItem) {
                DiaperDesign design = DiaperArmorItem.getDesign(stack, true);
                if (design == null) {
                    // Fall back to server-side registry if client hasn't synced yet
                    design = DiaperArmorItem.getDesign(stack, false);
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