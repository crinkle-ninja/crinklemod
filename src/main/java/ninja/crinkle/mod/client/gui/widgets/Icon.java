package ninja.crinkle.mod.client.gui.widgets;

import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import ninja.crinkle.mod.client.color.Color;
import ninja.crinkle.mod.client.gui.properties.Point;
import ninja.crinkle.mod.client.gui.properties.Rect;
import ninja.crinkle.mod.client.gui.renderers.ThemeGraphics;
import ninja.crinkle.mod.client.gui.textures.TextureSize;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class Icon extends AbstractWidget {
    private @Nullable TextureAtlas atlas;
    private boolean dropShadow;
    private ResourceLocation texture;
    private TextureSize textureSize;

    protected Icon(@NotNull AbstractIconBuilder<?> builder) {
        super(builder);
        this.atlas = builder.atlas();
        this.texture = builder.texture();
        this.textureSize = builder.textureSize();
        this.dropShadow = builder.dropShadow();
    }

    public @Nullable ResourceLocation texture() {
        return texture;
    }

    public void texture(ResourceLocation texture) {
        this.texture = texture;
    }

    public TextureSize textureSize() {
        return textureSize;
    }

    public void textureSize(TextureSize textureSize) {
        this.textureSize = textureSize;
    }

    public boolean dropShadow() {
        return dropShadow;
    }

    public void dropShadow(boolean dropShadow) {
        this.dropShadow = dropShadow;
    }

    @Override
    public int minimumWidth() {
        int explicit = super.minimumWidth();
        if (explicit > 0 || textureSize == null) return explicit;
        return textureSize.width();
    }

    @Override
    public int minimumHeight() {
        int explicit = super.minimumHeight();
        if (explicit > 0 || textureSize == null) return explicit;
        return textureSize.height();
    }

    @Override
    public void renderContent(ThemeGraphics graphics, Point pMouse, Rect renderedRect, float pPartialTick) {
        if (texture == null || textureSize == null) return;
        Color color = Optional.ofNullable(appearance().foregroundColor()).orElse(Color.RAINBOW);
        int xOffset = (renderedRect.width() - textureSize.width()) / 2;
        int yOffset = (renderedRect.height() - textureSize.height()) / 2;
        int x = renderedRect.x() + xOffset;
        int y = renderedRect.y() + yOffset;

        if (atlas != null) {
            TextureAtlasSprite sprite = atlas.getSprite(texture);
            if (dropShadow) {
                graphics.blit(sprite, Point.of(x + 1, y + 1), textureSize, zIndex(), Color.BLACK);
            }
            graphics.blit(sprite, Point.of(x, y), textureSize, zIndex() + (dropShadow ? 1 : 0), color);
        } else {
            if (dropShadow) {
                graphics.blit(texture, Point.of(x + 1, y + 1), textureSize, zIndex(), Color.BLACK);
            }
            graphics.blit(texture, Point.of(x, y), textureSize, zIndex() + (dropShadow ? 1 : 0), color);
        }
    }

    @Override
    public AbstractWidget visualCopy(AbstractContainer newParent) {
        Icon copy = new Icon.Builder(newParent)
                .texture(texture).textureSize(textureSize).dropShadow(dropShadow).build();
        copy.copyVisualProperties(this);
        return copy;
    }

    public abstract static class AbstractIconBuilder<T extends AbstractIconBuilder<T>> extends AbstractBuilder<T> {
        private @Nullable TextureAtlas atlas;
        private boolean dropShadow;
        private ResourceLocation texture;
        private TextureSize textureSize = TextureSize.of(16, 16);

        public AbstractIconBuilder(AbstractContainer parent) {
            super(parent);
            style("icon");
            active(true);
        }

        public T atlas(@Nullable TextureAtlas atlas) {
            this.atlas = atlas;
            return self();
        }

        public @Nullable TextureAtlas atlas() {
            return atlas;
        }

        public T dropShadow(boolean dropShadow) {
            this.dropShadow = dropShadow;
            return self();
        }

        public boolean dropShadow() {
            return dropShadow;
        }

        public T texture(ResourceLocation texture) {
            this.texture = texture;
            return self();
        }

        public ResourceLocation texture() {
            return texture;
        }

        public T textureSize(TextureSize textureSize) {
            this.textureSize = textureSize;
            return self();
        }

        public TextureSize textureSize() {
            return textureSize;
        }
    }

    public static class Builder extends AbstractIconBuilder<Builder> {

        public Builder(AbstractContainer parent) {
            super(parent);
        }

        @Override
        public Icon build() {
            return new Icon(this);
        }

        @Override
        public AbstractContainer push() {
            return parent().add(this);
        }

        @Override
        protected Builder self() {
            return this;
        }

        public Icon pushAndReturn() {
            Icon icon = new Icon(this);
            parent().add(icon);
            return icon;
        }
    }
}