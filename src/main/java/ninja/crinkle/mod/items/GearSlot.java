package ninja.crinkle.mod.items;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import ninja.crinkle.mod.capabilities.CrinkleGear;
import org.jetbrains.annotations.NotNull;

public class GearSlot extends Slot {
    private final CrinkleGear.SlotType slotType;

    public GearSlot(Container pContainer, int pX, int pY, CrinkleGear.SlotType slotType) {
        super(pContainer, CrinkleGear.SlotType.of(slotType), pX, pY);
        this.slotType = slotType;
    }

    public CrinkleGear.SlotType slotType() {
        return slotType;
    }

    @Override public boolean mayPlace(@NotNull ItemStack pStack) {
        return slotType().isAllowed(pStack);
    }
}
