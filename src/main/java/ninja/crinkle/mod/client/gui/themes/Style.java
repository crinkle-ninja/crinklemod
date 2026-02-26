package ninja.crinkle.mod.client.gui.themes;

import com.mojang.logging.LogUtils;
import ninja.crinkle.mod.client.color.Color;
import ninja.crinkle.mod.client.gui.builders.GenericBuilder;
import ninja.crinkle.mod.client.gui.properties.Box;
import ninja.crinkle.mod.client.gui.renderers.ThemeGraphics;
import ninja.crinkle.mod.client.gui.textures.ColorFilters;
import ninja.crinkle.mod.client.gui.textures.Texture;
import ninja.crinkle.mod.client.gui.themes.loader.StyleData;
import ninja.crinkle.mod.client.gui.widgets.AbstractWidget;
import org.slf4j.Logger;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

public record Style(String id, Map<Variant, StyleVariant> appearances, Theme theme, Style parent) {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final Style EMPTY = Style.builder("empty")
            .addAppearance(Style.Variant.base, StyleVariant.EMPTY)
            .addAppearance(Style.Variant.active, StyleVariant.EMPTY)
            .addAppearance(Style.Variant.hover, StyleVariant.EMPTY)
            .addAppearance(Style.Variant.inactive, StyleVariant.EMPTY)
            .addAppearance(Style.Variant.pressed, StyleVariant.EMPTY)
            .addAppearance(Style.Variant.focused, StyleVariant.EMPTY)
            .theme(Theme.EMPTY).build();

    public static Builder builder(String id) {
        return new Builder(id);
    }

    public static Style fromConfig(StyleData styleData, Theme theme) {
        Map<Variant, StyleVariant> appearances = new HashMap<>();
        Style parent = Optional.ofNullable(styleData.parent())
                .map(theme::getWidgetTheme)
                .orElse(null);
        if (parent != null) {
            LOGGER.info("Loading style '{}' with parent '{}'", styleData.id(), parent.id());
            appearances.putAll(parent.appearances());
        } else {
            LOGGER.info("Loading style '{}' with no parent", styleData.id());
        }
        for (var entry : styleData.variants().entrySet()) {
            Variant variant = entry.getKey();
            StyleData.Variant appearance = entry.getValue();
            StyleVariant parentAppearance = Optional.ofNullable(parent)
                    .map(p -> p.getAppearance(variant))
                    .orElse(null);
            Texture background = Optional.ofNullable(appearance.background())
                    .filter(data -> data.texture() != null)
                    .map(data -> theme.getTexture(data.texture()))
                    .orElse(null);
            Texture foreground = Optional.ofNullable(appearance.foreground())
                    .filter(data -> data.texture() != null)
                    .map(data -> theme.getTexture(data.texture()))
                    .orElse(null);
            Color backgroundColor = Optional.ofNullable(appearance.background())
                    .filter(data -> data.color() != null)
                    .map(data -> theme.getColor(data.color()))
                    .orElse(null);
            Color foregroundColor = Optional.ofNullable(appearance.foreground())
                    .filter(data -> data.color() != null)
                    .map(data -> theme.getColor(data.color()))
                    .orElse(null);
            List<ColorFilters> foregroundColorFilters = Optional.ofNullable(appearance.foreground())
                    .map(StyleData.Variant.Data::getFilters)
                    .orElse(List.of());
            List<ColorFilters> backgroundColorFilters = Optional.ofNullable(appearance.background())
                    .map(StyleData.Variant.Data::getFilters)
                    .orElse(List.of());
            StyleVariant styleVariant = new StyleVariant(background, foreground, backgroundColor,
                    foregroundColor, appearance.foreground() != null && appearance.foreground().shadow(),
                    foregroundColorFilters, backgroundColorFilters, parentAppearance);
            LOGGER.debug("Adding appearance '{}' to style '{}'", variant, styleVariant);
            appearances.put(variant, styleVariant);
        }
        return new Style(styleData.id(), appearances, theme, parent);
    }

    public static Style getDefault() {
        return ThemeRegistry.getDefault().getWidgetTheme("default");
    }

    public StyleVariant getAppearance(Variant variant) {
        return appearances.get(variant);
    }

    public void render(ThemeGraphics graphics, Box pBox, AbstractWidget widget) {
        StyleVariant styleVariant = widget.appearance();
        if (styleVariant == null) {
            styleVariant = appearances.get(Style.Variant.active);
        }
        if (styleVariant != null) {
            styleVariant.render(graphics, pBox, widget);
        } else {
            LOGGER.warn("No appearance found for widget {} at all", widget.name());
        }
    }

    @Override
    public String toString() {
        return "WidgetTheme{" +
                "id='" + id + '\'' +
                ", appearances=" + appearances +
                ", theme=" + theme +
                '}';
    }

    public static class Builder extends GenericBuilder<Builder, Style> {
        private final Map<Variant, StyleVariant> appearances = new HashMap<>();
        private final String id;
        private Style parent;
        private Theme theme;

        public Builder(String id) {
            this.id = id;
        }

        public Builder addAppearance(Variant variant, StyleVariant appearance) {
            appearances.put(variant, appearance);
            return self();
        }

        @Override
        public Style build() {
            return new Style(id, appearances, theme, parent);
        }

        public Builder parent(Style parent) {
            this.parent = parent;
            return self();
        }

        @Override
        protected Builder self() {
            return this;
        }

        public Builder theme(Theme theme) {
            this.theme = theme;
            return self();
        }
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

        public boolean matches(AbstractWidget widget) {
            return predicate.test(widget);
        }

        public int rank() {
            return rank;
        }

        public static List<Variant> from(AbstractWidget widget) {
            return Stream.of(values())
                    .filter(variant -> variant.matches(widget))
                    .sorted(Comparator.comparingInt(Variant::rank))
                    .toList();
        }
    }
}
