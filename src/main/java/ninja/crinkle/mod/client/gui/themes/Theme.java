package ninja.crinkle.mod.client.gui.themes;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import ninja.crinkle.mod.client.color.Color;
import ninja.crinkle.mod.client.gui.animations.Animation;
import ninja.crinkle.mod.client.gui.builders.GenericBuilder;
import ninja.crinkle.mod.client.gui.textures.ColorFilters;
import ninja.crinkle.mod.client.gui.textures.Texture;
import ninja.crinkle.mod.client.gui.textures.TextureSize;
import ninja.crinkle.mod.client.gui.textures.ThemeAtlas;
import ninja.crinkle.mod.client.gui.themes.loader.ThemeData;
import org.slf4j.Logger;

import java.util.*;

public class Theme {
    public static final Theme EMPTY = Theme.builder("empty")
            .name("Empty")
            .description("Empty theme")
            .version("0.0.0")
            .addWidgetTheme(Style.builder("default").build())
            .build();
    private static final Logger LOGGER = LogUtils.getLogger();
    private final List<String> authors;
    private final Map<String, Color> colors = new HashMap<>();
    private final String description;
    private final Map<String, ResourceLocation> generatedTextures = new HashMap<>();
    private final String id;
    private final String name;
    private final Map<String, Texture> textures = new HashMap<>();
    private final String version;
    private final Map<String, Style> widgetThemes = new HashMap<>();
    private final Map<String, Animation> animations = new HashMap<>();


    public Theme(String id, String name, String description, String version, List<String> authors) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.version = version;
        this.authors = authors;
    }

    public static Builder builder(String id) {
        return new Builder(id);
    }

    public static Theme fromConfig(ThemeData config) {
        LOGGER.info("Loading theme '{}' ({})", config.name(), config.id());
        Theme theme = new Theme(config.id(), config.name(), config.description(), config.version(), config.authors());

        if (config.colors() != null) {
            config.colors().forEach((key, value) -> theme.colors.put(key, Color.of(value)));
        }

        if (config.textures() != null) {
            config.textures().forEach(texture -> theme.textures.put(texture.id(),
                            ThemeAtlas.getTextureLocation(theme.getId(), texture.location())
                                .map(location -> new Texture(texture.id(), texture.location(), texture.slices(), theme))
                                .orElse(Texture.EMPTY)));
        }

        if (config.styles() != null && !config.styles().isEmpty()) {
            config.styles().forEach(widgetData -> theme.widgetThemes.put(widgetData.id(), Style.fromConfig(widgetData, theme)));
        }

        if (config.animations() != null) {
            config.animations().forEach(animationData -> theme.animations.put(animationData.id(), animationData.animation()));
        }

        return theme;
    }

    public String getId() {
        return id;
    }

    public void addColor(String key, Color color) {
        colors.put(key, color);
    }

    public void addTexture(Texture texture) {
        textures.put(texture.id(), texture);
    }

    public void addWidgetTheme(Style style) {
        widgetThemes.put(style.id(), style);
    }

    public ResourceLocation generateTexture(Texture texture, TextureSize size, List<ColorFilters> colorFilters) {
        StringBuilder filters = new StringBuilder();
        for (var filter : colorFilters) {
            if (filter == ColorFilters.NORMAL)
                continue;
            filters.append(filter.toString()).append("_");
        }
        String key = String.format("%s%s_%s_%dx%d", filters, getId(), texture.id(), size.width(), size.height());
        if (generatedTextures.containsKey(key)
                && generatedTextures.get(key).getPath().equals(Texture.EMPTY.location())) {
            generatedTextures.remove(key);
        }
        if (size.equals(TextureSize.ZERO)) {
            throw new IllegalArgumentException("Size must be greater than 1x1: " + texture.id() + " - " + texture.location());
        }
        return generatedTextures.computeIfAbsent(key, k -> texture.generate(k, size, this, colorFilters));
    }

    public List<String> getAuthors() {
        return authors;
    }

    public Color getColor(String key) {
        return getOrDefault(colors, key, Color.RAINBOW);
    }

    public String getDescription() {
        return description;
    }

    public String getName() {
        return name;
    }

    private <T> T getOrDefault(Map<String, T> map, String key, T defaultValue) {
        if (map.containsKey(key)) {
            return map.get(key);
        } else if (this == ThemeRegistry.getDefault() || this == EMPTY) {
            LOGGER.warn("Missing key '{}' from '{}' in theme '{}'", key, defaultValue.getClass().getSimpleName(), getId());
            return defaultValue;
        }
        return ThemeRegistry.getDefault().getOrDefault(map, key, defaultValue);
    }

    public Texture getTexture(String key) {
        return getOrDefault(textures, key, Texture.EMPTY);
    }

    public String getVersion() {
        return version;
    }

    public Style getWidgetTheme(String id) {
        return getOrDefault(widgetThemes, id, Style.EMPTY);
    }

    public Optional<Animation> getAnimation(String id) {
        return Optional.ofNullable(animations.get(id));
    }

    public static class Builder extends GenericBuilder<Builder, Theme> {
        private final Map<String, Color> colors = new HashMap<>();
        private final String id;
        private final List<Texture> textures = new ArrayList<>();
        private final List<Style> styles = new ArrayList<>();
        private List<String> authors;
        private String description;
        private String name;
        private String version;

        public Builder(String id) {
            this.id = id;
        }

        public Builder addColor(String key, Color color) {
            colors.put(key, color);
            return self();
        }

        public Builder addTexture(Texture texture) {
            textures.add(texture);
            return self();
        }

        public Builder addWidgetTheme(Style style) {
            styles.add(style);
            return self();
        }

        public Builder authors(List<String> authors) {
            this.authors = authors;
            return self();
        }

        @Override
        public Theme build() {
            var theme = new Theme(id, name, description, version, authors);
            colors.forEach(theme::addColor);
            textures.forEach(theme::addTexture);
            styles.forEach(theme::addWidgetTheme);
            return theme;
        }

        @Override
        protected Builder self() {
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return self();
        }

        public Builder name(String name) {
            this.name = name;
            return self();
        }

        public Builder version(String version) {
            this.version = version;
            return self();
        }
    }
}
