package ninja.crinkle.mod.client.gui.widgets;

import ninja.crinkle.mod.client.gui.managers.GuiManager;
import ninja.crinkle.mod.client.gui.properties.Rect;
import org.jetbrains.annotations.NotNull;

public class Container extends AbstractContainer {
    public Container() {
        super();
    }

    protected Container(@NotNull AbstractContainerBuilder<?> builder) {
        super(builder);
    }

    public TextBox.Builder addTextBox() {
        return new TextBox.Builder(this);
    }

    @Override
    public void arrange() {
        if (children().isEmpty() || rect().equals(Rect.ZERO)) return;
        for (AbstractWidget child : children()) {
            Rect fitted = fitChildInRect(child, rect());
            child.setRect(fitted);
        }
    }

    @Override
    public AbstractWidget visualCopy(AbstractContainer newParent) {
        Container copy = new Container.Builder(newParent).build();
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
        public Container build() {
            return new Container(this);
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
        public Container pushAndReturn() {
            Container container = new Container(this);
            parent().add(container);
            return container;
        }
    }
}
