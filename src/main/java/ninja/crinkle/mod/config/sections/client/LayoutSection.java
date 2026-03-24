package ninja.crinkle.mod.config.sections.client;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import net.minecraftforge.common.ForgeConfigSpec;
import ninja.crinkle.mod.client.gui.properties.ScreenRegion;
import ninja.crinkle.mod.config.ClientConfig;
import org.slf4j.Logger;

import java.util.Optional;

public record LayoutSection(ForgeConfigSpec.ConfigValue<String> positions) {
    private static final Gson GSON = new Gson();
    private static final Logger LOGGER = LogUtils.getLogger();

    public Optional<ScreenRegion.AnchoredPosition> getPosition(String id) {
        JsonObject root = parsePositions();
        if (!root.has(id)) return Optional.empty();
        try {
            JsonObject entry = root.getAsJsonObject(id);
            ScreenRegion anchor = ScreenRegion.valueOf(entry.get("anchor").getAsString());
            int x = entry.get("x").getAsInt();
            int y = entry.get("y").getAsInt();
            return Optional.of(new ScreenRegion.AnchoredPosition(anchor, x, y));
        } catch (Exception e) {
            LOGGER.warn("Failed to parse position for '{}'", id, e);
            return Optional.empty();
        }
    }

    private JsonObject parsePositions() {
        try {
            JsonElement element = JsonParser.parseString(positions.get());
            if (element.isJsonObject()) {
                return element.getAsJsonObject();
            }
        } catch (Exception e) {
            LOGGER.warn("Failed to parse layout positions, resetting to empty", e);
        }
        return new JsonObject();
    }

    public void save() {
        ClientConfig.getSpec().save();
    }

    public void setPosition(String id, ScreenRegion.AnchoredPosition pos) {
        JsonObject root = parsePositions();
        JsonObject entry = new JsonObject();
        entry.addProperty("anchor", pos.anchor().name());
        entry.addProperty("x", pos.offsetX());
        entry.addProperty("y", pos.offsetY());
        root.add(id, entry);
        positions.set(GSON.toJson(root));
    }
}
