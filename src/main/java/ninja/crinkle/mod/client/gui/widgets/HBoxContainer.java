package ninja.crinkle.mod.client.gui.widgets;

import ninja.crinkle.mod.client.gui.managers.GuiManager;
import org.jetbrains.annotations.NotNull;

public class HBoxContainer extends Container {
    protected HBoxContainer(@NotNull AbstractContainerBuilder<?> builder) {
        super(builder);
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
        public Container pushAndReturn() {
            Container container = new Container(this);
            parent().add(container);
            return container;
        }

        @Override
        public HBoxContainer build() {
            return new HBoxContainer(this);
        }

        @Override
        protected HBoxContainer.Builder self() {
            return this;
        }
    }
}
