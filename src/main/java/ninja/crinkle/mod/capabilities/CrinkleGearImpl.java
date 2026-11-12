package ninja.crinkle.mod.capabilities;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;
import java.util.function.Consumer;

public class CrinkleGearImpl implements CrinkleGear {
    private ItemStack undergarmentSlot = ItemStack.EMPTY;

    @Override public ItemStack getUndergarment() {
        return undergarmentSlot;
    }

    @Override public void setUndergarment(ItemStack itemStack) {
        this.undergarmentSlot = itemStack;
    }

    @Override public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        Optional.ofNullable(getUndergarment()).ifPresent(u -> nbt.put(SlotType.Undergarment.key(), u.serializeNBT()));
        return nbt;
    }

    private void loadSlot(@SuppressWarnings("SameParameterValue") SlotType slotType, CompoundTag nbt, Consumer<ItemStack> setter) {
        if (nbt.get(slotType.key()) instanceof CompoundTag tag) {
            setter.accept(ItemStack.of(tag));
        }
    }

    @Override public void deserializeNBT(CompoundTag nbt) {
        loadSlot(SlotType.Undergarment, nbt, itemStack -> undergarmentSlot = itemStack);
    }
}
