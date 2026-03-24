package ninja.crinkle.mod.undergarment;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public record DiaperDesign(
        ResourceLocation id,
        String displayName,
        ResourceLocation armorTexture,
        ResourceLocation itemTexture,
        @Nullable Integer maxLiquids,
        @Nullable Integer maxSolids,
        List<OverlayConfig> overlays
) {
    public static DiaperDesign fromJson(ResourceLocation id, @NotNull JsonObject json) {
        String displayName = json.get("display_name").getAsString();
        ResourceLocation armorTexture = new ResourceLocation(json.get("armor_texture").getAsString());
        ResourceLocation itemTexture = new ResourceLocation(json.get("item_texture").getAsString());

        Integer maxLiquids = null;
        Integer maxSolids = null;
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

        return new DiaperDesign(id, displayName, armorTexture, itemTexture, maxLiquids, maxSolids, overlays);
    }

    public void toNetwork(@NotNull FriendlyByteBuf buf) {
        buf.writeResourceLocation(id);
        buf.writeUtf(displayName);
        buf.writeResourceLocation(armorTexture);
        buf.writeResourceLocation(itemTexture);
        buf.writeBoolean(maxLiquids != null);
        if (maxLiquids != null) buf.writeInt(maxLiquids);
        buf.writeBoolean(maxSolids != null);
        if (maxSolids != null) buf.writeInt(maxSolids);
        buf.writeInt(overlays.size());
        for (OverlayConfig overlay : overlays) {
            overlay.toNetwork(buf);
        }
    }

    public static DiaperDesign fromNetwork(@NotNull FriendlyByteBuf buf) {
        ResourceLocation id = buf.readResourceLocation();
        String displayName = buf.readUtf();
        ResourceLocation armorTexture = buf.readResourceLocation();
        ResourceLocation itemTexture = buf.readResourceLocation();
        Integer maxLiquids = buf.readBoolean() ? buf.readInt() : null;
        Integer maxSolids = buf.readBoolean() ? buf.readInt() : null;
        int overlayCount = buf.readInt();
        List<OverlayConfig> overlays = new ArrayList<>();
        for (int i = 0; i < overlayCount; i++) {
            overlays.add(OverlayConfig.fromNetwork(buf));
        }
        return new DiaperDesign(id, displayName, armorTexture, itemTexture, maxLiquids, maxSolids, overlays);
    }

    public record OverlayConfig(
            String type,
            List<String> parts,
            List<String> fadeColors
    ) {
        public static OverlayConfig fromJson(String type, @NotNull JsonObject json) {
            List<String> parts = new ArrayList<>();
            JsonArray partsArray = json.getAsJsonArray("parts");
            for (JsonElement el : partsArray) {
                parts.add(el.getAsString());
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

        public void toNetwork(@NotNull FriendlyByteBuf buf) {
            buf.writeUtf(type);
            buf.writeInt(parts.size());
            for (String part : parts) {
                buf.writeUtf(part);
            }
            buf.writeInt(fadeColors.size());
            for (String color : fadeColors) {
                buf.writeUtf(color);
            }
        }

        public static OverlayConfig fromNetwork(@NotNull FriendlyByteBuf buf) {
            String type = buf.readUtf();
            int partCount = buf.readInt();
            List<String> parts = new ArrayList<>();
            for (int i = 0; i < partCount; i++) {
                parts.add(buf.readUtf());
            }
            int replaceCount = buf.readInt();
            List<String> fadeColors = new ArrayList<>();
            for (int i = 0; i < replaceCount; i++) {
                fadeColors.add(buf.readUtf());
            }
            return new OverlayConfig(type, parts, fadeColors);
        }
    }
}