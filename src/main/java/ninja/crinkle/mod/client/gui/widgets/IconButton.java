package ninja.crinkle.mod.client.gui.widgets;

import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import ninja.crinkle.mod.client.gui.events.ClickEvent;
import ninja.crinkle.mod.client.gui.managers.EventManager;
import ninja.crinkle.mod.client.gui.properties.Point;
import ninja.crinkle.mod.client.gui.properties.Rect;
import ninja.crinkle.mod.client.gui.renderers.ThemeGraphics;
import ninja.crinkle.mod.client.gui.textures.Texture;
import ninja.crinkle.mod.client.gui.textures.TextureSize;
import ninja.crinkle.mod.client.gui.themes.StyleVariant;

import java.util.function.BiConsumer;

public class IconButton extends AbstractContainer {
    private final BiConsumer<ClickEvent, AbstractWidget> onClick;
    private Icon icon;
    private Label label;

    protected IconButton(Builder builder) {
        super(builder);
        this.onClick = builder.onClick();

        if (builder.itemStack() != null) {
            this.icon = new ItemIcon.Builder(this)
                    .itemStack(builder.itemStack())
                    .textureSize(builder.textureSize())
                    .dropShadow(builder.dropShadow())
                    .pushAndReturn();
            icon.zIndex(zIndex() + 1);
            icon.priority(EventManager.PRIORITY_IGNORE);
            add(icon);
        } else if (builder.texture() != null) {
            this.icon = new Icon.Builder(this)
                    .atlas(builder.atlas())
                    .texture(builder.texture())
                    .textureSize(builder.textureSize())
                    .dropShadow(builder.dropShadow())
                    .pushAndReturn();
            icon.zIndex(zIndex() + 1);
            icon.priority(EventManager.PRIORITY_IGNORE);
            add(icon);
        }

        if (builder.text() != null) {
            this.label = new Label.Builder(this)
                    .text(builder.text())
                    .pushAndReturn();
            label.zIndex(zIndex() + 1);
            label.priority(EventManager.PRIORITY_IGNORE);
            add(label);
        }
    }

    @Override
    public int minimumHeight() {
        int explicit = super.minimumHeight();
        int childMin = children().stream().mapToInt(AbstractWidget::minimumHeight).max().orElse(0);
        return Math.max(explicit, childMin + borderTop() + borderBottom());
    }

    @Override
    public int minimumWidth() {
        int explicit = super.minimumWidth();
        int iconWidth = children().stream()
                .filter(c -> c instanceof Icon || c instanceof ItemIcon)
                .mapToInt(AbstractWidget::minimumWidth).sum();
        int labelWidth = label != null ? label.minimumWidth() : 0;
        int contentWidth = iconWidth + (iconWidth > 0 && labelWidth > 0 ? separation() : 0) + labelWidth;
        return Math.max(explicit, contentWidth + borderLeft() + borderRight());
    }

    @Override
    public void arrange() {
        if (children().isEmpty() || rect().equals(Rect.ZERO)) return;
        for (AbstractWidget child : children()) {
            Rect fitted = fitChildInRect(child, rect());
            child.setRect(fitted);
        }
    }

    @Override
    public void renderContent(ThemeGraphics graphics, Point pMouse, Rect renderRect, float pPartialTick) {
        super.renderContent(graphics, pMouse, renderRect, pPartialTick);
        if (label != null) {
            label.renderContent(graphics, pMouse, renderRect, pPartialTick);
        }
    }

    @Override
    protected void registerEventHandlers() {
        super.registerEventHandlers();
        eventManager().addListener(ClickEvent.KEY, priority(), this::onClick);
    }

    public void onClick(ClickEvent event) {
        if (event.cancelled()) return;
        if (mouseOver(event.position()) && event.isLeftButton() && onClick != null && visible() && active() && event.released()) {
            event.consumer(this);
            onClick.accept(event, this);
        }
    }

    public Icon icon() {
        return icon;
    }

    public Label label() {
        return label;
    }

    @Override
    public AbstractWidget visualCopy(AbstractContainer newParent) {
        IconButton copy = new IconButton.Builder(newParent).build();
        copy.copyVisualProperties(this);
        if (icon != null) {
            Icon iconCopy = (Icon) icon.visualCopy(copy);
            copy.icon = iconCopy;
            copy.add(iconCopy);
        }
        if (label != null) {
            Label labelCopy = (Label) label.visualCopy(copy);
            copy.label = labelCopy;
            copy.add(labelCopy);
        }
        return copy;
    }

    private int borderLeft() {
        Texture tex = backgroundTexture();
        return tex != null ? tex.boundsOf(Texture.Slice.Location.topLeft).width() : 0;
    }

    private int borderRight() {
        Texture tex = backgroundTexture();
        return tex != null ? tex.boundsOf(Texture.Slice.Location.topRight).width() : 0;
    }

    private int borderTop() {
        Texture tex = backgroundTexture();
        return tex != null ? tex.boundsOf(Texture.Slice.Location.topLeft).height() : 0;
    }

    private int borderBottom() {
        Texture tex = backgroundTexture();
        return tex != null ? tex.boundsOf(Texture.Slice.Location.bottomLeft).height() : 0;
    }

    private Texture backgroundTexture() {
        StyleVariant sv = appearance();
        return sv != null ? sv.backgroundTexture() : null;
    }

    @Override
    public String toString() {
        return "IconButton{icon=" + icon + ", label=" + label + ", onClick=" + (onClick != null) + ", " + super.toString() + '}';
    }

    public static class Builder extends AbstractContainerBuilder<Builder> {
        private TextureAtlas atlas;
        private BiConsumer<ClickEvent, AbstractWidget> onClick;
        private boolean dropShadow;
        private ResourceLocation texture;
        private TextureSize textureSize = TextureSize.of(16, 16);
        private ItemStack itemStack;
        private String text;

        public Builder(AbstractContainer container) {
            super(container);
            hoverable(true);
            focusable(true);
            active(true);
            pressable(true);
            style("button");
        }

        @Override
        public IconButton build() {
            return new IconButton(this);
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

        @Override
        public IconButton pushAndReturn() {
            IconButton b = new IconButton(this);
            parent().add(b);
            return b;
        }

        public BiConsumer<ClickEvent, AbstractWidget> onClick() {
            return onClick;
        }

        public Builder onClick(BiConsumer<ClickEvent, AbstractWidget> onClick) {
            this.onClick = onClick;
            return self();
        }

        public Builder texture(ResourceLocation texture) {
            this.texture = texture;
            return self();
        }

        public ResourceLocation texture() {
            return texture;
        }

        public Builder textureSize(TextureSize textureSize) {
            this.textureSize = textureSize;
            return self();
        }

        public TextureSize textureSize() {
            return textureSize;
        }

        public Builder itemStack(ItemStack itemStack) {
            this.itemStack = itemStack;
            return self();
        }

        public ItemStack itemStack() {
            return itemStack;
        }

        public Builder text(String text) {
            this.text = text;
            return self();
        }

        public String text() {
            return text;
        }

        public Builder dropShadow(boolean dropShadow) {
            this.dropShadow = dropShadow;
            return self();
        }

        public boolean dropShadow() {
            return dropShadow;
        }

        public Builder atlas(TextureAtlas atlas) {
            this.atlas = atlas;
            return self();
        }

        public TextureAtlas atlas() {
            return atlas;
        }
    }
}