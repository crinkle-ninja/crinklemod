package ninja.crinkle.mod.client.gui.themes.loader;

import ninja.crinkle.mod.CrinkleMod;
import ninja.crinkle.mod.client.gui.textures.Texture;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.client.Minecraft;
import ninja.crinkle.mod.client.gui.themes.Theme;

import java.nio.file.FileSystems;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public record TextureData(String id, String location, Map<Texture.Slice.Location, Texture.Slice> slices) {

    public List<String> locations(String themeId) {
        if (location == null || location.isEmpty()) {
            throw new IllegalArgumentException("Texture location is missing or empty for texture '" + id + "' in theme '" + themeId + "'.");
        }
        final List<String> locations = new ArrayList<>();
        PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + location);
        ResourceManager resourceManager = Minecraft.getInstance().getResourceManager();
        String namespace = CrinkleMod.MODID;
        String basePath = "textures/theme/" + themeId;

        // List all resources in the namespace
        resourceManager.listResources(basePath, resourceLocation -> {
            if (resourceLocation.getNamespace().equals(namespace)) {
                String path = resourceLocation.getPath();
                // Remove the base path prefix
                if (path.startsWith(basePath)) {
                    String relativePath = path.substring(basePath.length());
                    // Check if it matches the glob pattern
                    if (matcher.matches(java.nio.file.Path.of(relativePath))) {
                        locations.add(relativePath);
                        return true;
                    }
                }
            }
            return false;
        });

        return locations;
    }
}