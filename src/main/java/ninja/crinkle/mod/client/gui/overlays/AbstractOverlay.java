package ninja.crinkle.mod.client.gui.overlays;

import com.mojang.logging.LogUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import ninja.crinkle.mod.client.gui.editors.LayoutRegistry;
import ninja.crinkle.mod.client.gui.managers.GuiManager;
import ninja.crinkle.mod.client.gui.managers.IManagedGUI;
import ninja.crinkle.mod.client.gui.properties.Point;
import ninja.crinkle.mod.client.gui.properties.Rect;
import ninja.crinkle.mod.client.gui.renderers.ThemeGraphics;
import ninja.crinkle.mod.client.gui.textures.ThemeAtlas;
import ninja.crinkle.mod.client.gui.widgets.AbstractWidget;
import org.slf4j.Logger;

public abstract class AbstractOverlay implements IGuiOverlay, IManagedGUI {
    public static final Logger LOGGER = LogUtils.getLogger();
    private boolean ready = false;
    private final GuiManager manager;
    private int lastScreenWidth;
    private int lastScreenHeight;

    public AbstractOverlay(GuiManager manager) {
        this.manager = manager;
    }

    public AbstractOverlay() {
        this(GuiManager.create());
    }

    public void init() {
        registerLayoutEntries();
        resolveLayoutPositions();
        ready = true;
    }

    public IManagedGUI gui() {
        return this;
    }

    public GuiManager manager() {
        return manager;
    }

    public int width() {
        return lastScreenWidth;
    }

    public int height() {
        return lastScreenHeight;
    }

    @Override
    public void render(ForgeGui gui, GuiGraphics guiGraphics, float partialTick, int screenWidth, int screenHeight) {
        if (screenWidth != lastScreenWidth || screenHeight != lastScreenHeight) {
            lastScreenWidth = screenWidth;
            lastScreenHeight = screenHeight;
            if (ready) {
                onScreenResize();
            }
        }
        if (!ready) {
            init();
        }
        ThemeGraphics themeGraphics = new ThemeGraphics(guiGraphics, ThemeAtlas.getAtlas());
        Point point = Point.of(screenWidth / 2, screenHeight / 2);
        manager().root().render(themeGraphics, point, partialTick);
    }

    protected void onScreenResize() {
        resolveLayoutPositions();
    }
}
