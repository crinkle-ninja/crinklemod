package ninja.crinkle.mod.menus;

import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;
import ninja.crinkle.mod.blocks.CrinkleBlocks;
import ninja.crinkle.mod.blocks.entities.DunnyBlockEntity;
import org.jetbrains.annotations.NotNull;

public class DunnyContainer extends AbstractContainerMenu {

    private final BlockPos pos;

    public DunnyContainer(int windowId, Player player, BlockPos pos) {
        super(CrinkleMenus.DUNNY_CONTAINER.get(), windowId);
        this.pos = pos;
        if (player.level().getBlockEntity(pos) instanceof DunnyBlockEntity processor) {
            addSlot(new SlotItemHandler(processor.getInputItems(), DunnyBlockEntity.SLOT_INPUT, 64, 24));
            addSlot(new SlotItemHandler(processor.getOutputItems(), DunnyBlockEntity.SLOT_OUTPUT, 108, 24));
            addSlot(new SlotItemHandler(processor.getOutputItems(), DunnyBlockEntity.SLOT_OUTPUT + 1, 126, 24));
            addSlot(new SlotItemHandler(processor.getOutputItems(), DunnyBlockEntity.SLOT_OUTPUT + 2, 144, 24));
            addSlot(new SlotItemHandler(processor.getOutputItems(), DunnyBlockEntity.SLOT_OUTPUT + 3, 108, 42));
            addSlot(new SlotItemHandler(processor.getOutputItems(), DunnyBlockEntity.SLOT_OUTPUT + 4, 126, 42));
            addSlot(new SlotItemHandler(processor.getOutputItems(), DunnyBlockEntity.SLOT_OUTPUT + 5, 144, 42));
        }
        layoutPlayerInventorySlots(player.getInventory(), 10, 70);
    }

    private void layoutPlayerInventorySlots(Container playerInventory, int leftCol, int topRow) {
        // Player inventory
        addSlotBox(playerInventory, 9, leftCol, topRow, 9, 18, 3, 18);

        // Hotbar
        topRow += 58;
        addSlotRange(playerInventory, 0, leftCol, topRow, 9, 18);
    }

    private int addSlotBox(Container playerInventory, int index, int x, int y, int horAmount, int dx, int verAmount,
                           int dy) {
        for (int j = 0; j < verAmount; j++) {
            index = addSlotRange(playerInventory, index, x, y, horAmount, dx);
            y += dy;
        }
        return index;
    }

    private int addSlotRange(Container playerInventory, int index, int x, int y, int amount, int dx) {
        for (int i = 0; i < amount; i++) {
            addSlot(new Slot(playerInventory, index, x, y));
            x += dx;
            index++;
        }
        return index;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot.hasItem()) {
            ItemStack stack = slot.getItem();
            itemstack = stack.copy();
            if (index < DunnyBlockEntity.SLOT_COUNT) {
                if (!this.moveItemStackTo(stack, DunnyBlockEntity.SLOT_COUNT,
                        Inventory.INVENTORY_SIZE + DunnyBlockEntity.SLOT_COUNT, true)) {
                    return ItemStack.EMPTY;
                }
            }
            if (!this.moveItemStackTo(stack, DunnyBlockEntity.SLOT_INPUT, DunnyBlockEntity.SLOT_INPUT + 1, false)) {
                if (index < 27 + DunnyBlockEntity.SLOT_COUNT) {
                    if (!this.moveItemStackTo(stack, 27 + DunnyBlockEntity.SLOT_COUNT,
                            36 + DunnyBlockEntity.SLOT_COUNT, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (index < Inventory.INVENTORY_SIZE + DunnyBlockEntity.SLOT_COUNT && !this.moveItemStackTo(stack, DunnyBlockEntity.SLOT_COUNT, 27 + DunnyBlockEntity.SLOT_COUNT, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (stack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (stack.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, stack);
        }

        return itemstack;
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return stillValid(ContainerLevelAccess.create(player.level(), pos), player, CrinkleBlocks.DUNNY_BLOCK.get());
    }
}
