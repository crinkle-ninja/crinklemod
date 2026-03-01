package ninja.crinkle.mod.client.gui.widgets;

import ninja.crinkle.mod.client.gui.layouts.SizeFlags;
import ninja.crinkle.mod.client.gui.managers.GuiManager;
import ninja.crinkle.mod.client.gui.properties.Rect;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Horizontal box container. Arranges children left-to-right along the primary (horizontal) axis.
 * Cross axis is vertical (applies vSizeFlags).
 */
public class HBoxContainer extends AbstractContainer {

    protected HBoxContainer(@NotNull AbstractContainerBuilder<?> builder) {
        super(builder);
    }

    public static Builder builder(AbstractContainer parent) {
        return new Builder(parent);
    }

    public static Builder builder(GuiManager manager) {
        return new Builder(manager);
    }

    @Override
    public int getMinimumWidth() {
        int minW = super.getMinimumWidth();
        List<AbstractWidget> kids = children();
        if (kids.isEmpty()) return minW;
        int childSum = kids.stream().mapToInt(AbstractWidget::getMinimumWidth).sum();
        int gaps = separation() * (kids.size() - 1);
        return Math.max(minW, childSum + gaps);
    }

    @Override
    public int getMinimumHeight() {
        int minH = super.getMinimumHeight();
        return Math.max(minH, children().stream().mapToInt(AbstractWidget::getMinimumHeight).max().orElse(0));
    }

    @Override
    public void arrange() {
        List<AbstractWidget> kids = children();
        if (kids.isEmpty() || rect().equals(Rect.ZERO)) return;

        int available = rect().width() - (separation() * Math.max(0, kids.size() - 1));

        // Measure non-expanding children
        int nonExpandSum = 0;
        float totalRatio = 0;
        for (AbstractWidget child : kids) {
            if (child.hSizeFlags().contains(SizeFlags.Expand)) {
                totalRatio += child.stretchRatio();
            } else {
                nonExpandSum += child.getMinimumWidth();
            }
        }

        int leftover = Math.max(0, available - nonExpandSum);

        // Place children
        int offset = rect().x();
        for (AbstractWidget child : kids) {
            int allocW;
            if (child.hSizeFlags().contains(SizeFlags.Expand)) {
                int extra = totalRatio > 0 ? (int) (leftover * (child.stretchRatio() / totalRatio)) : 0;
                allocW = child.getMinimumWidth() + extra;
            } else {
                allocW = child.getMinimumWidth();
            }

            Rect allocated = new Rect(offset, rect().y(), allocW, rect().height());
            Rect fitted = fitChildInRect(child, allocated);
            child.setRect(fitted);

            offset += allocW + separation();
        }
    }

    public static class Builder extends AbstractContainerBuilder<Builder> {

        public Builder(AbstractContainer container) {
            super(container);
            active(true);
        }

        public Builder(GuiManager manager) {
            super(manager);
            active(true);
        }

        @Override
        public AbstractContainer push() {
            return parent().add(this);
        }

        @Override
        public HBoxContainer pushAndReturn() {
            HBoxContainer container = new HBoxContainer(this);
            parent().add(container);
            return container;
        }

        @Override
        public HBoxContainer build() {
            return new HBoxContainer(this);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}
