package ninja.crinkle.mod.client.textures;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import ninja.crinkle.mod.CrinkleMod;
import ninja.crinkle.mod.client.textures.generators.DesignTextureGenerator;
import ninja.crinkle.mod.client.textures.generators.TextureData;
import ninja.crinkle.mod.client.textures.generators.TextureGenerator;
import ninja.crinkle.mod.undergarment.Design;
import ninja.crinkle.mod.undergarment.DesignRegistry;
import org.jetbrains.annotations.NotNull;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public class Textures {
    private static final Map<ResourceLocation, TextureGenerator<?>> textureGenerators = new HashMap<>();
    private static final Map<ResourceLocation, NativeImage> originalImageCache = new HashMap<>();
    private static Textures INSTANCE;
    private final Map<String, ResourceLocation> dynamicTextures = new HashMap<>();
    private final Map<SpriteLoaderType, CrinkleSpriteLoader> loaders = new EnumMap<>(SpriteLoaderType.class);

    Textures() {
    }

    public static Textures getInstance() {
        if (INSTANCE == null) {
            Minecraft minecraft = Minecraft.getInstance();
            TextureManager manager = minecraft.getTextureManager();
            INSTANCE = new Textures();
            for (SpriteLoaderType type : SpriteLoaderType.values()) {
                INSTANCE.loaders.put(type, new CrinkleSpriteLoader(manager, type));
            }
        }
        return INSTANCE;
    }

    public static void rebuildGenerators() {
        textureGenerators.clear();
        originalImageCache.clear();

        for (Design design : DesignRegistry.getDesigns().values()) {
            TextureGenerator<?> generator = DesignTextureGenerator.buildGenerator(design);
            if (generator != null) {
                textureGenerators.put(design.armorTexture(), generator);
            }
        }

        if (INSTANCE != null) {
            INSTANCE.dynamicTextures.clear();
        }
    }


    public CrinkleSpriteLoader getSpriteLoader(SpriteLoaderType pType) {
        return loaders.get(pType);
    }

    @SuppressWarnings("resource")
    public ResourceLocation getTexture(ResourceLocation pOriginal, @NotNull TextureData pData) {
        String key = pData.getName();

        ResourceLocation cached = dynamicTextures.get(key);
        if (cached != null) {
            return cached;
        }

        TextureGenerator<?> generator = textureGenerators.get(pOriginal);
        if (generator == null) {
            return pOriginal;
        }

        // Get or cache the original sprite image for this texture path
        NativeImage originalImage = originalImageCache.get(pOriginal);
        if (originalImage == null) {
            SpriteLoaderType type = SpriteLoaderType.fromResourceLocation(pOriginal);
            CrinkleSpriteLoader spriteLoader = loaders.get(type);
            TextureAtlasSprite sprite = spriteLoader.getSprite(pOriginal);
            originalImage = new NativeImage(sprite.contents().width(), sprite.contents().height(), true);
            originalImage.copyFrom(sprite.contents().getOriginalImage());
            originalImageCache.put(pOriginal, originalImage);
        }
        // Make a working copy for the generator to modify
        NativeImage image = new NativeImage(originalImage.getWidth(), originalImage.getHeight(), true);
        image.copyFrom(originalImage);
        // Set the cached original for fade color matching across chained generators
        if (pData instanceof DesignTextureGenerator.Data diaperData) {
            diaperData.setOriginalImage(originalImage);
        }
        return registerTexture(key, generator.apply(image, pData));
    }

    public ResourceLocation registerTexture(String name, NativeImage image) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(CrinkleMod.MODID, name);
        Minecraft.getInstance().getTextureManager().register(id, new DynamicTexture(image));
        dynamicTextures.put(name, id);
        return id;
    }


}