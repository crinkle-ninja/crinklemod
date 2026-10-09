package ninja.crinkle.mod.client.textures.generators;

import com.mojang.blaze3d.platform.NativeImage;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@FunctionalInterface
public interface TextureGenerator<T> {
    default @NotNull TextureGenerator<T> andThen(@NotNull TextureGenerator<T> pOther) {
        Objects.requireNonNull(pOther);
        return (pImage, pData) -> pOther.apply(apply(pImage, pData), pData);
    }

    @NotNull NativeImage apply(@NotNull NativeImage pImage, @NotNull TextureData pData);
}
