package ninja.crinkle.mod.client.gui.widgets;

import ninja.crinkle.mod.client.gui.managers.GuiManager;
import ninja.crinkle.mod.client.gui.properties.Rect;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * A container that adds margins around a single child.
 */
public class MarginContainer extends AbstractContainer {
    private int marginTop;
    private int marginRight;
    private int marginBottom;
    private int marginLeft;

    protected MarginContainer(@NotNull Builder builder) {
        super(builder);
        this.marginTop = builder.marginTop;
        this.marginRight = builder.marginRight;
        this.marginBottom = builder.marginBottom;
        this.marginLeft = builder.marginLeft;
    }

    public static Builder builder(AbstractContainer parent) {
        return new Builder(parent);
    }

    public void margins(int top, int right, int bottom, int left) {
        this.marginTop = top;
        this.marginRight = right;
        this.marginBottom = bottom;
        this.marginLeft = left;
    }

    public void margins(int all) {
        margins(all, all, all, all);
    }

    @Override
    public int getMinimumWidth() {
        int childMin = children().stream().mapToInt(AbstractWidget::getMinimumWidth).max().orElse(0);
        return Math.max(super.getMinimumWidth(), childMin + marginLeft + marginRight);
    }

    @Override
    public int getMinimumHeight() {
        int childMin = children().stream().mapToInt(AbstractWidget::getMinimumHeight).max().orElse(0);
        return Math.max(super.getMinimumHeight(), childMin + marginTop + marginBottom);
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

    public static class Builder extends AbstractContainerBuilder<Builder> {
        private int marginTop = 0;
        private int marginRight = 0;
        private int marginBottom = 0;
        private int marginLeft = 0;

        public Builder(AbstractContainer container) {
            super(container);
            active(true);
        }

        public Builder(GuiManager manager) {
            super(manager);
            active(true);
        }

        public Builder margins(int all) {
            return margins(all, all, all, all);
        }

        public Builder margins(int top, int right, int bottom, int left) {
            this.marginTop = top;
            this.marginRight = right;
            this.marginBottom = bottom;
            this.marginLeft = left;
            return self();
        }

        @Override
        public AbstractContainer push() {
            return parent().add(this);
        }

        @Override
        public MarginContainer pushAndReturn() {
            MarginContainer c = new MarginContainer(this);
            parent().add(c);
            return c;
        }

        @Override
        public MarginContainer build() {
            return new MarginContainer(this);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}
