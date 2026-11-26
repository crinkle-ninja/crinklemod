package ninja.crinkle.mod.client.gui.containers.menus;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import ninja.crinkle.mod.capabilities.CrinkleGear;
import ninja.crinkle.mod.capabilities.CrinkleGearImpl;
import ninja.crinkle.mod.items.GearSlot;
import ninja.crinkle.mod.menus.CrinkleMenus;
import org.jetbrains.annotations.NotNull;

public class CrinkleGearMenu extends ThemedContainerMenu {
    private final Player player;

    public CrinkleGearMenu(int pContainerId, Player player) {
        super(CrinkleMenus.CRINKLE_GEAR_MENU.get(), pContainerId);
        this.player = player;
        for (CrinkleGear.SlotType slotType : CrinkleGear.SlotType.values()) {
            addSlot(new GearSlot(new CrinkleGearImpl(), slotType, 0, 0));
        }
    }

    public Player player() {
        return player;
    }

    @Override public @NotNull ItemStack quickMoveStack(@NotNull Player pPlayer, int pIndex) {
        ItemStack itemStack = ItemStack.EMPTY;
        CrinkleGear.SlotType slotType = CrinkleGear.SlotType.of(pIndex);

        return itemStack;
    }

    @Override public boolean stillValid(@NotNull Player pPlayer) {
        return true;
    }
}
