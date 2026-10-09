package ninja.crinkle.mod.client.gui.themes;

import com.mojang.logging.LogUtils;
import net.minecraft.client.gui.Font;
import ninja.crinkle.mod.client.color.Color;
import ninja.crinkle.mod.client.gui.builders.GenericBuilder;
import ninja.crinkle.mod.client.gui.properties.Rect;
import ninja.crinkle.mod.client.gui.renderers.ThemeGraphics;
import ninja.crinkle.mod.client.gui.textures.ColorFilters;
import ninja.crinkle.mod.client.gui.textures.Texture;
import ninja.crinkle.mod.client.gui.widgets.AbstractWidget;
import ninja.crinkle.mod.util.ClientUtil;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class StyleVariant {
    public static final StyleVariant EMPTY = StyleVariant.builder().build();
    public static final Logger LOGGER = LogUtils.getLogger();
    private final Color backgroundColor;
    private final List<ColorFilters> backgroundColorFilters = new ArrayList<>();
    private final Texture backgroundTexture;
    private final Color foregroundColor;
    private final List<ColorFilters> foregroundColorFilters = new ArrayList<>();
    private final Texture foregroundTexture;
    private final Color shadow;

    public StyleVariant(Texture backgroundTexture, Texture foregroundTexture, Color backgroundColor,
                        Color foregroundColor, Color shadow, List<ColorFilters> foregroundColorFilters,
                        List<ColorFilters> backgroundColorFilters) {
        this.backgroundTexture = backgroundTexture;
        this.foregroundTexture = foregroundTexture;
        this.backgroundColor = backgroundColor;
        this.foregroundColor = foregroundColor;
        this.shadow = shadow;
        this.foregroundColorFilters.addAll(foregroundColorFilters);
        this.backgroundColorFilters.addAll(backgroundColorFilters);
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Null-safe entry point for coalesceWith.
     */
    public static StyleVariant coalesce(StyleVariant a, StyleVariant b) {
        if (a == null) {
            return b;
        }
        if (b == null) {
            return a;
        }
        if (a == b) {
            return a;
        }
        return a.coalesceWith(b);
    }

    /**
     * Merge variant states: prefers this values, fills gaps from other (e.g. hover over base).
     */
    public StyleVariant coalesceWith(StyleVariant other) {
        if (other == null) {
            return this;
        }
        return new StyleVariant(
                Optional.ofNullable(backgroundTexture()).orElse(other.backgroundTexture()),
                Optional.ofNullable(foregroundTexture()).orElse(other.foregroundTexture()),
                Optional.ofNullable(backgroundColor()).orElse(other.backgroundColor()),
                Optional.ofNullable(foregroundColor()).orElse(other.foregroundColor()),
                Optional.ofNullable(shadow).orElse(other.shadow),
                Optional.ofNullable(foregroundColorFilters()).orElse(other.foregroundColorFilters()),
                Optional.ofNullable(backgroundColorFilters()).orElse(other.backgroundColorFilters())
        );
    }

    public static StyleVariant getDefault(String styleId) {
        return Optional.ofNullable(ThemeRegistry.defaultTheme().style(styleId)).map(s -> s.variant(Style.Variant.active)).orElse(StyleVariant.EMPTY);
    }

    public Font font() {
        return ClientUtil.getMinecraft().font;
    }

    public boolean hasShadow() {
        return shadow != null;
    }

    /**
     * Inherit foreground-only properties from a parent widget's appearance. Skips background.
     */
    public StyleVariant inheritFrom(StyleVariant appearance) {
        if (appearance == null) return this;
        return new StyleVariant(
                backgroundTexture(),
                Optional.ofNullable(foregroundTexture()).orElse(appearance.foregroundTexture()),
                backgroundColor(),
                Optional.ofNullable(foregroundColor()).orElse(appearance.foregroundColor()),
                Optional.ofNullable(shadow).orElse(appearance.shadow),
                Optional.ofNullable(foregroundColorFilters()).orElse(appearance.foregroundColorFilters()),
                backgroundColorFilters()
        );
    }

    public Texture backgroundTexture() {
        return backgroundTexture;
    }

    public Texture foregroundTexture() {
        return foregroundTexture;
    }

    public @Nullable Color backgroundColor() {
        return backgroundColor;
    }

    public Color foregroundColor() {
        return foregroundColor;
    }

    public List<ColorFilters> foregroundColorFilters() {
        return foregroundColorFilters;
    }

    public List<ColorFilters> backgroundColorFilters() {
        return backgroundColorFilters;
    }

    public void render(ThemeGraphics pGuiGraphics, Rect rect, AbstractWidget widget) {
        if (backgroundTexture != null) {
            backgroundTexture.render(pGuiGraphics, widget, backgroundColorFilters);
        } else if (backgroundColor != null) {
            pGuiGraphics.fill(rect, backgroundColor, widget.zIndex());
        }

        if (foregroundTexture != null) {
            foregroundTexture.render(pGuiGraphics, widget, foregroundColorFilters);
        }
    }

    public Color shadow() {
        return shadow;
    }

    @Override
    public String toString() {
        return "StyleVariant{"
                + "backgroundColor=" + backgroundColor
                + ", backgroundTexture=" + backgroundTexture
                + ", foregroundColor=" + foregroundColor
                + ", foregroundTexture=" + foregroundTexture
                + ", shadow=" + shadow
                + ", foregroundColorFilters=" + String.join(",",
                foregroundColorFilters.stream().map(ColorFilters::toString).toList())
                + ", backgroundColorFilters=" + String.join(",",
                backgroundColorFilters.stream().map(ColorFilters::toString).toList())
                + '}';
    }

    public static class Builder extends GenericBuilder<Builder, StyleVariant> {
        private final List<ColorFilters> backgroundColorFilters = new ArrayList<>();
        private final List<ColorFilters> foregroundColorFilters = new ArrayList<>();
        private Color backgroundColor;
        private Texture backgroundTexture;
        private Color foregroundColor;
        private Texture foregroundTexture;
        private Color shadow;

        public Builder addBackgroundColorFilter(ColorFilters filter) {
            backgroundColorFilters.add(filter);
            return self();
        }

        public Builder addBackgroundColorFilters(List<ColorFilters> filters) {
            backgroundColorFilters.addAll(filters);
            return self();
        }

        public Builder addForegroundColorFilter(ColorFilters filter) {
            foregroundColorFilters.add(filter);
            return self();
        }

        public Builder addForegroundColorFilters(List<ColorFilters> filters) {
            foregroundColorFilters.addAll(filters);
            return self();
        }

        public Builder backgroundColor(Color backgroundColor) {
            this.backgroundColor = backgroundColor;
            return self();
        }

        public Color backgroundColor() {
            return backgroundColor;
        }

        public Builder backgroundTexture(Texture backgroundTexture) {
            this.backgroundTexture = backgroundTexture;
            return self();
        }

        public Texture backgroundTexture() {
            return backgroundTexture;
        }

        public StyleVariant build() {
            return new StyleVariant(backgroundTexture, foregroundTexture, backgroundColor, foregroundColor,
                    shadow, foregroundColorFilters, backgroundColorFilters);
        }

        @Override
        protected Builder self() {
            return this;
        }

        public Builder foregroundColor(Color foregroundColor) {
            this.foregroundColor = foregroundColor;
            return self();
        }

        public Color foregroundColor() {
            return foregroundColor;
        }

        public Builder foregroundTexture(Texture foregroundTexture) {
            this.foregroundTexture = foregroundTexture;
            return self();
        }

        public Texture foregroundTexture() {
            return foregroundTexture;
        }

        public Color shadow() {
            return shadow;
        }

        public Builder shadow(Color shadow) {
            this.shadow = shadow;
            return self();
        }
    }
}
