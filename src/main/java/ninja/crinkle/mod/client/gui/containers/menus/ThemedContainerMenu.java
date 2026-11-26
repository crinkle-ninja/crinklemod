package ninja.crinkle.mod.client.gui.containers.menus;

import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import ninja.crinkle.mod.client.gui.widgets.ItemSlot;
import org.jetbrains.annotations.Nullable;

public abstract class ThemedContainerMenu extends AbstractContainerMenu {
    protected ThemedContainerMenu(@Nullable MenuType<?> pMenuType, int pContainerId) {
        super(pMenuType, pContainerId);
    }

    @SuppressWarnings("UnusedReturnValue")
    public Slot addSlot(ItemSlot itemSlot) {
        Slot added = addSlot(itemSlot.slotSupplier().apply(itemSlot));
        itemSlot.onArrange(w -> {
            Slot newSlot = itemSlot.slotSupplier().apply(w);
            slots.set(newSlot.index, newSlot);
        });
        return added;
    }
}
