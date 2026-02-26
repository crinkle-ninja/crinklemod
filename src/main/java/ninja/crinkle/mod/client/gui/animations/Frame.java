package ninja.crinkle.mod.client.gui.animations;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import ninja.crinkle.mod.client.gui.textures.ThemeAtlas;
import ninja.crinkle.mod.client.gui.themes.ThemeRegistry;
import org.slf4j.Logger;

import java.util.Optional;

public record Frame(String namespace, String animationId, String frameId) {
    private static final Logger LOGGER = LogUtils.getLogger();
    public ResourceLocation resourceLocation(int size) {
        String textureId = animationId() + "/" + size + "/" + frameId();
        Optional<ResourceLocation> resourceLocation =
                ThemeAtlas.getTextureLocation(ThemeRegistry.current().getId(), "animations", textureId);
        if (resourceLocation.isEmpty()) {
            LOGGER.error("Failed to find textureId '{}' in theme '{}'", textureId, ThemeRegistry.current().getId());
            return new ResourceLocation(namespace, "missingno");
        }
        return resourceLocation.get();
    }
}
