package ninja.crinkle.mod.events;

import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.Event;
import ninja.crinkle.mod.capabilities.MetabolismImpl;
import ninja.crinkle.mod.metabolism.Metabolism;

/**
 * An accident event that is fired when a player has an accident.
 *
 * @author Galen
 * @see Event
 * @see MetabolismImpl
 */
public class DesperationEvent extends CrinkleEvent {
    private final Metabolism.DesperationLevel level;

    public DesperationEvent(Player player, Metabolism.DesperationLevel level, Side side, Type type) {
        super(side, player, type);
        this.level = level;
    }

    @Override
    public String toString() {
        return "DesperationEvent{" +
                "side=" + getSide() +
                ", level=" + getLevel() +
                ", player=" + getPlayer() +
                '}';
    }

    /**
     * Get the amount of liquids or solids that were lost
     *
     * @return The amount of liquids or solids that were lost
     */
    public Metabolism.DesperationLevel getLevel() {
        return level;
    }
}
