package ninja.crinkle.mod.client.gui.layouts;

import ninja.crinkle.mod.client.gui.widgets.AbstractContainer;

public class Vertical extends AbstractLayout {
    protected Vertical(AbstractBuilder<?> builder) {
        super(builder);
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public void arrange(AbstractContainer container) {
    }

    public static class Builder extends AbstractBuilder<Builder> {
        public Builder() {
            super();
        }

        @Override
        public Vertical build() {
            return new Vertical(this);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}
