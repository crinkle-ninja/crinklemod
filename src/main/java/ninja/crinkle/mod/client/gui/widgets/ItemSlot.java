package ninja.crinkle.mod.client.gui.widgets;

import net.minecraft.world.inventory.Slot;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public class ItemSlot extends CenterContainer {
    private final Function<Widget, Slot> slotSupplier;

    protected ItemSlot(@NotNull Builder builder) {
        super(builder);
        slotSupplier = builder.slotSupplier();
    }

    public Function<Widget, Slot> slotSupplier() {
        return slotSupplier;
    }

    protected static abstract class ItemSlotBuilder<T extends CenterContainer.CenterBuilder<T>>
            extends CenterContainer.CenterBuilder<T> {
        private Function<Widget, Slot> slotSupplier;

        public ItemSlotBuilder(AbstractContainer container) {
            super(container);
            // Button defaults
            hoverable(true);
            active(true);
            style("slot");
        }

        public Function<Widget, Slot> slotSupplier() {
            return slotSupplier;
        }

        public T slotSupplier(Function<Widget, Slot> slotSupplier) {
            this.slotSupplier = slotSupplier;
            return self();
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
