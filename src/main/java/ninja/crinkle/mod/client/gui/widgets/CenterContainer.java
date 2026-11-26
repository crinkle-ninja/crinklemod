package ninja.crinkle.mod.client.gui.widgets;

import ninja.crinkle.mod.client.gui.managers.GuiManager;
import ninja.crinkle.mod.client.gui.properties.Rect;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * A container that centers a single child at its minimum size within this container's rect.
 */
public class CenterContainer extends AbstractContainer {

    protected CenterContainer(@NotNull AbstractContainerBuilder<?> builder) {
        super(builder);
    }

    @Override
    public boolean arrangeContent() {
        List<Widget> kids = children();
        if (kids.isEmpty() || rect().equals(Rect.ZERO)) return false;
        // Only the first child is arranged
        Widget child = kids.get(0);
        int childW = child.minimumWidth();
        int childH = child.minimumHeight();
        int x = rect().x() + (rect().width() - childW) / 2;
        int y = rect().y() + (rect().height() - childH) / 2;
        child.setRect(new Rect(x, y, childW, childH));
        return true;
    }

    @Override
    public int minimumHeight() {
        return Math.max(super.minimumHeight(),
                children().stream().mapToInt(Widget::minimumHeight).max().orElse(0));
    }

    @Override
    public int minimumWidth() {
        return Math.max(super.minimumWidth(),
                children().stream().mapToInt(Widget::minimumWidth).max().orElse(0));
    }

    @Override
    public AbstractWidget visualCopy(AbstractContainer newParent) {
        CenterContainer copy = new CenterContainer.Builder(newParent).build();
        copy.copyVisualProperties(this);
        visualCopyChildrenInto(copy);
        return copy;
    }

    protected static abstract class CenterBuilder<T extends CenterContainer.CenterBuilder<T>>
            extends AbstractContainerBuilder<T> {

        public CenterBuilder(AbstractContainer container) {
            super(container);
        }

        public CenterBuilder(GuiManager manager) {
            super(manager);
        }
    }

    public static class Builder extends CenterContainer.CenterBuilder<CenterContainer.Builder> {
        public Builder(AbstractContainer container) {
            super(container);
        }

        public Builder(GuiManager manager) {
            super(manager);
        }

        @Override
        public CenterContainer build() {
            return new CenterContainer(this);
        }

        @Override
        public AbstractContainer push() {
            return parent().add(this);
        }

        @Override
        protected CenterContainer.Builder self() {
            return this;
        }

        @Override
        public CenterContainer pushAndReturn() {
            CenterContainer c = new CenterContainer(this);
            parent().add(c);
            return c;
        }
    }
}
