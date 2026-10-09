package ninja.crinkle.mod.client.gui.textures;

import com.mojang.logging.LogUtils;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import ninja.crinkle.mod.util.ClientUtil;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum ThemeAtlas {
    INSTANCE;

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String textureLocationRegex
            = "^theme/(?<themeId>[a-zA-Z0-9-_]+)/(?<textureType>[a-zA-Z0-9-_]+)/(?<textureId>.+)$";
    private static final Pattern textureLocationPattern = Pattern.compile(textureLocationRegex);
    private final Map<String, ResourceLocation> dynamicTextures = new HashMap<>();
    private final TextureManager textureManager = ClientUtil.getMinecraft().getTextureManager();
    private final Map<ResourceLocation, TextureInfo> textures = new HashMap<>();
    private Atlas atlas;

    public static Optional<ResourceLocation> getGeneratedTexture(String key) {
        return Optional.ofNullable(INSTANCE.dynamicTextures.get(key));
    }

    public static ThemeAtlas getInstance() {
        return INSTANCE;
    }

    public static TextureAtlasSprite getSprite(ResourceLocation resourceLocation) {
        return getAtlas().getSprite(resourceLocation);
    }

    public static Atlas getAtlas() {
        if (INSTANCE.atlas == null) {
            INSTANCE.atlas = new Atlas(ClientUtil.getMinecraft().getTextureManager());
        }
        return INSTANCE.atlas;
    }

    public static Optional<ResourceLocation> getTextureLocation(String themeId, String textureId) {
        return INSTANCE.textures.values().stream()
                .filter(info -> info.themeId.equals(themeId))
                .filter(info -> info.qualifiedId().equals(textureId) || info.textureId.equals(textureId))
                .map(TextureInfo::location)
                .findFirst();
    }

    public static Optional<ResourceLocation> getTextureLocation(String themeId, String textureType, String textureId) {
        return INSTANCE.textures.values().stream()
                .filter(info -> info.themeId.equals(themeId))
                .filter(info -> info.textureType.equals(textureType))
                .filter(info -> info.qualifiedId().equals(textureId) || info.textureId.equals(textureId))
                .map(TextureInfo::location)
                .findFirst();
    }

    public static boolean hasTexture(String themeId, String textureId) {
        return INSTANCE.textures.values().stream()
                .filter(info -> info.themeId.equals(themeId))
                .anyMatch(info -> info.qualifiedId().equals(textureId) || info.textureId.equals(textureId));
    }

    public static void register(ResourceLocation resourceLocation) {
        if (!resourceLocation.getPath().startsWith("theme/") || hasTexture(resourceLocation)) {
            return;
        }
        Matcher matcher = textureLocationPattern.matcher(resourceLocation.getPath());
        if (!matcher.matches()) {
            LOGGER.warn("Invalid texture location: {}", resourceLocation);
            return;
        }
        String themeId = matcher.group("themeId");
        String textureType = matcher.group("textureType");
        String textureId = matcher.group("textureId");
        TextureInfo info = new TextureInfo(themeId, textureType, textureId, resourceLocation);
        INSTANCE.textures.put(resourceLocation, info);
        LOGGER.debug("Registered texture: {}", info);
    }

    public static boolean hasTexture(ResourceLocation resourceLocation) {
        return INSTANCE.textures.containsKey(resourceLocation);
    }

    public static @NotNull ResourceLocation register(String key, DynamicTexture dynamicTexture) {
        ResourceLocation location = INSTANCE.textureManager.register(key, dynamicTexture);
        INSTANCE.dynamicTextures.put(key, location);
        return location;
    }

    public record TextureInfo(String themeId, String textureType, String textureId, ResourceLocation location) {
        public TextureInfo {
            if (themeId == null) {
                throw new IllegalArgumentException("Theme ID cannot be null.");
            }
            if (textureId == null) {
                throw new IllegalArgumentException("Texture ID cannot be null.");
            }
            if (location == null) {
                throw new IllegalArgumentException("Location cannot be null.");
            }
        }

        public String qualifiedId() {
            return textureType.isEmpty() ? textureId : textureType + "/" + textureId;
        }

        @Override
        public @NotNull String toString() {
            return String.format("TextureInfo{themeId='%s', textureType='%s', textureId='%s', location=%s}",
                    themeId, textureType, textureId, location);
        }
    }
}