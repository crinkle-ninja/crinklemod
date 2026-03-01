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
import java.util.function.IntUnaryOperator;

public class StyleVariant {
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final StyleVariant EMPTY = StyleVariant.builder().build();
    private final Color backgroundColor;
    private final List<ColorFilters> backgroundColorFilters = new ArrayList<>();
    private final Texture backgroundTexture;
    private final Color foregroundColor;
    private final List<ColorFilters> foregroundColorFilters = new ArrayList<>();
    private final Texture foregroundTexture;
    private final boolean shadow;
    private final StyleVariant parent;

    public StyleVariant(Texture backgroundTexture, Texture foregroundTexture, Color backgroundColor,
                        Color foregroundColor, boolean shadow, List<ColorFilters> foregroundColorFilters,
                        List<ColorFilters> backgroundColorFilters, StyleVariant parent) {
        this.backgroundTexture = backgroundTexture;
        this.foregroundTexture = foregroundTexture;
        this.backgroundColor = backgroundColor;
        this.foregroundColor = foregroundColor;
        this.shadow = shadow;
        this.parent = parent;
        this.foregroundColorFilters.addAll(foregroundColorFilters);
        this.backgroundColorFilters.addAll(backgroundColorFilters);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static StyleVariant getDefault(String widgetTheme) {
        return Optional.of(ThemeRegistry.defaultTheme().widgetTheme(widgetTheme)).map(wt -> wt.appearance(Style.Variant.active)).orElse(StyleVariant.EMPTY);
    }

    public List<ColorFilters> backgroundColorFilters() {
        return backgroundColorFilters;
    }

    public Font font() {
        return ClientUtil.getMinecraft().font;
    }

    public List<ColorFilters> foregroundColorFilters() {
        return foregroundColorFilters;
    }

    public @Nullable Color backgroundColor() {
        return backgroundColor;
    }

    public Texture backgroundTexture() {
        return backgroundTexture;
    }

    public List<IntUnaryOperator> colorFilters() {
        return List.of();
    }

    public Color foregroundColor() {
        return foregroundColor;
    }

    public Texture foregroundTexture() {
        return foregroundTexture;
    }

    public boolean hasShadow() {
        return shadow;
    }

    public StyleVariant parent() {
        return parent;
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

    public StyleVariant coalesceWith(StyleVariant other) {
        if (other == null) {
            return this;
        }
        StyleVariant b = other.parent() != null && other.parent() != this ? other.parent().coalesceWith(other) : other;
        StyleVariant a = parent() != null && parent() != other ? parent().coalesceWith(this) : this;

        return new StyleVariant(
                Optional.ofNullable(b.backgroundTexture()).orElse(a.backgroundTexture()),
                Optional.ofNullable(b.foregroundTexture()).orElse(a.foregroundTexture()),
                Optional.ofNullable(b.backgroundColor()).orElse(a.backgroundColor()),
                Optional.ofNullable(b.foregroundColor()).orElse(a.foregroundColor()),
                b.hasShadow() || a.shadow,
                b.foregroundColorFilters.isEmpty() ? a.foregroundColorFilters : b.foregroundColorFilters,
                b.backgroundColorFilters.isEmpty() ? a.backgroundColorFilters : b.backgroundColorFilters,
                Optional.ofNullable(b.parent()).orElse(a.parent())
        );
    }

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

    @Override
    public String toString() {
        return "StyleVariant{"
                + "backgroundColor=" + backgroundColor
                + ", backgroundTexture=" + backgroundTexture
                + ", foregroundColor=" + foregroundColor
                + ", foregroundTexture=" + foregroundTexture
                + ", shadow=" + shadow
                + ", foregroundColorFilters=" + String.join(",", foregroundColorFilters.stream().map(ColorFilters::toString).toList())
                + ", backgroundColorFilters=" + String.join(",", backgroundColorFilters.stream().map(ColorFilters::toString).toList())
                + '}';
    }

    public static class Builder extends GenericBuilder<Builder, StyleVariant> {
        private final List<ColorFilters> foregroundColorFilters = new ArrayList<>();
        private final List<ColorFilters> backgroundColorFilters = new ArrayList<>();
        private Color backgroundColor;
        private Texture backgroundTexture;
        private Color foregroundColor;
        private Texture foregroundTexture;
        private boolean shadow;
        private StyleVariant parent;

        public Builder addForegroundColorFilter(ColorFilters filter) {
            foregroundColorFilters.add(filter);
            return self();
        }

        public Builder addForegroundColorFilters(List<ColorFilters> filters) {
            foregroundColorFilters.addAll(filters);
            return self();
        }

        public Builder addBackgroundColorFilter(ColorFilters filter) {
            backgroundColorFilters.add(filter);
            return self();
        }

        public Builder addBackgroundColorFilters(List<ColorFilters> filters) {
            backgroundColorFilters.addAll(filters);
            return self();
        }

        public Builder backgroundColor(Color backgroundColor) {
            this.backgroundColor = backgroundColor;
            return self();
        }

        public Builder backgroundTexture(Texture backgroundTexture) {
            this.backgroundTexture = backgroundTexture;
            return self();
        }

        public StyleVariant build() {
            return new StyleVariant(backgroundTexture, foregroundTexture, backgroundColor, foregroundColor,
                    shadow, foregroundColorFilters, backgroundColorFilters, parent);
        }

        public StyleVariant parent() {
            return parent;
        }

        public Builder parent(StyleVariant parent) {
            this.parent = parent;
            return self();
        }

        @Override
        protected Builder self() {
            return this;
        }

        public Builder foregroundColor(Color foregroundColor) {
            this.foregroundColor = foregroundColor;
            return self();
        }

        public Builder foregroundTexture(Texture foregroundTexture) {
            this.foregroundTexture = foregroundTexture;
            return self();
        }

        public Color backgroundColor() {
            return backgroundColor;
        }

        public Texture backgroundTexture() {
            return backgroundTexture;
        }

        public Color foregroundColor() {
            return foregroundColor;
        }

        public Texture foregroundTexture() {
            return foregroundTexture;
        }

        public boolean isShadow() {
            return shadow;
        }

        public Builder shadow(boolean shadow) {
            this.shadow = shadow;
            return self();
        }
    }
}
