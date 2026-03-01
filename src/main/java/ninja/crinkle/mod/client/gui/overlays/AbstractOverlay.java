package ninja.crinkle.mod.client.gui.overlays;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import ninja.crinkle.mod.client.gui.editors.LayoutRegistry;
import ninja.crinkle.mod.client.gui.managers.GuiManager;
import ninja.crinkle.mod.client.gui.managers.IManagedGUI;
import ninja.crinkle.mod.client.gui.properties.Point;
import ninja.crinkle.mod.client.gui.properties.Rect;
import ninja.crinkle.mod.client.gui.renderers.ThemeGraphics;
import ninja.crinkle.mod.client.gui.textures.ThemeAtlas;
import ninja.crinkle.mod.client.gui.widgets.AbstractWidget;

public abstract class AbstractOverlay implements IGuiOverlay, IManagedGUI {
    private final GuiManager manager;
    private int lastScreenWidth;
    private int lastScreenHeight;

    public AbstractOverlay(GuiManager manager) {
        this.manager = manager;
    }

    public AbstractOverlay() {
        this(GuiManager.create());
    }

    public GuiManager manager() {
        return manager;
    }

    public int screenWidth() {
        return lastScreenWidth;
    }

    public int screenHeight() {
        return lastScreenHeight;
    }

    @Override
    public void render(ForgeGui gui, GuiGraphics guiGraphics, float partialTick, int screenWidth, int screenHeight) {
        if (screenWidth != lastScreenWidth || screenHeight != lastScreenHeight) {
            lastScreenWidth = screenWidth;
            lastScreenHeight = screenHeight;
            onScreenResize(screenWidth, screenHeight);
        }
        ThemeGraphics themeGraphics = new ThemeGraphics(guiGraphics, ThemeAtlas.getAtlas());
        Point point = Point.of(screenWidth / 2, screenHeight / 2);
        manager().root().render(themeGraphics, point, partialTick);
    }

    protected abstract void onScreenResize(int screenWidth, int screenHeight);
}
