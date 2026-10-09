package ninja.crinkle.mod.client.gui.widgets;

import org.jetbrains.annotations.NotNull;

/**
 * A MarginContainer that defaults to the "panel" style.
 */
public class PanelContainer extends MarginContainer {
    protected PanelContainer(@NotNull MarginContainer.BaseBuilder<?> builder) {
        super(builder);
    }

    @Override
    public AbstractWidget visualCopy(AbstractContainer newParent) {
        PanelContainer copy = new PanelContainer.Builder(newParent)
                .margins(marginTop(), marginRight(), marginBottom(), marginLeft()).build();
        copy.copyVisualProperties(this);
        visualCopyChildrenInto(copy);
        return copy;
    }

    public static class Builder extends MarginContainer.BaseBuilder<Builder> {
        public Builder(AbstractContainer parent) {
            super(parent);
            style("panel");
        }

        @Override
        public PanelContainer build() {
            return new PanelContainer(this);
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
        public PanelContainer pushAndReturn() {
            PanelContainer c = new PanelContainer(this);
            parent().add(c);
            return c;
        }
    }
}
