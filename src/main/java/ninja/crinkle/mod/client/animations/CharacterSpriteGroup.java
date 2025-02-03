package ninja.crinkle.mod.client.animations;

import ninja.crinkle.mod.metabolism.Metabolism;

public class CharacterSpriteGroup extends SpriteGroup {
    public static final CharacterSpriteGroup NORMAL = new CharacterSpriteGroup(2, "normal");
    public static final CharacterSpriteGroup DESPERATE = new CharacterSpriteGroup(2, "desperate");
    public static final CharacterSpriteGroup VERY_DESPERATE = new CharacterSpriteGroup(2, "very_desperate");
    public static final CharacterSpriteGroup EXTREMELY_DESPERATE =
            new CharacterSpriteGroup("desperate1", "desperate2", "very_desperate1", "very_desperate2");
    public static final CharacterSpriteGroup ACCIDENT = new CharacterSpriteGroup("accident1", "accident2",
            "accident1", "accident2", "accident1", "accident2");
    public static final CharacterSpriteGroup RELIEF = new CharacterSpriteGroup(2, "relief");

    private static final int OFFSET_X = 0;
    private static final int OFFSET_Y = 17;
    private static final String GROUP_NAME = "character";

    CharacterSpriteGroup(int frameCount, String spriteName) {
        super(frameCount, OFFSET_X, OFFSET_Y, SPRITE_SIZE, GROUP_NAME, spriteName);
    }

    CharacterSpriteGroup(String... spriteNames) {
        super(OFFSET_X, OFFSET_Y, SPRITE_SIZE, GROUP_NAME, spriteNames);
    }

    public static CharacterSpriteGroup forLevel(Metabolism.DesperationLevel level) {
        return switch (level) {
            case MEDIUM_LOW -> DESPERATE;
            case MEDIUM, MEDIUM_HIGH -> VERY_DESPERATE;
            case HIGH -> EXTREMELY_DESPERATE;
            case NONE, LOW -> NORMAL;
        };
    }
}
