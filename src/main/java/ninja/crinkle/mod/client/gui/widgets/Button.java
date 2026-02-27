package ninja.crinkle.mod.client.gui.widgets;

import ninja.crinkle.mod.client.gui.events.ClickEvent;
import ninja.crinkle.mod.client.gui.managers.EventManager;
import ninja.crinkle.mod.client.gui.properties.Point;
import ninja.crinkle.mod.client.gui.properties.Rect;
import ninja.crinkle.mod.client.gui.renderers.ThemeGraphics;

import java.util.function.BiConsumer;

public class Button extends AbstractContainer {
    private final BiConsumer<ClickEvent, AbstractWidget> onClick;
    private Label text;

    protected Button(Builder builder) {
        super(builder);
        this.text = builder.text();
        this.onClick = builder.onClick();
        if (text != null) {
            text.zIndex(zIndex() + 1);
            text.priority(EventManager.PRIORITY_IGNORE);
            add(text);
        }
    }

    @Override
    public void renderContent(ThemeGraphics graphics, Point pMouse, Rect renderRect, float pPartialTick) {
        super.renderContent(graphics, pMouse, renderRect, pPartialTick);
        if (text != null) {
            text.renderContent(graphics, pMouse, renderRect, pPartialTick);
        }
    }

    public static Builder builder(AbstractContainer parent) {
        return new Builder(parent);
    }

    @Override
    public void onClick(ClickEvent event) {
        super.onClick(event);
        if (event.cancelled()) return;
        if (mouseOver(event.position()) && event.isLeftButton() && onClick != null && visible() && active() && event.released()) {
            event.consumer(this);
            onClick.accept(event, this);
        }
    }

    public Label text() {
        return text;
    }

    public void text(Label text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return "Button{" + "text=" + text + ", onClick=" + (onClick != null) + ", " + super.toString() + '}';
    }

    public static class Builder extends AbstractContainerBuilder<Builder> {
        private BiConsumer<ClickEvent, AbstractWidget> onClick;
        private Label text;

        public Builder(AbstractContainer container) {
            super(container);
            // Button defaults
            hoverable(true);
            focusable(true);
            active(true);
            pressable(true);
        }

        public Builder onClick(BiConsumer<ClickEvent, AbstractWidget> onClick) {
            this.onClick = onClick;
            return self();
        }

        public BiConsumer<ClickEvent, AbstractWidget> onClick() {
            return onClick;
        }

        @Override
        public AbstractContainer push() {
            parent().add(this);
            return parent();
        }

        public Button pushAndReturn() {
            Button b = new Button(this);
            parent().add(b);
            return b;
        }

        @Override
        public Button build() {
            return new Button(this);
        }

        @Override
        protected Builder self() {
            return this;
        }

        public Builder text(String text) {
            return text(Label.builder(parent()).text(text).focusable(false).build());
        }

        public Builder text(Label text) {
            this.text = text;
            return self();
        }

        public Label text() {
            return text;
        }
    }
}
