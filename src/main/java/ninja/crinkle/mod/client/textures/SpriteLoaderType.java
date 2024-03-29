package ninja.crinkle.mod.client.textures;

import net.minecraft.resources.ResourceLocation;
import ninja.crinkle.mod.CrinkleMod;

public enum SpriteLoaderType {
    GUI("textures/atlas/gui.png", "gui"),
    ARMOR("textures/atlas/armor.png", "armor");

    private final ResourceLocation atlasLocation;
    private final ResourceLocation atlasInfoLocation;

    SpriteLoaderType(String atlasLocation, String atlasInfoLocation) {
        this.atlasLocation = new ResourceLocation(CrinkleMod.MODID, atlasLocation);
        this.atlasInfoLocation = new ResourceLocation(CrinkleMod.MODID, atlasInfoLocation);
    }

    public static SpriteLoaderType fromString(String string) {
        return switch (string) {
            case "gui" -> GUI;
            case "armor" -> ARMOR;
            default -> null;
        };
    }

    public static SpriteLoaderType fromResourceLocation(ResourceLocation resourceLocation) {
        return fromString(resourceLocation.getPath().split("/")[0]);
    }

    public ResourceLocation getAtlasLocation() {
        return atlasLocation;
    }

    public ResourceLocation getAtlasInfoLocation() {
        return atlasInfoLocation;
    }
}
