package ninja.crinkle.mod.client.gui.renderers;

import ninja.crinkle.mod.client.gui.properties.Point;
import org.jetbrains.annotations.NotNull;

public interface ThemeRenderable {
    void render(@NotNull ThemeGraphics graphics, Point pMouse, float pPartialTick);
}
