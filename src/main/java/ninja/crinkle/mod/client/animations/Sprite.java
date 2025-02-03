package ninja.crinkle.mod.client.animations;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import ninja.crinkle.mod.CrinkleMod;
import ninja.crinkle.mod.client.textures.SpriteLoaderType;
import ninja.crinkle.mod.client.textures.Textures;

public class Sprite {
    private final int x;
    private final int y;
    private final int width;
    private final int height;
    private final ResourceLocation sprite;
    private final float frameTicks;

    public Sprite(int x, int y, int width, int height, ResourceLocation sprite, float frameTicks) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.sprite = sprite;
        this.frameTicks = frameTicks;
    }

    public static Builder builder() {
        return new Builder();
    }

    public float frameTicks() {
        return frameTicks;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public ResourceLocation getSprite() {
        return sprite;
    }

    public void render(GuiGraphics guiGraphics, int xOffset, int yOffset, int blitOffset) {
        TextureAtlasSprite sprite = Textures.getInstance().getSpriteLoader(SpriteLoaderType.SPRITE).getSprite(getSprite());
        guiGraphics.blit(getX() + xOffset, getY() + yOffset, blitOffset, getWidth(), getHeight(), sprite);
    }

    public static class Builder {
        private int x = 0;
        private int y = 0;
        private int width = 0;
        private int height = 0;
        private int frameTicks = 20;
        private ResourceLocation sprite = null;

        public int frameTicks() {
            return frameTicks;
        }

        public Builder frameTicks(int frameTicks) {
            this.frameTicks = frameTicks;
            return this;
        }

        public Builder x(int x) {
            this.x = x;
            return this;
        }

        public Builder y(int y) {
            this.y = y;
            return this;
        }

        public Builder width(int width) {
            this.width = width;
            return this;
        }

        public Builder height(int height) {
            this.height = height;
            return this;
        }

        public Builder size(int width, int height) {
            this.width = width;
            this.height = height;
            return this;
        }

        public Builder size(int size) {
            return size(size, size);
        }

        /***
         * Set the position of the sprite position to the top left corner of the parent frame
         * @param x The x position
         * @param y The y position
         * @return The {@link Builder} instance
         */
        public Builder position(int x, int y) {
            this.x = x;
            this.y = y;
            return this;
        }

        public Builder bounds(int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            return this;
        }


        public Builder resource(ResourceLocation sprite) {
            this.sprite = sprite;
            return this;
        }

        public Builder resource(String sprite) {
            return resource(new ResourceLocation(CrinkleMod.MODID, sprite));
        }

        public Sprite build() {
            return new Sprite(x, y, width, height, sprite, frameTicks);
        }
    }
}
