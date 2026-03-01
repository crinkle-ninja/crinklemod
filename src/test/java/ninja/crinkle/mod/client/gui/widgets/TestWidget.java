package ninja.crinkle.mod.client.gui.widgets;

import ninja.crinkle.mod.client.gui.managers.GuiManager;
import ninja.crinkle.mod.client.gui.properties.Point;
import ninja.crinkle.mod.client.gui.properties.Rect;
import ninja.crinkle.mod.client.gui.renderers.ThemeGraphics;

public class TestWidget extends AbstractWidget {
    public TestWidget(Builder builder) {
        super(builder);
    }

    @Override
    public void renderContent(ThemeGraphics graphics, Point pMouse, Rect renderedRect, float pPartialTick) {
        // Nothing to render for testing
    }

    public static class Builder extends AbstractBuilder<Builder> {
        protected Builder(GuiManager manager) {
            super(manager);
        }

        protected Builder(AbstractContainer parent) {
            super(parent);
        }

        @Override
        public TestWidget build() {
            return new TestWidget(this);
        }

        @Override
        public AbstractContainer push() {
            if (parent() != null) {
                parent().add(this);
            }
            return parent();
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}
