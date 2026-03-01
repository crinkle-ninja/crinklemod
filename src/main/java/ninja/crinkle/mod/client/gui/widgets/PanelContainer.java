package ninja.crinkle.mod.client.gui.widgets;

import org.jetbrains.annotations.NotNull;

/**
 * A MarginContainer that defaults to the "panel" style.
 */
public class PanelContainer extends MarginContainer {
    protected PanelContainer(@NotNull MarginContainer.BaseBuilder<?> builder) {
        super(builder);
    }

    public static class Builder extends MarginContainer.BaseBuilder<Builder> {
        public Builder(AbstractContainer parent) {
            super(parent);
            style("panel");
        }

        @Override
        public AbstractContainer push() {
            return parent().add(this);
        }

        @Override
        public PanelContainer build() {
            return new PanelContainer(this);
        }

        @Override
        public PanelContainer pushAndReturn() {
            PanelContainer c = new PanelContainer(this);
            parent().add(c);
            return c;
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}
