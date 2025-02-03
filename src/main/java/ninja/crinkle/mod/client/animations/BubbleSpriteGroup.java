package ninja.crinkle.mod.client.animations;

import ninja.crinkle.mod.events.CrinkleEvent;

public class BubbleSpriteGroup extends SpriteGroup {
    public static final BubbleSpriteGroup NORMAL = new BubbleSpriteGroup(2, "normal");
    public static final BubbleSpriteGroup WET = new BubbleSpriteGroup(2, "wet");
    public static final BubbleSpriteGroup MESSY = new BubbleSpriteGroup(2, "messy");
    public static final BubbleSpriteGroup BOTH = new BubbleSpriteGroup("wet1", "wet2", "messy1", "messy2");

    private static final int OFFSET_X = 1;
    private static final int OFFSET_Y = -3;
    private static final String GROUP_NAME = "bubble";

    BubbleSpriteGroup(int frameCount, String spriteName) {
        super(frameCount, OFFSET_X, OFFSET_Y, SPRITE_SIZE, GROUP_NAME, spriteName);
    }

    BubbleSpriteGroup(String... spriteNames) {
        super(OFFSET_X, OFFSET_Y, SPRITE_SIZE, GROUP_NAME, spriteNames);
    }

    public static BubbleSpriteGroup forType(CrinkleEvent.Type type) {
        return switch (type) {
            case BLADDER -> WET;
            case BOWEL -> MESSY;
            case BOTH -> BOTH;
            default -> NORMAL;
        };
    }
}
