package ninja.crinkle.mod.client.textures;

import net.minecraft.resources.ResourceLocation;
import ninja.crinkle.mod.CrinkleMod;

import java.util.Objects;

public enum SpriteLoaderType {
    ARMOR("textures/atlas/armor.png", "armor"),
    ;

    private final ResourceLocation atlasInfoLocation;
    private final ResourceLocation atlasLocation;

    SpriteLoaderType(String atlasLocation, String atlasInfoLocation) {
        this.atlasLocation = new ResourceLocation(CrinkleMod.MODID, atlasLocation);
        this.atlasInfoLocation = new ResourceLocation(CrinkleMod.MODID, atlasInfoLocation);
    }

    public static SpriteLoaderType fromResourceLocation(ResourceLocation resourceLocation) {
        return fromString(resourceLocation.getPath().split("/")[0]);
    }

    public static SpriteLoaderType fromString(String string) {
        return Objects.equals(string, "armor") ? ARMOR : null;
    }

    public ResourceLocation getAtlasInfoLocation() {
        return atlasInfoLocation;
    }

    public ResourceLocation getAtlasLocation() {
        return atlasLocation;
    }
}
