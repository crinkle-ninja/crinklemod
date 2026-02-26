package ninja.crinkle.mod.client.gui.widgets;

import ninja.crinkle.mod.client.gui.layouts.BoxModel;
import ninja.crinkle.mod.client.gui.properties.Box;
import ninja.crinkle.mod.client.gui.properties.Point;
import ninja.crinkle.mod.client.gui.properties.Position;
import ninja.crinkle.mod.client.gui.renderers.ThemeGraphics;
import ninja.crinkle.mod.client.gui.states.WidgetLayout;

public interface Widget {
    WidgetLayout layout();
    String name();
    void renderContent(ThemeGraphics pGuiGraphics, Point pMouse, Box renderBox, float pPartialTick);
    void renderDebug(ThemeGraphics pGuiGraphics);
}
