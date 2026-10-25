package ninja.crinkle.mod.client.gui.widgets;

import ninja.crinkle.mod.client.gui.actors.MouseActor;
import ninja.crinkle.mod.client.gui.actors.MouseButton;
import ninja.crinkle.mod.client.gui.properties.Point;
import ninja.crinkle.mod.client.gui.properties.Sizing;
import ninja.crinkle.mod.client.gui.textures.Texture;
import ninja.crinkle.mod.client.gui.themes.Style;
import ninja.crinkle.mod.client.gui.themes.StyleVariant;

import java.util.Optional;
import java.util.function.Consumer;

public class Button extends MarginContainer implements MouseActor {
    private final Consumer<AbstractWidget> onClick;
    private final Label text;

    protected Button(Builder builder) {
        super(builder);
        this.text = new Label.Builder(this)
                .text(builder.text())
                .pushAndReturn();
        this.onClick = builder.onClick();
        setMargins();
    }

    private void setMargins() {
        margins(Optional.ofNullable(style())
                        .map(s -> s.variant(Style.Variant.active))
                        .map(StyleVariant::backgroundTexture)
                        .map(t -> t.boundsOf(Texture.Slice.Location.top)
                                .height())
                        .orElse(0) + marginTop(),
                Optional.ofNullable(style())
                        .map(s -> s.variant(Style.Variant.active))
                        .map(StyleVariant::backgroundTexture)
                        .map(t -> t.boundsOf(Texture.Slice.Location.right)
                                .width())
                        .orElse(0) + marginRight(),
                Optional.ofNullable(style())
                        .map(s -> s.variant(Style.Variant.active))
                        .map(StyleVariant::backgroundTexture)
                        .map(t -> t.boundsOf(Texture.Slice.Location.bottom)
                                .height())
                        .orElse(0) + marginBottom(),
                Optional.ofNullable(style())
                        .map(s -> s.variant(Style.Variant.active))
                        .map(StyleVariant::backgroundTexture)
                        .map(t -> t.boundsOf(Texture.Slice.Location.left)
                                .width())
                        .orElse(0) + marginLeft());
    }

    @Override
    protected void copyVisualProperties(AbstractWidget source) {
        super.copyVisualProperties(source);
        if (source instanceof MarginContainer m)
            margins(m.marginTop(), m.marginRight(), m.marginBottom(), m.marginLeft());
    }

    @Override
    public AbstractWidget visualCopy(AbstractContainer newParent) {
        Button copy = new Button.Builder(newParent).text(text().text()).build();
        copy.copyVisualProperties(this);
        return copy;
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

    public void text(String text) {
        this.text.text(text);
    }

    protected static abstract class ButtonBuilder<T extends MarginBuilder<T>>
        extends MarginBuilder<T> {

        private Consumer<AbstractWidget> onClick;
        private String text;

        public ButtonBuilder(AbstractContainer container) {
            super(container);
            // Button defaults
            hoverable(true);
            active(true);
            pressable(true);
            verticalSizing(Sizing.Expand);
            horizontalSizing(Sizing.Expand);
            style("button");
            margins(4);
        }

        public T text(String text) {
            this.text = text;
            return self();
        }

        public String text() {
            return text;
        }

        public Consumer<AbstractWidget> onClick() {
            return onClick;
        }

        public T onClick(Consumer<AbstractWidget> onClick) {
            this.onClick = onClick;
            return self();
        }
    }

    public static class Builder extends ButtonBuilder<Builder> {
        public Builder(AbstractContainer container) {
            super(container);
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
    }
}
