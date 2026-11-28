package ninja.crinkle.mod.undergarment;

import com.google.gson.*;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import ninja.crinkle.mod.CrinkleMod;
import ninja.crinkle.mod.util.ClientUtil;
import org.slf4j.Logger;

import java.util.*;

public class DesignRegistry {
    public static final ResourceLocation PLAIN_ID = CrinkleMod.loc("designs/diaper_white");
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final Map<ResourceLocation, Design> DESIGNS = new LinkedHashMap<>();

    public static List<Design> getDesignsForItem(ItemStack stack) {
        List<Design> matches = new ArrayList<>();
        for (Design design : getAllDesigns()) {
            for (Ingredient ingredient : design.ingredients()) {
                if (ingredient.test(stack)) {
                    matches.add(design);
                    break;
                }
            }
        }
        return matches;
    }

    public static void loadDesigns(ResourceManager resourceManager) {
        Map<ResourceLocation, Resource> found =
                resourceManager.listResources("designs", id -> id.getPath().endsWith(".json"));
        for (Map.Entry<ResourceLocation, Resource> entry : found.entrySet()) {
            try (var reader = entry.getValue().openAsReader()) {
                JsonObject designJson = GsonHelper.parse(reader);
                Design design = Design.fromJson(entry.getKey(), designJson);
                DESIGNS.put(design.id(), design);
                LOGGER.info("Loaded diaper design: {}", entry.getKey());
            } catch (Exception e) {
                throw new JsonSyntaxException(e);
            }
        }
        LOGGER.info("Loaded {} diaper design(s)", DESIGNS.size());
        ClientUtil.rebuildTextures();
    }

    public static Map<ResourceLocation, Design> getDesigns() {
        return Collections.unmodifiableMap(DESIGNS);
    }

    public static Optional<Design> getDesign(ResourceLocation id) {
        return Optional.ofNullable(DESIGNS.get(id));
    }

    public static Collection<Design> getAllDesigns() {
        return DESIGNS.values();
    }
}