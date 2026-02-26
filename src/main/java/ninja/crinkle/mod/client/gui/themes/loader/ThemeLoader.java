package ninja.crinkle.mod.client.gui.themes.loader;

import com.google.gson.Gson;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import ninja.crinkle.mod.client.gui.textures.Texture;
import ninja.crinkle.mod.client.gui.textures.ThemeAtlas;
import org.apache.commons.compress.utils.Lists;
import org.slf4j.Logger;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ThemeLoader {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String THEMES_PATH = "themes";
    private static final Gson GSON = new Gson();

    public static Map<ResourceLocation, Config> loadConfigs(ResourceManager resourceManager) {
        Map<ResourceLocation, Config> themes = new HashMap<>();
        resourceManager.listResources(THEMES_PATH, (path) -> path.getPath().endsWith(".json")
                        && !path.getPath().split("/")[1].startsWith("schema"))
                .forEach((location, resource) -> {
                    try {
                        Config themeConfig = GSON.fromJson(resource.openAsReader(), Config.class);
                        if (themes.values().stream().anyMatch(config ->
                                config.theme().id().equals(themeConfig.theme().id()))) {
                            // I will never get the hours back...
                            throw new IllegalStateException("Duplicate theme id: " + themeConfig.theme().id()
                                    + ", path: " + location);
                        }
                        themes.put(location, themeConfig);
                    } catch (Exception e) {
                        LOGGER.error("Failed to load theme from {}", location, e);
                    }
                });
        return themes;
    }

    public record Config(int version, ThemeData theme) {
        public record Error(String message) {
        }

        private List<Error> validateTexturesExist() {
            return theme.textures().stream()
                    .flatMap(texture -> texture.locations(theme.id()).stream())
                    .filter(location -> (!ThemeAtlas.hasTexture(theme.id(), location)))
                    .map(location -> new Error("Texture '" + location + "' for theme '" + theme.id()
                            + "' does not exist."))
                    .toList();
        }

        private List<Error> validateTextureSlices() {
            List<Error> errors = Lists.newArrayList();
            errors.addAll(theme.textures().stream()
                    .filter(texture -> texture.slices() != null && !texture.slices().isEmpty())
                            .flatMap(texture -> {
                                List<Error> textureErrors = Lists.newArrayList();
                                List<Texture.Slice.Location> foundLocations = Lists.newArrayList();
                                texture.slices().forEach((location, slice) -> {
                                    if (slice.start() == null) {
                                        textureErrors.add(new Error("Texture slice '" + location + "' for texture '"
                                                + texture.id() + "' is missing start position."));
                                    }
                                    if (slice.size() == null) {
                                        textureErrors.add(new Error("Texture slice '" + location + "' for texture '"
                                                + texture.id() + "' is missing bounds."));
                                    }
                                    if (foundLocations.contains(location)) {
                                        textureErrors.add(new Error("Texture slice '" + location + "' for texture '"
                                                + texture.id() + "' is duplicated."));
                                    }
                                    foundLocations.add(location);
                                });
                                Arrays.stream(Texture.Slice.Location.values()).forEach(location -> {
                                    if (!foundLocations.contains(location)) {
                                        textureErrors.add(new Error("Texture slice '" + location + "' for texture '"
                                                + texture.id() + "' is missing."));
                                    }
                                });
                                return textureErrors.stream();
                            })
                    .toList());
            return errors;
        }

        public List<Error> validate() {
            List<Error> errors = Lists.newArrayList();
            if (theme == null) {
                errors.add(new Error("Theme data is missing."));
            }
            if (version != 1) {
                errors.add(new Error("Invalid theme version: " + version));
            }
            if (theme().id() == null || theme().id().isEmpty()) {
                errors.add(new Error("Theme id is missing or empty."));
            }
            if (theme().name() == null || theme().name().isEmpty()) {
                errors.add(new Error("Theme name is missing or empty."));
            }
            errors.addAll(validateTexturesExist());
            errors.addAll(validateTextureSlices());
            return errors;
        }
    }
}
