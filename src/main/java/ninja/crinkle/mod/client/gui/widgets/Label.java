package ninja.crinkle.mod.client.gui.widgets;

import ninja.crinkle.mod.client.color.Color;
import ninja.crinkle.mod.client.gui.properties.Point;
import ninja.crinkle.mod.client.gui.properties.Rect;
import ninja.crinkle.mod.client.gui.renderers.ThemeGraphics;
import org.jetbrains.annotations.NotNull;

public class Label extends AbstractWidget {
    private Color color;
    private String text;

    protected Label(@NotNull Builder builder) {
        super(builder);
        this.text = builder.text();
        this.color = builder.color();
    }

    @Override
    public void render(@NotNull ThemeGraphics graphics, Point pMouse, float pPartialTick) {
        // A label inside a container (e.g. Button) should not render itself independently;
        // the container renders it via renderContent.
        if (!(parentOrThrow() instanceof AbstractContainer)) {
            super.render(graphics, pMouse, pPartialTick);
        }
    }

    public void color(Color color) {
        this.color = color;
    }

    public static Builder builder(AbstractContainer parent) {
        return new Builder(parent);
    }

    @Override
    public void renderContent(ThemeGraphics graphics, Point pMouse, Rect renderedRect, float pPartialTick) {
        int xOffset = (int) Math.ceil((double) (renderedRect.width() - graphics.textWidth(text())) / 2);
        int yOffset = (int) Math.ceil((double) (renderedRect.height() - graphics.textHeight()) / 2);
        graphics.text(text(), Point.of(renderedRect.x() + xOffset, renderedRect.y() + yOffset),
                zIndex(), color(), appearance().hasShadow());
    }

    public String text() {
        return text;
    }

    public Color color() {
        return color;
    }

    public void text(String text) {
        this.text = text;
    }

    public static class Builder extends AbstractBuilder<Builder> {
        private Color color = Color.WHITE;
        private String text;

        public Builder(AbstractContainer parent) {
            super(parent);
            active(true);
        }

        public Builder color(Color color) {
            this.color = color;
            return self();
        }

        @Override
        protected Builder self() {
            return this;
        }

        public Color color() {
            return color;
        }

        @Override
        public AbstractContainer push() {
            return parent().add(this);
        }

        @Override
        public Label build() {
            return new Label(this);
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
