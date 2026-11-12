package ninja.crinkle.mod.capabilities;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.items.IItemHandler;
import ninja.crinkle.mod.datagen.CrinkleItemTags;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.*;

public interface CrinkleGear extends IItemHandler, INBTSerializable<CompoundTag> {
    ItemStack getUndergarment();
    void setUndergarment(ItemStack itemStack);

    @Override default int getSlots() {
        return SlotType.values().length;
    }

    @Override default @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        if (!isItemValid(slot, stack)) return stack;
        SlotType slotType = SlotType.of(slot);
        ItemStack current = slotType.getter().apply(this);
        int actual = Math.min(slotType.limit(), stack.getCount());
        if (current != ItemStack.EMPTY) {
            if (!current.hasTag() && current.is(stack.getItem())) {
                actual = Math.max(1, Math.min(actual - current.getCount(), slotType.limit()));
            } else {
                extractItem(slot, current.getCount(), simulate);
            }
        }
        if (!simulate) {
            slotType.setter().accept(this, stack.copyWithCount(actual));
        }
        return stack.copyWithCount(stack.getCount() - actual);
    }

    @Override default @NotNull ItemStack getStackInSlot(int slot) {
        return SlotType.of(slot).getter().apply(this);
    }

    @Override default @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
        SlotType slotType = SlotType.of(slot);
        ItemStack existing = slotType.getter().apply(this);
        if (!existing.equals(ItemStack.EMPTY)) {
            ItemStack extracted = existing.copyWithCount(amount);
            if (!simulate)
                slotType.setter().accept(this, ItemStack.EMPTY);
            return extracted;
        }
        return ItemStack.EMPTY;
    }

    @Override default int getSlotLimit(int slot) {
        return SlotType.of(slot).limit();
    }

    @Override default boolean isItemValid(int slot, @NotNull ItemStack stack) {
        return SlotType.of(slot).isAllowed(stack);
    }

    enum SlotType {
        Undergarment(1, Collections.singletonList(CrinkleItemTags.UNDERGARMENTS), "gear.undergarmentSlot",
                CrinkleGear::getUndergarment, CrinkleGear::setUndergarment);

        private final List<TagKey<Item>> allowedItemTags;
        private final Function<CrinkleGear, ItemStack> getter;
        private final String key;
        private final int limit;
        private final BiConsumer<CrinkleGear, ItemStack> setter;
        private static final SlotType[] typeMap = {
                SlotType.Undergarment
        };

        SlotType(int limit, List<TagKey<Item>> allowedItemTags, String key,
                 Function<CrinkleGear, ItemStack> getter, BiConsumer<CrinkleGear, ItemStack> setter) {
            this.getter = getter;
            this.setter = setter;
            this.key = key;
            this.limit = limit;
            this.allowedItemTags = allowedItemTags;
        }

        public static SlotType of(int slot) {
            if (slot >= typeMap.length) {
                throw new IndexOutOfBoundsException("you probably forgot to put your new slot in CrinkleGear.SlotType.typeMap!");
            }
            return typeMap[slot];
        }

        public static int of(SlotType slotType) {
            return Arrays.asList(typeMap).indexOf(slotType);
        }

        public Function<CrinkleGear, ItemStack> getter() {
            return getter;
        }

        public boolean isAllowed(ItemStack itemStack) {
            return itemStack.getTags().anyMatch(allowedItemTags::contains);
        }

        public String key() {
            return key;
        }

        public int limit() {
            return limit;
        }

        public BiConsumer<CrinkleGear, ItemStack> setter() {
            return setter;
        }
    }
}
