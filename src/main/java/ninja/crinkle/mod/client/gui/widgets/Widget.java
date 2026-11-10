package ninja.crinkle.mod.client.gui.widgets;

import net.minecraft.client.gui.components.events.GuiEventListener;
import ninja.crinkle.mod.client.gui.properties.Point;
import ninja.crinkle.mod.client.gui.properties.Rect;
import ninja.crinkle.mod.client.gui.properties.Sizing;
import ninja.crinkle.mod.client.gui.renderers.ThemeGraphics;
import ninja.crinkle.mod.client.gui.renderers.ThemeRenderable;

import java.util.EnumSet;
import java.util.Optional;

public interface Widget extends ThemeRenderable, GuiEventListener {
    boolean active();

    void active(boolean b);

    boolean draggable();

    void draggable(boolean b);

    void dragged(boolean b);

    boolean focusable();

    boolean focused();

    void focused(boolean b);

    EnumSet<Sizing> horizontalSizing();

    boolean hoverable();

    void hovered(boolean b);

    void init();

    String name();

    Optional<AbstractContainer> parent();

    void parent(AbstractContainer parent);

    boolean pressable();

    void pressable(boolean b);

    void pressed(boolean b);

    boolean pressed();

    Rect rect();

    void renderContent(ThemeGraphics pGuiGraphics, Point pMouse, Rect renderRect, float pPartialTick);

    void renderDebug(ThemeGraphics pGuiGraphics);
    int minimumHeight();

    int minimumWidth();

    void renderTooltip(ThemeGraphics graphics, Point pMouse);

    boolean scrollable();

    void setRect(Rect fitted);

    float stretchRatio();

    int tabIndex();

    void tick();

    EnumSet<Sizing> verticalSizing();

    boolean visible();

    void visible(boolean b);

    Widget visualCopy(AbstractContainer target);

    void zIndex(int i);

    int zIndex();
}
