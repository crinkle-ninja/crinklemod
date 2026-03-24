package ninja.crinkle.mod.events;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class LeakEvent extends AccidentEvent {
    private final ItemStack itemStack;

    public LeakEvent(Player player, int amount, ItemStack itemStack, Side side, Type type) {
        super(player, amount, side, type);
        this.itemStack = itemStack;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }
}
