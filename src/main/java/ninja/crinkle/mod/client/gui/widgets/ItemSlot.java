package ninja.crinkle.mod.client.gui.widgets;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.LazyOptional;
import ninja.crinkle.mod.capabilities.CrinkleGear;
import ninja.crinkle.mod.capabilities.CrinkleGearProvider;
import ninja.crinkle.mod.items.GearSlot;
import org.jetbrains.annotations.NotNull;

public class ItemSlot extends CenterContainer implements Container {
    private final CrinkleGear.SlotType slotType;
    private GearSlot storage;
    private final LazyOptional<GearSlot> gearSlot = LazyOptional.of(this::createGearSlot);

    protected ItemSlot(@NotNull Builder builder) {
        super(builder);
        slotType = builder.slotType();
    }

    private GearSlot createGearSlot() {
        if (storage == null)
            storage = new GearSlot(this, rect().x(), rect().y(), slotType());
        else {
            GearSlot old = storage;
            storage = new GearSlot(this, rect().x(), rect().y(), slotType());
            storage.set(old.getItem());
        }
        return storage;
    }

    @Override public void arrange() {
        super.arrange();
        gearSlot().invalidate();
    }

    @Override public void clearContent() {
        gearSlot().ifPresent(s -> s.remove(s.getItem().getCount()));
    }

    public LazyOptional<GearSlot> gearSlot() {
        return gearSlot;
    }

    @Override public int getContainerSize() {
        return 1;
    }

    @Override public boolean isEmpty() {
        return gearSlot().map(Slot::getItem).map(ItemStack::isEmpty).orElse(true);
    }

    @Override public @NotNull ItemStack getItem(int pSlot) {
        return gearSlot().map(Slot::getItem).orElse(ItemStack.EMPTY);
    }

    @Override public @NotNull ItemStack removeItem(int pSlot, int pAmount) {
        return gearSlot().map(s -> s.remove(pAmount)).orElse(ItemStack.EMPTY);
    }

    @Override public int getMaxStackSize() {
        return slotType().limit();
    }

    @Override public @NotNull ItemStack removeItemNoUpdate(int pSlot) {
        ItemStack current = gearSlot().map(Slot::getItem).orElse(ItemStack.EMPTY);
        if (!current.isEmpty())
            gearSlot().ifPresent(s -> s.set(ItemStack.EMPTY));
        return current;
    }

    @Override public void setItem(int pSlot, @NotNull ItemStack pStack) {
        gearSlot().ifPresent(s -> s.set(pStack));
    }

    @Override public void setChanged() {
        gearSlot().ifPresent(Slot::setChanged);
    }

    public CrinkleGear.SlotType slotType() {
        return slotType;
    }

    @Override public boolean stillValid(@NotNull Player pPlayer) {
        return pPlayer.getCapability(CrinkleGearProvider.CAPABILITY).isPresent();
    }

    protected static abstract class ItemSlotBuilder<T extends CenterContainer.CenterBuilder<T>>
            extends CenterContainer.CenterBuilder<T> {

        private CrinkleGear.SlotType slotType;

        public ItemSlotBuilder(AbstractContainer container) {
            super(container);
            // Button defaults
            hoverable(true);
            active(true);
            style("slot");

        }

        public T slotType(CrinkleGear.SlotType slotType) {
            this.slotType = slotType;
            return self();
        }

        public CrinkleGear.SlotType slotType() {
            return slotType;
        }
    }

    public static class Builder extends ItemSlotBuilder<Builder> {
        public Builder(AbstractContainer container) {
            super(container);
        }

        @Override
        public ItemSlot build() {
            return new ItemSlot(this);
        }

        @Override
        public AbstractContainer push() {
            parent().add(this);
            return parent();
        }

        @Override
        protected Builder self() {
            return this;
        }

        public ItemSlot pushAndReturn() {
            ItemSlot b = new ItemSlot(this);
            parent().add(b);
            return b;
        }
    }
}
