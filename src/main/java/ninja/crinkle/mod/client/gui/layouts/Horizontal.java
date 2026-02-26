package ninja.crinkle.mod.client.gui.layouts;

import ninja.crinkle.mod.client.gui.properties.*;
import ninja.crinkle.mod.client.gui.states.CalculatedBoxes;
import ninja.crinkle.mod.client.gui.widgets.AbstractContainer;
import ninja.crinkle.mod.client.gui.widgets.AbstractWidget;

import java.util.List;
import java.util.function.Predicate;

public class Horizontal extends AbstractLayout {
    protected Horizontal(AbstractBuilder<?> builder) {
        super(builder);
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public void arrange(AbstractContainer container) {
        Box parentContentBox = container.cachedBoxes().contentBox();
        List<AbstractWidget> childrenToArrange = container.children(Predicate.not(w -> w.layout().position().absolute()));
        if (childrenToArrange.isEmpty()) {
            return;
        }

        // 1. Measurement Pass (Size.resolve is NOT obsolete, it's essential here)
        double totalDemandedWidth = childrenToArrange.stream()
                .mapToDouble(child -> {
                    Size resolvedSize = child.layout().size().resolve(parentContentBox);
                    return resolvedSize.width();
                })
                .sum();

        // 2. Determine Scale Factor
        double scaleFactor = 1.0;
        if (totalDemandedWidth > parentContentBox.size().width()) {
            scaleFactor = parentContentBox.size().width() / totalDemandedWidth;
        }

        // 3. Arrange Pass
        double currentX = parentContentBox.topLeft().x();
        for (AbstractWidget child : childrenToArrange) {
            Size originalResolvedSize = child.layout().size().resolve(parentContentBox);
            Margin margin = child.layout().margin();

            // Scale the entire footprint of the child
            double scaledWidth = child.layout().size().unit() == Size.Unit.Pixels
                    ? originalResolvedSize.width()
                    : originalResolvedSize.width() * scaleFactor;

            // Create the absolute base box for the widget
            Position finalPosition = Position.absolute((int) Math.round(currentX) + margin.left(),
                    parentContentBox.topLeft().yInt() + margin.top() + child.layout().position().point().yInt());
            Size finalSize = new Size((int) Math.round(scaledWidth), originalResolvedSize.height(), Size.Unit.Pixels);
            Box baseBox = new Box(finalPosition, finalSize);

            // Use the simplified resolver to get the final boxes
            CalculatedBoxes boxes = CalculatedBoxes.calculate(child, baseBox);
            child.cachedBoxes(boxes);

            // Advance pointer by the widget's width and right margin
            currentX += scaledWidth;
        }
    }

    public static class Builder extends AbstractBuilder<Builder> {
        public Builder() {
            super();
        }

        @Override
        public Horizontal build() {
            return new Horizontal(this);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}
