package ninja.crinkle.mod.client.gui.widgets;

import ninja.crinkle.mod.client.color.Color;
import ninja.crinkle.mod.client.gui.events.BehaviorChangedEvent;
import ninja.crinkle.mod.client.gui.properties.Point;
import ninja.crinkle.mod.client.gui.properties.Rect;
import ninja.crinkle.mod.client.gui.renderers.ThemeGraphics;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class Label extends AbstractWidget {
    private Color color;
    private String text;

    protected Label(@NotNull Builder builder) {
        super(builder);
        this.text = builder.text();
        this.color = builder.color();
    }

    public void color(Color color) {
        this.color = color;
    }

    @Override
    protected void registerEventHandlers() {
        super.registerEventHandlers();
        eventManager().addListener(BehaviorChangedEvent.KEY, priority(), this::onBehaviorChanged);
    }

    public void onBehaviorChanged(BehaviorChangedEvent event) {
        if (event.consumed() || event.cancelled() || event.source() == this || parent().isEmpty() || !event.source().equals(parent().get()))
            return;
        active(event.current().active());
        event.consumed(true);
    }

    @Override
    public int minimumHeight() {
        int explicit = super.minimumHeight();
        if (explicit > 0 || text == null || text.isEmpty()) return explicit;
        return appearance().font().lineHeight;
    }

    @Override
    public int minimumWidth() {
        int explicit = super.minimumWidth();
        if (explicit > 0 || text == null || text.isEmpty()) return explicit;
        return appearance().font().width(text);
    }

    @Override
    public void renderContent(ThemeGraphics graphics, Point pMouse, Rect renderedRect, float pPartialTick) {
        int xOffset = (int) Math.ceil((double) (renderedRect.width() - graphics.textWidth(text())) / 2);
        int yOffset = (int) Math.ceil((double) (renderedRect.height() - graphics.textHeight()) / 2);
        graphics.text(text(), Point.of(renderedRect.x() + xOffset, renderedRect.y() + yOffset),
                zIndex(), color(), appearance().shadow());
    }

    @Override
    public void render(@NotNull ThemeGraphics graphics, Point pMouse, float pPartialTick) {
        // A label inside a Button should not render itself independently;
        // the Button renders it via renderContent.
        if (!(parentOrThrow() instanceof Button)) {
            super.render(graphics, pMouse, pPartialTick);
        }
    }

    @Override
    public AbstractWidget visualCopy(AbstractContainer newParent) {
        Label copy = new Label.Builder(newParent)
                .text(text).color(color).build();
        copy.copyVisualProperties(this);
        return copy;
    }

    public String text() {
        return text;
    }

    public Color color() {
        return Optional.ofNullable(color)
                .or(() -> Optional.ofNullable(appearance().foregroundColor()))
                .orElse(Color.RAINBOW);
    }

    public void text(String text) {
        this.text = text;
    }

    public static class Builder extends AbstractBuilder<Builder> {
        private Color color;
        private String text;

        public Builder(AbstractContainer parent) {
            super(parent);
            style("label");
            active(true);
        }

        @Override
        public Label build() {
            return new Label(this);
        }

        @Override
        public AbstractContainer push() {
            return parent().add(this);
        }

        public Color color() {
            return color;
        }

        public Builder color(Color color) {
            this.color = color;
            return self();
        }

        @Override
        protected Builder self() {
            return this;
        }

        public Label pushAndReturn() {
            Label label = new Label(this);
            parent().add(label);
            return label;
        }

        public Builder text(String text) {
            this.text = text;
            return this;
        }

        public String text() {
            return text;
        }
    }
}
