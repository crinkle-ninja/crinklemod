package ninja.crinkle.mod.client.gui.widgets;

import ninja.crinkle.mod.client.gui.actors.MouseActor;
import ninja.crinkle.mod.client.gui.actors.MouseButton;
import ninja.crinkle.mod.client.gui.properties.Point;
import ninja.crinkle.mod.client.gui.properties.Rect;
import ninja.crinkle.mod.client.gui.renderers.ThemeGraphics;
import ninja.crinkle.mod.client.gui.textures.Texture;
import ninja.crinkle.mod.client.gui.textures.Texture.Slice;
import ninja.crinkle.mod.client.gui.themes.StyleVariant;

import java.util.function.Consumer;

public class Button extends AbstractContainer implements MouseActor {
    private final Consumer<AbstractWidget> onClick;
    private Label text;

    protected Button(Builder builder) {
        super(builder);
        this.text = new Label.Builder(this)
                .text(builder.text())
                .build();
        this.onClick = builder.onClick();
        if (text != null) {
            add(text);
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
        int childMin = children().stream().mapToInt(AbstractWidget::minimumWidth).max().orElse(0);
        return Math.max(explicit, childMin + borderLeft() + borderRight());
    }

    @Override
    public AbstractWidget visualCopy(AbstractContainer newParent) {
        Button copy = new Button.Builder(newParent).build();
        copy.copyVisualProperties(this);
        if (text != null) {
            Label labelCopy = (Label) text.visualCopy(copy);
            copy.text = labelCopy;
            copy.add(labelCopy);
        }
        return copy;
    }

    private int borderLeft() {
        Texture tex = backgroundTexture();
        return tex != null ? tex.boundsOf(Slice.Location.topLeft).width() : 0;
    }

    private int borderRight() {
        Texture tex = backgroundTexture();
        return tex != null ? tex.boundsOf(Slice.Location.topRight).width() : 0;
    }

    private Texture backgroundTexture() {
        StyleVariant sv = appearance();
        return sv != null ? sv.backgroundTexture() : null;
    }

    private int borderTop() {
        Texture tex = backgroundTexture();
        return tex != null ? tex.boundsOf(Slice.Location.topLeft).height() : 0;
    }

    private int borderBottom() {
        Texture tex = backgroundTexture();
        return tex != null ? tex.boundsOf(Slice.Location.bottomLeft).height() : 0;
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
        if (text != null) {
            text.renderContent(graphics, pMouse, renderRect, pPartialTick);
        }
    }

    @Override
    public String toString() {
        return "Button{" + "text=" + text + ", onClick=" + (onClick != null) + ", " + super.toString() + '}';
    }

    @Override
    public boolean onClick(Point point, MouseButton button) {
        if (!visible() || !active())
            return false;
        if (mouseOver(point) && button == MouseButton.LEFT && onClick != null && visible() && active()) {
            onClick.accept(this);
            return true;
        }
        return false;
    }

    public Label text() {
        return text;
    }

    public void text(Label text) {
        this.text = text;
    }

    public void text(String text) {
        if (this.text == null) {
            this.text = new Label.Builder(this)
                    .text(text)
                    .build();
        } else {
            this.text.text(text);
        }
    }

    public static class Builder extends AbstractContainerBuilder<Builder> {
        private Consumer<AbstractWidget> onClick;
        private String textString;

        public Builder(AbstractContainer container) {
            super(container);
            // Button defaults
            hoverable(true);
            focusable(true);
            active(true);
            pressable(true);
            style("button");
        }

        @Override
        public Button build() {
            return new Button(this);
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

        public Button pushAndReturn() {
            Button b = new Button(this);
            parent().add(b);
            return b;
        }

        public Consumer<AbstractWidget> onClick() {
            return onClick;
        }

        public Builder onClick(Consumer<AbstractWidget> onClick) {
            this.onClick = onClick;
            return self();
        }

        public Builder text(String text) {
            textString = text;
            return self();
        }

        public String text() {
            return textString;
        }
    }
}
