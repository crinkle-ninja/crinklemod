package ninja.crinkle.mod.events;

import net.minecraft.world.entity.player.Player;
import ninja.crinkle.mod.metabolism.Metabolism;
import ninja.crinkle.mod.metabolism.Pang;

public class PangEvent extends CrinkleEvent {
    private final Pang pang;
    private final int pangDuration;
    protected PangEvent(Player player, Metabolism.Type type, Pang pang, int pangDuration) {
        super(CrinkleEvent.side(), player, type);
        this.pang = pang;
        this.pangDuration = pangDuration;
    }

    public Pang pang() {
        return pang;
    }

    public int pangDuration() {
        return pangDuration;
    }

    public static class Start extends PangEvent {
        public Start(Player player, Metabolism.Type type, Pang pang, int pangDuration) {
            super(player, type, pang, pangDuration);
        }
    }

    public static class Stop extends PangEvent {
        public Stop(Player player, Metabolism.Type type, Pang pang, int pangDuration) {
            super(player, type, pang, pangDuration);
        }
    }

    public static class Tick extends PangEvent {
        public Tick(Player player, Metabolism.Type type, Pang pang, int pangDuration) {
            super(player, type, pang, pangDuration);
        }
    }
}
