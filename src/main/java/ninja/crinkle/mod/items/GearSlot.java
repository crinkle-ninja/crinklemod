package ninja.crinkle.mod.items;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;
import ninja.crinkle.mod.capabilities.CrinkleGear;
import org.jetbrains.annotations.NotNull;

public class GearSlot extends SlotItemHandler {
    private final CrinkleGear.SlotType slotType;

    public GearSlot(CrinkleGear crinkleGear, CrinkleGear.SlotType slotType, int pX, int pY) {
        super(crinkleGear, CrinkleGear.SlotType.of(slotType), pX, pY);
        this.slotType = slotType;
    }

    public CrinkleGear.SlotType slotType() {
        return slotType;
    }

    @Override public boolean mayPlace(@NotNull ItemStack pStack) {
        return slotType().isAllowed(pStack);
    }
}
