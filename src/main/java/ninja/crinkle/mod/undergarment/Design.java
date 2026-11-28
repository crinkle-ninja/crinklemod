package ninja.crinkle.mod.undergarment;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public record Design(
        ResourceLocation id,
        String displayName,
        ResourceLocation armorTexture,
        ResourceLocation itemTexture,
        ResourceLocation itemId,
        int maxLiquids,
        int maxSolids,
        List<OverlayConfig> overlays,
        NonNullList<Ingredient> ingredients
) {
    public static Design fromJson(ResourceLocation pId, @NotNull JsonObject json) {
        String cleanedPath = pId.getPath();
        cleanedPath = cleanedPath.substring(0, cleanedPath.lastIndexOf('.'));
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(pId.getNamespace(), cleanedPath);
        String displayName = json.get("display_name").getAsString();
        ResourceLocation armorTexture = ResourceLocation.parse(json.get("armor_texture").getAsString());
        ResourceLocation itemTexture = ResourceLocation.parse(json.get("item_texture").getAsString());
        ResourceLocation itemId = ResourceLocation.parse(json.get("base_item").getAsString());
        Item baseItem = ForgeRegistries.ITEMS.getValue(itemId);
        if (baseItem == null || baseItem == Items.AIR) {
            throw new JsonSyntaxException("invalid base item: " + itemId);
        }

        int maxLiquids = 0;
        int maxSolids = 0;
        if (json.has("stats")) {
            JsonObject stats = json.getAsJsonObject("stats");
            if (stats.has("max_liquids")) maxLiquids = stats.get("max_liquids").getAsInt();
            if (stats.has("max_solids")) maxSolids = stats.get("max_solids").getAsInt();
        }

        List<OverlayConfig> overlays = new ArrayList<>();
        if (json.has("texture_overlays")) {
            JsonObject overlaysJson = json.getAsJsonObject("texture_overlays");
            for (Map.Entry<String, JsonElement> entry : overlaysJson.entrySet()) {
                overlays.add(OverlayConfig.fromJson(entry.getKey(), entry.getValue().getAsJsonObject()));
            }
        }

        NonNullList<Ingredient> requiredIngredients = NonNullList.create();
        if (json.has("ingredients")) {
            JsonArray ingredientsJson = json.getAsJsonArray("ingredients");
            for (JsonElement jsonIngredient : ingredientsJson.asList()) {
                requiredIngredients.add(Ingredient.fromJson(jsonIngredient, false));
            }
        }
        return new Design(id, displayName, armorTexture, itemTexture, itemId, maxLiquids, maxSolids, overlays, requiredIngredients);
    }

    public static @Nullable Design fromNetwork(FriendlyByteBuf pBuffer) {
        ResourceLocation id = pBuffer.readResourceLocation();
        String displayName = pBuffer.readUtf();
        ResourceLocation armorTexture = pBuffer.readResourceLocation();
        ResourceLocation itemTexture = pBuffer.readResourceLocation();
        ResourceLocation itemId = pBuffer.readResourceLocation();
        Item baseItem = ForgeRegistries.ITEMS.getValue(itemId);
        if (baseItem == null || baseItem == Items.AIR) {
            return null;
        }
        int maxLiquids = pBuffer.readVarInt();
        int maxSolids = pBuffer.readVarInt();
        List<OverlayConfig> overlays = OverlayConfig.fromNetwork(pBuffer);
        int ingredientCount = pBuffer.readVarInt();
        NonNullList<Ingredient> ingredients = NonNullList.withSize(ingredientCount, Ingredient.EMPTY);
        if (ingredientCount != 0) {
            for (int i = 0; i < ingredientCount; i++) {
                ingredients.set(i, Ingredient.fromNetwork(pBuffer));
            }
        }
        return new Design(id, displayName, armorTexture, itemTexture, itemId, maxLiquids, maxSolids, overlays, ingredients);
    }

    public static void toNetwork(FriendlyByteBuf pBuffer, Design pDesign) {
        pBuffer.writeResourceLocation(pDesign.id());
        pBuffer.writeUtf(pDesign.displayName());
        pBuffer.writeResourceLocation(pDesign.armorTexture());
        pBuffer.writeResourceLocation(pDesign.itemTexture());
        pBuffer.writeResourceLocation(pDesign.itemId());
        pBuffer.writeVarInt(pDesign.maxLiquids());
        pBuffer.writeVarInt(pDesign.maxSolids());
        OverlayConfig.toNetwork(pBuffer, pDesign.overlays());
        pBuffer.writeVarInt(pDesign.ingredients().size());
        for(Ingredient ingredient : pDesign.ingredients()) {
            ingredient.toNetwork(pBuffer);
        }
    }


    @Override
    public @NotNull String toString() {
        return String.format(
                "Design{"
                        + "id=%s"
                        + ",displayName=%s"
                        + ",armorTexture=%s"
                        + ",itemTexture=%s"
                        +"}",
                id().toString(),
                displayName(),
                armorTexture().toString(),
                itemTexture().toString());
    }

    public record OverlayConfig(
            String type,
            List<String> parts,
            List<String> fadeColors
    ) {
        public static OverlayConfig fromJson(String type, @NotNull JsonObject json) {
            List<String> parts = new ArrayList<>();
            JsonArray partsArray = json.getAsJsonArray("parts");
            if (partsArray != null) {
                for (JsonElement el : partsArray) {
                    parts.add(el.getAsString());
                }
            }

            List<String> fadeColors = new ArrayList<>();
            JsonArray fadeArray = json.getAsJsonArray("fade_colors");
            if (fadeArray != null) {
                for (JsonElement el : fadeArray) {
                    fadeColors.add(el.getAsString());
                }
            }

            return new OverlayConfig(type, parts, fadeColors);
        }

        public static List<OverlayConfig> fromNetwork(FriendlyByteBuf pBuffer) {
            int count = pBuffer.readVarInt();
            List<OverlayConfig> overlays = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                String type = pBuffer.readUtf();
                int partCount = pBuffer.readVarInt();
                List<String> parts = new ArrayList<>(partCount);
                for (int part = 0; part < partCount; part++) {
                    parts.add(pBuffer.readUtf());
                }
                int fadeColorCount = pBuffer.readVarInt();
                List<String> fadeColors = new ArrayList<>();
                for (int color = 0; color < fadeColorCount; color++) {
                    fadeColors.add(pBuffer.readUtf());
                }
                overlays.add(new OverlayConfig(type, parts, fadeColors));
            }
            return overlays;
        }

        public static void toNetwork(FriendlyByteBuf pBuffer, List<OverlayConfig> overlays) {
            pBuffer.writeVarInt(overlays.size());
            for(OverlayConfig overlayConfig : overlays) {
                pBuffer.writeUtf(overlayConfig.type());
                pBuffer.writeVarInt(overlayConfig.parts().size());
                for(String part : overlayConfig.parts()) {
                    pBuffer.writeUtf(part);
                }
                pBuffer.writeVarInt(overlayConfig.fadeColors().size());
                for(String color : overlayConfig.fadeColors()) {
                    pBuffer.writeUtf(color);
                }
            }
        }
    }
}