package ninja.crinkle.mod.client.gui.widgets;

import ninja.crinkle.mod.client.gui.managers.GuiManager;
import ninja.crinkle.mod.client.gui.properties.Rect;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * A container that adds margins around a single child.
 */
public class MarginContainer extends AbstractContainer {
    private int marginBottom;
    private int marginLeft;
    private int marginRight;
    private int marginTop;

    protected MarginContainer(@NotNull BaseBuilder<?> builder) {
        super(builder);
        this.marginTop = builder.marginTop;
        this.marginRight = builder.marginRight;
        this.marginBottom = builder.marginBottom;
        this.marginLeft = builder.marginLeft;
    }

    @Override
    public void arrange() {
        List<AbstractWidget> kids = children();
        if (kids.isEmpty() || rect().equals(Rect.ZERO)) return;
        // Only the first child is arranged
        AbstractWidget child = kids.get(0);
        Rect inner = rect().shrink(marginTop, marginRight, marginBottom, marginLeft);
        Rect fitted = fitChildInRect(child, inner);
        child.setRect(fitted);
    }

    protected int marginBottom() {
        return marginBottom;
    }

    protected int marginLeft() {
        return marginLeft;
    }

    protected int marginRight() {
        return marginRight;
    }

    protected int marginTop() {
        return marginTop;
    }

    public void margins(int all) {
        margins(all, all, all, all);
    }

    public void margins(int top, int right, int bottom, int left) {
        this.marginTop = top;
        this.marginRight = right;
        this.marginBottom = bottom;
        this.marginLeft = left;
    }

    @Override
    public int minimumHeight() {
        int childMin = children().stream().mapToInt(AbstractWidget::minimumHeight).max().orElse(0);
        return Math.max(super.minimumHeight(), childMin + marginTop + marginBottom);
    }

    @Override
    public int minimumWidth() {
        int childMin = children().stream().mapToInt(AbstractWidget::minimumWidth).max().orElse(0);
        return Math.max(super.minimumWidth(), childMin + marginLeft + marginRight);
    }

    @Override
    public AbstractWidget visualCopy(AbstractContainer newParent) {
        MarginContainer copy = new MarginContainer.Builder(newParent)
                .margins(marginTop, marginRight, marginBottom, marginLeft).build();
        copy.copyVisualProperties(this);
        visualCopyChildrenInto(copy);
        return copy;
    }

    protected static abstract class BaseBuilder<T extends BaseBuilder<T>>
            extends AbstractContainerBuilder<T> {
        int marginBottom = 0;
        int marginLeft = 0;
        int marginRight = 0;
        int marginTop = 0;

        public BaseBuilder(AbstractContainer container) {
            super(container);
            active(true);
        }

        public BaseBuilder(GuiManager manager) {
            super(manager);
            active(true);
        }

        public T margins(int all) {
            return margins(all, all, all, all);
        }

        public T margins(int top, int right, int bottom, int left) {
            this.marginTop = top;
            this.marginRight = right;
            this.marginBottom = bottom;
            this.marginLeft = left;
            return self();
        }
    }

    public static class Builder extends BaseBuilder<Builder> {
        public Builder(AbstractContainer container) {
            super(container);
        }

        public Builder(GuiManager manager) {
            super(manager);
        }

        @Override
        public MarginContainer build() {
            return new MarginContainer(this);
        }

        @Override
        public AbstractContainer push() {
            return parent().add(this);
        }

        @Override
        protected Builder self() {
            return this;
        }

        @Override
        public MarginContainer pushAndReturn() {
            MarginContainer c = new MarginContainer(this);
            parent().add(c);
            return c;
        }
    }
}
