package ninja.crinkle.mod.client.gui.textures;

import org.jetbrains.annotations.NotNull;

public record TextureSize(int width, int height) {
    public static final TextureSize ZERO = new TextureSize(0, 0);

    public static TextureSize of(int width, int height) {
        return new TextureSize(width, height);
    }

    public static TextureSize of(double width, double height) {
        return new TextureSize((int) width, (int) height);
    }

    public TextureSize add(TextureSize size) {
        return add(size.width(), size.height());
    }

    public TextureSize add(int width, int height) {
        return new TextureSize(this.width() + width, this.height() + height);
    }

    public TextureSize subtract(TextureSize size) {
        return subtract(size.width(), size.height());
    }

    public TextureSize subtract(int width, int height) {
        return new TextureSize(this.width() - width, this.height() - height);
    }

    @Override
    public @NotNull String toString() {
        return "Size{" + "width=" + width + ", height=" + height + '}';
    }
}
