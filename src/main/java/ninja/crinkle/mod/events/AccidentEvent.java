package ninja.crinkle.mod.events;

import net.minecraft.world.entity.player.Player;
import ninja.crinkle.mod.capabilities.MetabolismImpl;

/**
 * An accident event that is fired when a player has an accident.
 *
 * @author Galen
 * @see net.minecraftforge.eventbus.api.Event
 * @see MetabolismImpl
 */
public class AccidentEvent extends CrinkleEvent {
    private final int amount;

    public AccidentEvent(Player player, int amount, Side side, Type type) {
        super(side, player, type);
        this.amount = amount;
    }

    @Override
    public String toString() {
        return "AccidentEvent{" +
                "side=" + getSide() +
                ", amount=" + getAmount() +
                ", player=" + getPlayer() +
                '}';
    }

    /**
     * Get the amount of liquids or solids that were lost
     *
     * @return The amount of liquids or solids that were lost
     */
    public int getAmount() {
        return amount;
    }
}
