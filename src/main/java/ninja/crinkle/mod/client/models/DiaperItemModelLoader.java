package ninja.crinkle.mod.client.models;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import ninja.crinkle.mod.CrinkleMod;
import ninja.crinkle.mod.items.CrinkleItems;
import org.slf4j.Logger;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Mod.EventBusSubscriber(modid = CrinkleMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class DiaperItemModelLoader {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Set<ResourceLocation> registeredModelIds = new HashSet<>();

    @SubscribeEvent
    public static void onRegisterAdditionalModels(ModelEvent.RegisterAdditional event) {
        var resourceManager = Minecraft.getInstance().getResourceManager();
        resourceManager.listResources("models/diaper_design", loc ->
                loc.getNamespace().equals(CrinkleMod.MODID) && loc.getPath().endsWith(".json")
        ).keySet().forEach(modelFile -> {
            // modelFile = crinklemod:models/diaper_design/diaper_little_pawz.json
            String modelPath = modelFile.getPath()
                    .replace("models/", "")
                    .replace(".json", "");
            ResourceLocation modelId = CrinkleMod.loc(modelPath);
            registeredModelIds.add(modelId);
            event.register(modelId);
            LOGGER.debug("Registered diaper design model: {}", modelId);
        });
    }

    @SubscribeEvent
    public static void onModifyBakingResult(ModelEvent.ModifyBakingResult event) {
        ModelResourceLocation diaperModelLoc = new ModelResourceLocation(
                Objects.requireNonNull(CrinkleItems.DIAPER.getId()), "inventory");
        BakedModel originalModel = event.getModels().get(diaperModelLoc);
        if (originalModel == null) {
            LOGGER.error("Could not find diaper item model at {}", diaperModelLoc);
            return;
        }

        DiaperItemModel wrappedModel = new DiaperItemModel(originalModel);

        // Map each registered design model by its texture path (item_texture in design JSON)
        for (ResourceLocation modelId : registeredModelIds) {
            // modelId = crinklemod:diaper_design/diaper_little_pawz
            // item_texture in design JSON = crinklemod:item/diaper_little_pawz
            String fileName = modelId.getPath().replace("diaper_design/", "");
            ResourceLocation itemTexture = CrinkleMod.loc("item/" + fileName);

            BakedModel bakedModel = event.getModels().get(modelId);
            if (bakedModel != null) {
                wrappedModel.registerDesignModel(itemTexture, bakedModel);
                LOGGER.debug("Mapped item texture {} to baked design model", itemTexture);
            }
        }

        event.getModels().put(diaperModelLoc, wrappedModel);
        LOGGER.info("Installed diaper item model wrapper with {} design variants", registeredModelIds.size());
    }
}