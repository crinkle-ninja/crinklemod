package ninja.crinkle.mod.client.gui.widgets;

import ninja.crinkle.mod.client.gui.layouts.SizeFlags;
import ninja.crinkle.mod.client.gui.managers.GuiManager;
import ninja.crinkle.mod.client.gui.properties.Rect;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Vertical box container. Arranges children top-to-bottom along the primary (vertical) axis.
 * Cross axis is horizontal (applies hSizeFlags).
 */
public class VBoxContainer extends AbstractContainer {

    protected VBoxContainer(@NotNull AbstractContainerBuilder<?> builder) {
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
        return Math.max(minW, children().stream().mapToInt(AbstractWidget::getMinimumWidth).max().orElse(0));
    }

    @Override
    public int getMinimumHeight() {
        int minH = super.getMinimumHeight();
        List<AbstractWidget> kids = children();
        if (kids.isEmpty()) return minH;
        int childSum = kids.stream().mapToInt(AbstractWidget::getMinimumHeight).sum();
        int gaps = separation() * (kids.size() - 1);
        return Math.max(minH, childSum + gaps);
    }

    @Override
    public void arrange() {
        List<AbstractWidget> kids = children();
        if (kids.isEmpty() || rect().equals(Rect.ZERO)) return;

        int available = rect().height() - (separation() * Math.max(0, kids.size() - 1));

        // Measure non-expanding children
        int nonExpandSum = 0;
        float totalRatio = 0;
        for (AbstractWidget child : kids) {
            if (child.vSizeFlags().contains(SizeFlags.EXPAND)) {
                totalRatio += child.stretchRatio();
            } else {
                nonExpandSum += child.getMinimumHeight();
            }
        }

        int leftover = Math.max(0, available - nonExpandSum);

        // Place children
        int offset = rect().y();
        for (AbstractWidget child : kids) {
            int allocH;
            if (child.vSizeFlags().contains(SizeFlags.EXPAND)) {
                int extra = totalRatio > 0 ? (int) (leftover * (child.stretchRatio() / totalRatio)) : 0;
                allocH = child.getMinimumHeight() + extra;
            } else {
                allocH = child.getMinimumHeight();
            }

            Rect allocated = new Rect(rect().x(), offset, rect().width(), allocH);
            Rect fitted = fitChildInRect(child, allocated);
            child.setRect(fitted);

            offset += allocH + separation();
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
        public VBoxContainer pushAndReturn() {
            VBoxContainer container = new VBoxContainer(this);
            parent().add(container);
            return container;
        }

        @Override
        public VBoxContainer build() {
            return new VBoxContainer(this);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}
