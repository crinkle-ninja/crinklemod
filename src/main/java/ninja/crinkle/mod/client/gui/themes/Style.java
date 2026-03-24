package ninja.crinkle.mod.client.gui.themes;

import com.mojang.logging.LogUtils;
import ninja.crinkle.mod.client.color.Color;
import ninja.crinkle.mod.client.gui.builders.GenericBuilder;
import ninja.crinkle.mod.client.gui.properties.Rect;
import ninja.crinkle.mod.client.gui.renderers.ThemeGraphics;
import ninja.crinkle.mod.client.gui.textures.ColorFilters;
import ninja.crinkle.mod.client.gui.textures.Texture;
import ninja.crinkle.mod.client.gui.themes.loader.StyleData;
import ninja.crinkle.mod.client.gui.widgets.AbstractWidget;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

public record Style(String id, Map<Variant, StyleVariant> variants, Theme theme, Style parent) {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static Builder builder(String id) {
        return new Builder(id);
    }

    public static Style defaultStyle() {
        return ThemeRegistry.defaultTheme().style("default");
    }

    public static Style fromConfig(StyleData styleData, Theme theme) {
        Map<Variant, StyleVariant> variants = new HashMap<>();
        Style parent = Optional.ofNullable(styleData.parent())
                .map(theme::style)
                .orElse(null);
        if (parent != null) {
            LOGGER.info("Loading style '{}' with parent '{}'", styleData.id(), parent.id());
            variants.putAll(parent.variants());
        } else {
            LOGGER.info("Loading style '{}' with no parent", styleData.id());
        }
        for (var entry : styleData.variants().entrySet()) {
            Variant variant = entry.getKey();
            StyleData.VariantData appearance = entry.getValue();
            Texture background = Optional.ofNullable(appearance.background())
                    .filter(data -> data.texture() != null)
                    .map(data -> theme.texture(data.texture()))
                    .orElse(null);
            Texture foreground = Optional.ofNullable(appearance.foreground())
                    .filter(data -> data.texture() != null)
                    .map(data -> theme.texture(data.texture()))
                    .orElse(null);
            Color backgroundColor = Optional.ofNullable(appearance.background())
                    .filter(data -> data.color() != null)
                    .map(data -> theme.color(data.color()))
                    .orElse(null);
            Color foregroundColor = Optional.ofNullable(appearance.foreground())
                    .filter(data -> data.color() != null)
                    .map(data -> theme.color(data.color()))
                    .orElse(null);
            List<ColorFilters> foregroundColorFilters = Optional.ofNullable(appearance.foreground())
                    .map(StyleData.VariantData.LayerData::colorFilters)
                    .orElse(List.of());
            List<ColorFilters> backgroundColorFilters = Optional.ofNullable(appearance.background())
                    .map(StyleData.VariantData.LayerData::colorFilters)
                    .orElse(List.of());
            Color shadow = Optional.ofNullable(appearance.foreground())
                    .map(data -> theme.color(data.shadow()))
                    .orElse(null);
            StyleVariant styleVariant = new StyleVariant(background, foreground, backgroundColor,
                    foregroundColor, shadow, foregroundColorFilters, backgroundColorFilters);
            LOGGER.debug("Adding variant '{}' to style '{}'", variant, styleVariant);
            variants.put(variant, styleVariant);
        }
        return new Style(styleData.id(), variants, theme, parent);
    }

    public void render(ThemeGraphics graphics, Rect rect, AbstractWidget widget) {
        StyleVariant styleVariant = widget.appearance();
        if (styleVariant == null) {
            styleVariant = variants.get(Style.Variant.active);
        }
        if (styleVariant != null) {
            styleVariant.render(graphics, rect, widget);
        } else {
            LOGGER.warn("No appearance found for widget {} at all", widget.name());
        }
    }

    @Override
    public @NotNull String toString() {
        return "Style{" +
                "id='" + id + '\'' +
                ", variants=" + variants +
                ", theme=" + theme +
                '}';
    }

    public StyleVariant variant(Variant variant) {
        return variants.get(variant);
    }

    public enum Variant {
        base(variant -> true, 0),
        active(AbstractWidget::active, 2),
        inactive(widget -> !widget.active(), 1),
        focused(w -> w.focusable() && w.focused(), 3),
        hover(w -> w.behavior().hoverable() && w.hovered(), 4),
        pressed(w -> w.behavior().pressable() && w.pressed(), 5);

        private final Predicate<AbstractWidget> predicate;
        private final int rank;

        Variant(Predicate<AbstractWidget> predicate, int rank) {
            this.predicate = predicate;
            this.rank = rank;
        }

        public static List<Variant> from(AbstractWidget widget) {
            return Stream.of(values())
                    .filter(variant -> variant.matches(widget))
                    .sorted(Comparator.comparingInt(Variant::rank))
                    .toList();
        }

        public boolean matches(AbstractWidget widget) {
            return predicate.test(widget);
        }

        public int rank() {
            return rank;
        }
    }

    public static class Builder extends GenericBuilder<Builder, Style> {
        private final String id;
        private final Map<Variant, StyleVariant> variants = new HashMap<>();
        private Style parent;
        private Theme theme;

        public Builder(String id) {
            this.id = id;
        }

        public Builder addVariant(Variant variant, StyleVariant styleVariant) {
            variants.put(variant, styleVariant);
            return self();
        }

        @Override
        public Style build() {
            return new Style(id, variants, theme, parent);
        }

        @Override
        protected Builder self() {
            return this;
        }

        public Builder parent(Style parent) {
            this.parent = parent;
            return self();
        }

        public Builder theme(Theme theme) {
            this.theme = theme;
            return self();
        }
    }
}
