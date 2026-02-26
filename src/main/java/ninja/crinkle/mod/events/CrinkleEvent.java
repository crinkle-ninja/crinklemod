package ninja.crinkle.mod.events;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.Event;

public class CrinkleEvent extends Event {
    public enum Side {
        CLIENT,
        SERVER
    }

    public enum Type {
        NONE,
        BLADDER,
        BOWEL,
        SOLIDS,
        LIQUIDS,
        BOTH;

        @Override
        public String toString() {
            return switch (this) {
                case BLADDER -> "bladder";
                case BOWEL -> "bowel";
                case SOLIDS -> "solids";
                case LIQUIDS -> "liquids";
                case BOTH -> "both";
                case NONE -> "none";
                default -> throw new IllegalStateException("Unexpected value: " + this);
            };
        }

        public Style getStyle() {
            return switch (this) {
                case BLADDER, LIQUIDS -> Style.EMPTY.withColor(ChatFormatting.YELLOW);
                case BOWEL -> Style.EMPTY.withColor(ChatFormatting.DARK_GREEN);
                case SOLIDS, BOTH -> Style.EMPTY.withColor(ChatFormatting.GOLD);
                default -> throw new IllegalStateException("Unexpected value: " + this);
            };
        }
    }

    private final Player player;
    private final Side side;
    private final Type type;

    public CrinkleEvent(Side side, Player player, Type type) {
        this.side = side;
        this.player = player;
        this.type = type;
    }

    public Player getPlayer() {
        return player;
    }

    public Side getSide() {
        return side;
    }

    public Type getType() {
        return type;
    }
}
