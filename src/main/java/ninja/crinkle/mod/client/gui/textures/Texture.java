package ninja.crinkle.mod.client.gui.textures;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.logging.LogUtils;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import ninja.crinkle.mod.client.color.Color;
import ninja.crinkle.mod.client.gui.builders.GenericBuilder;
import ninja.crinkle.mod.client.gui.properties.*;
import ninja.crinkle.mod.client.gui.renderers.ThemeGraphics;
import ninja.crinkle.mod.client.gui.themes.StyleVariant;
import ninja.crinkle.mod.client.gui.themes.Theme;
import ninja.crinkle.mod.client.gui.themes.Style;
import ninja.crinkle.mod.client.gui.widgets.AbstractWidget;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record Texture(String id, String location, Map<Slice.Location, Slice> slices, Theme theme) {
    public static final Texture EMPTY = new Texture("empty", "missingno", Slice.Location.emptySliceMap(), ninja.crinkle.mod.client.gui.themes.Theme.EMPTY);
    private static final Logger LOGGER = LogUtils.getLogger();

    public TextureSize boundsOf(Slice.Location location) {
        if (slices().containsKey(location)) {
            return slices().get(location).size();
        }
        return TextureSize.ZERO;
    }

    public ResourceLocation resourceLocation() {
        return ThemeAtlas.getTextureLocation(theme.getId(), id())
                .orElse(new ResourceLocation("minecraft", "missingno"));
    }

    private boolean isOutOfBounds(TextureBox a, Slice b, String key) {
        if (!a.contains(b.box().bottomRight())) {
            LOGGER.warn("Texture '{}' could not be created since the slice '{}' is out of bounds. a bounds: {}, b bounds: {}", key, b, a, b.box());
            return true;
        }
        return false;
    }

    @SuppressWarnings("resource")
    public @NotNull ResourceLocation generate(String key, @NotNull TextureSize size, @NotNull Theme theme, List<ColorFilters> colorFilters) {
        if (!isLoadedFor(theme)) {
            LOGGER.warn("Texture '{}' could not be created since '{}' is not loaded.", key, ThemeAtlas.getTextureLocation(theme.getId(), id()));
            return new ResourceLocation("minecraft", "missingno");
        }
        TextureAtlasSprite sprite = ThemeAtlas.getSprite(resourceLocation());
        final NativeImage original = new NativeImage(sprite.contents().width(), sprite.contents().height(), true);
        TextureBox originalBox = new TextureBox(TextureSize.of(original.getWidth(), original.getHeight()));
        original.copyFrom(sprite.contents().getOriginalImage());
        NativeImage image = new NativeImage(size.width(), size.height(), true);
        colorFilters.forEach(colorFilter -> original.applyToAllPixels(colorFilter.filter()));

        for (var entry : slices.entrySet()) {
            Slice.Location location = entry.getKey();
            Slice slice = entry.getValue();
            final int remainingWidth = size.width() - slice.size().width();
            final int remainingHeight = size.height() - slice.size().height();
            switch (location) {
                case topLeft, topRight, bottomLeft, bottomRight -> {
                    // The x,y coords of the original image
                    final Point from = slice.start();
                    // The x,y coords of the destination image
                    final Point to = switch (location) {
                        case topLeft -> Point.ZERO;
                        case topRight -> new ImmutablePoint(remainingWidth, 0);
                        case bottomLeft -> new ImmutablePoint(0, remainingHeight);
                        case bottomRight -> new ImmutablePoint(remainingWidth, remainingHeight);
                        default -> throw new IllegalStateException("Unexpected value: " + location);
                    };
                    if (isOutOfBounds(originalBox, slice, key)) {
                        return new ResourceLocation("minecraft", "missingno");
                    }
                    original.copyRect(image, (int) from.x(), (int) from.y(), (int) to.x(), (int) to.y(), slice.size().width(),
                            slice.size().height(), false, false);
                }
                case top, bottom -> {
                    Slice leftCorner = slices.get(location == Slice.Location.top ?
                            Slice.Location.topLeft : Slice.Location.bottomLeft);
                    Slice rightCorner = slices.get(location == Slice.Location.top ?
                            Slice.Location.topRight : Slice.Location.bottomRight);
                    double startX = leftCorner.start().add(leftCorner.size()).x();
                    double endX = size.subtract(rightCorner.size()).width();
                    for (double x = startX; x < endX; x += slice.size().width()) {
                        // The x,y coords of the original image
                        final Point from = slice.start();
                        // The x,y coords of the destination image
                        final Point to = switch (location) {
                            case top -> new ImmutablePoint(x, 0);
                            case bottom -> new ImmutablePoint(x, remainingHeight);
                            default -> throw new IllegalStateException("Unexpected value: " + location);
                        };
                        if (isOutOfBounds(originalBox, slice, key)) {
                            return new ResourceLocation("minecraft", "missingno");
                        }
                        original.copyRect(image, (int) from.x(), (int) from.y(), (int) to.x(), (int) to.y(), slice.size().width(),
                                slice.size().height(), false, false);
                    }
                }
                case left, right -> {
                    Slice topCorner = slices.get(location == Slice.Location.left ?
                            Slice.Location.topLeft : Slice.Location.topRight);
                    Slice bottomCorner = slices.get(location == Slice.Location.left ?
                            Slice.Location.bottomLeft : Slice.Location.bottomRight);
                    double startY = topCorner.start().add(topCorner.size()).y();
                    double endY = size.subtract(bottomCorner.size()).height();
                    for (double y = startY; y < endY; y += slice.size().height()) {
                        // The x,y coords of the original image
                        final Point from = slice.start();
                        // The x,y coords of the destination image
                        final Point to = switch (location) {
                            case left -> new ImmutablePoint(0, y);
                            case right -> new ImmutablePoint(remainingWidth, y);
                            default -> throw new IllegalStateException("Unexpected value: " + location);
                        };
                        if (isOutOfBounds(originalBox, slice, key)) {
                            return new ResourceLocation("minecraft", "missingno");
                        }
                        original.copyRect(image, (int) from.x(), (int) from.y(), (int) to.x(), (int) to.y(), slice.size().width(),
                                slice.size().height(), false, false);
                    }
                }
                case center -> {
                    Slice top = slices.get(Slice.Location.top);
                    Slice left = slices.get(Slice.Location.left);
                    Slice bottom = slices.get(Slice.Location.bottom);
                    Slice right = slices.get(Slice.Location.right);
                    double startX = left.start().add(left.size()).x();
                    double startY = top.start().add(top.size()).y();
                    for (double x = startX; x < size.subtract(right.size()).width(); x += slice.size().width()) {
                        for (double y = startY; y < size.subtract(bottom.size()).height(); y += slice.size().height()) {
                            // The x,y coords of the original image
                            final Point from = slice.start();
                            // The x,y coords of the destination image
                            final Point to = new ImmutablePoint(x, y);
                            if (isOutOfBounds(originalBox, slice, key)) {
                                return new ResourceLocation("minecraft", "missingno");
                            }
                            original.copyRect(image, (int) from.x(), (int) from.y(), (int) to.x(), (int) to.y(), slice.size().width(),
                                    slice.size().height(), false, false);
                        }
                    }
                }
            }
        }
        // We can't close the new image, or the sprite contents or Minecraft will lose reference to them
        original.close();
        for (var filter : colorFilters) {
            image.applyToAllPixels(filter.filter());
        }
        return ThemeAtlas.register(key, new DynamicTexture(image));
    }

    public boolean isLoadedFor(Theme theme) {
        return ThemeAtlas.hasTexture(theme.getId(), id());
    }

    public void render(@NotNull ThemeGraphics graphics, @NotNull AbstractWidget widget, List<ColorFilters> colorFilters) {
        if (this == EMPTY || !widget.visible() || widget.alpha() == 0.0f) {
            return;
        }
        StyleVariant styleVariant = widget.appearance();
        if (styleVariant == null) {
            styleVariant = widget.widgetTheme().getAppearance(Style.Variant.active);
        }
        Color color = styleVariant.getBackgroundColor() == null ? Color.RAINBOW : styleVariant.getBackgroundColor();
        for (var filter : styleVariant.backgroundColorFilters()) {
            color = Color.of(filter.filter().applyAsInt(color.color()));
        }
        TextureBox background = TextureBox.from(widget.cachedBoxes().backgroundBox());
        if (background.size().equals(TextureSize.ZERO)) {
            return;
        }
        ResourceLocation texture = theme.generateTexture(this, background.size(), colorFilters);
        graphics.blit(texture, background.topLeft(), background.size(), widget.zIndex(), color.withAlpha(widget.alpha()));
    }

    public static class Builder extends GenericBuilder<Builder, Texture> {
        private final Map<Slice.Location, Slice> slices = new HashMap<>(Slice.Location.emptySliceMap());
        private final String id;
        private final Theme theme;
        private String location = "missingno";

        public Builder(String id, Theme theme) {
            this.id = id;
            this.theme = theme;
        }

        public Builder addSlice(Slice.Location location, Slice slice) {
            slices.put(location, slice);
            return self();
        }

        @SuppressWarnings("UnusedReturnValue")
        public Builder addSlice(Slice.Location location, Point start, TextureSize size) {
            return addSlice(location, new Slice(Point.of(start.x(), start.y()), size));
        }

        public Builder location(String location) {
            this.location = location;
            return self();
        }

        @Override
        public Texture build() {
            return new Texture(id, location, slices, theme);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }

    public record Slice(ImmutablePoint start, TextureSize size) {
        public Slice(int x, int y, int width, int height) {
            this(new ImmutablePoint(x, y), TextureSize.of(width, height));
        }

        public TextureBox box() {
            return new TextureBox(size);
        }

        public enum Location {
            topLeft,
            top,
            topRight,
            left,
            center,
            right,
            bottomLeft,
            bottom,
            bottomRight;

            public static Map<Location, Slice> emptySliceMap() {
                return Map.of(
                        topLeft, new Slice(ImmutablePoint.ZERO, TextureSize.ZERO),
                        top, new Slice(ImmutablePoint.ZERO, TextureSize.ZERO),
                        topRight, new Slice(ImmutablePoint.ZERO, TextureSize.ZERO),
                        left, new Slice(ImmutablePoint.ZERO, TextureSize.ZERO),
                        center, new Slice(ImmutablePoint.ZERO, TextureSize.ZERO),
                        right, new Slice(ImmutablePoint.ZERO, TextureSize.ZERO),
                        bottomLeft, new Slice(ImmutablePoint.ZERO, TextureSize.ZERO),
                        bottom, new Slice(ImmutablePoint.ZERO, TextureSize.ZERO),
                        bottomRight, new Slice(ImmutablePoint.ZERO, TextureSize.ZERO)
                );
            }
        }
    }
}
