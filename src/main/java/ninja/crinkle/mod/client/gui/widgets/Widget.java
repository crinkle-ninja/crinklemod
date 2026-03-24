package ninja.crinkle.mod.client.gui.widgets;

import ninja.crinkle.mod.client.gui.properties.Point;
import ninja.crinkle.mod.client.gui.properties.Rect;
import ninja.crinkle.mod.client.gui.renderers.ThemeGraphics;

public interface Widget {
    String name();

    Rect rect();

    void renderContent(ThemeGraphics pGuiGraphics, Point pMouse, Rect renderRect, float pPartialTick);

    void renderDebug(ThemeGraphics pGuiGraphics);
}
