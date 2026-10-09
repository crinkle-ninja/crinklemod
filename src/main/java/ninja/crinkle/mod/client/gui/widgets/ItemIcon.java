package ninja.crinkle.mod.client.gui.widgets;

import net.minecraft.world.item.ItemStack;
import ninja.crinkle.mod.client.gui.properties.Point;
import ninja.crinkle.mod.client.gui.properties.Rect;
import ninja.crinkle.mod.client.gui.renderers.ThemeGraphics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemIcon extends Icon {
    private static final int DEFAULT_ITEM_SIZE = 16;
    private ItemStack itemStack;

    protected ItemIcon(@NotNull Builder builder) {
        super(builder);
        this.itemStack = builder.itemStack();
    }

    public @Nullable ItemStack itemStack() {
        return itemStack;
    }

    public void itemStack(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    @Override
    public int minimumWidth() {
        int explicit = super.minimumWidth();
        if (explicit > 0) return explicit;
        return DEFAULT_ITEM_SIZE;
    }

    @Override
    public int minimumHeight() {
        int explicit = super.minimumHeight();
        if (explicit > 0) return explicit;
        return DEFAULT_ITEM_SIZE;
    }

    @Override
    public void renderContent(ThemeGraphics graphics, Point pMouse, Rect renderedRect, float pPartialTick) {
        if (itemStack == null || itemStack.isEmpty()) return;
        int xOffset = (renderedRect.width() - DEFAULT_ITEM_SIZE) / 2;
        int yOffset = (renderedRect.height() - DEFAULT_ITEM_SIZE) / 2;
        int x = renderedRect.x() + xOffset;
        int y = renderedRect.y() + yOffset;
        if (dropShadow()) {
            graphics.renderItemWithShadow(itemStack, x, y, zIndex());
        } else {
            graphics.renderItem(itemStack, x, y, zIndex());
        }
    }

    @Override
    public AbstractWidget visualCopy(AbstractContainer newParent) {
        ItemIcon copy = new ItemIcon.Builder(newParent)
                .itemStack(itemStack != null ? itemStack.copy() : null).build();
        copy.copyVisualProperties(this);
        return copy;
    }

    public static class Builder extends AbstractIconBuilder<Builder> {
        private ItemStack itemStack;

        public Builder(AbstractContainer parent) {
            super(parent);
        }

        @Override
        public ItemIcon build() {
            return new ItemIcon(this);
        }

        @Override
        public AbstractContainer push() {
            return parent().add(this);
        }

        @Override
        protected Builder self() {
            return this;
        }

        public ItemIcon pushAndReturn() {
            ItemIcon icon = new ItemIcon(this);
            parent().add(icon);
            return icon;
        }

        public Builder itemStack(ItemStack itemStack) {
            this.itemStack = itemStack;
            return self();
        }

        public ItemStack itemStack() {
            return itemStack;
        }
    }
}