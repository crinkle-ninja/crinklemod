package ninja.crinkle.mod.client.gui.widgets;

import ninja.crinkle.mod.client.gui.managers.GuiManager;
import ninja.crinkle.mod.client.gui.properties.Rect;
import ninja.crinkle.mod.client.gui.properties.Sizing;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Horizontal box container. Arranges children left-to-right along the primary (horizontal) axis.
 * Cross axis is vertical (applies verticalSizing).
 */
public class HBoxContainer extends AbstractContainer {

    protected HBoxContainer(@NotNull AbstractContainerBuilder<?> builder) {
        super(builder);
    }

    @Override
    public void arrange() {
        List<AbstractWidget> kids = children();
        if (kids.isEmpty() || rect().equals(Rect.ZERO)) return;

        int available = rect().width() - (separation() * Math.max(0, kids.size() - 1));

        // Measure children
        int nonExpandSum = 0;
        int expandMinSum = 0;
        float totalRatio = 0;
        for (AbstractWidget child : kids) {
            if (child.horizontalSizing().contains(Sizing.Expand)) {
                totalRatio += child.stretchRatio();
                expandMinSum += child.minimumWidth();
            } else {
                nonExpandSum += child.minimumWidth();
            }
        }

        int leftover = Math.max(0, available - nonExpandSum - expandMinSum);

        // Place children
        int offset = rect().x();
        for (AbstractWidget child : kids) {
            int allocW;
            if (child.horizontalSizing().contains(Sizing.Expand)) {
                int extra = totalRatio > 0 ? (int) (leftover * (child.stretchRatio() / totalRatio)) : 0;
                allocW = child.minimumWidth() + extra;
            } else {
                allocW = child.minimumWidth();
            }

            Rect allocated = new Rect(offset, rect().y(), allocW, rect().height());
            Rect fitted = fitChildInRect(child, allocated);
            child.setRect(fitted);

            offset += allocW + separation();
        }
    }

    @Override
    public int minimumHeight() {
        int minH = super.minimumHeight();
        return Math.max(minH, children().stream().mapToInt(AbstractWidget::minimumHeight).max().orElse(0));
    }

    @SuppressWarnings("DuplicatedCode")
    @Override
    public int minimumWidth() {
        int minW = super.minimumWidth();
        List<AbstractWidget> kids = children();
        if (kids.isEmpty()) return minW;
        int childSum = kids.stream().mapToInt(AbstractWidget::minimumWidth).sum();
        int gaps = separation() * (kids.size() - 1);
        return Math.max(minW, childSum + gaps);
    }

    @Override
    public AbstractWidget visualCopy(AbstractContainer newParent) {
        HBoxContainer copy = new HBoxContainer.Builder(newParent)
                .separation(separation()).build();
        copy.copyVisualProperties(this);
        visualCopyChildrenInto(copy);
        return copy;
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
        public HBoxContainer build() {
            return new HBoxContainer(this);
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
        public HBoxContainer pushAndReturn() {
            HBoxContainer container = new HBoxContainer(this);
            parent().add(container);
            return container;
        }
    }
}
