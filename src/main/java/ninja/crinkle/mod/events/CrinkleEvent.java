package ninja.crinkle.mod.events;

import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.Event;
import ninja.crinkle.mod.metabolism.Metabolism;
import ninja.crinkle.mod.util.ClientUtil;

public class CrinkleEvent extends Event {
    private final Player player;
    private final Side side;
    private final Metabolism.Type type;

    public static Side side() {
        return ClientUtil.isClient() ? Side.CLIENT : Side.SERVER;
    }

    public CrinkleEvent(Side side, Player player, Metabolism.Type type) {
        this.side = side;
        this.player = player;
        this.type = type;
    }

    public Player player() {
        return player;
    }

    public Side getSide() {
        return side;
    }

    public Metabolism.Type type() {
        return type;
    }

    public enum Side {
        CLIENT,
        SERVER
    }
}
