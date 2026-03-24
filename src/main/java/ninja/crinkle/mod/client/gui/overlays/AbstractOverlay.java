package ninja.crinkle.mod.client.gui.overlays;

import com.mojang.logging.LogUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import ninja.crinkle.mod.client.gui.managers.GuiManager;
import ninja.crinkle.mod.client.gui.managers.IManagedGUI;
import ninja.crinkle.mod.client.gui.properties.Point;
import ninja.crinkle.mod.client.gui.renderers.ThemeGraphics;
import ninja.crinkle.mod.client.gui.textures.ThemeAtlas;
import org.slf4j.Logger;

public abstract class AbstractOverlay implements IGuiOverlay, IManagedGUI {
    public static final Logger LOGGER = LogUtils.getLogger();
    private final GuiManager manager;
    private int lastScreenHeight;
    private int lastScreenWidth;
    private boolean ready = false;

    public AbstractOverlay() {
        this(GuiManager.create());
    }

    public AbstractOverlay(GuiManager manager) {
        this.manager = manager;
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

    public void init() {
        registerLayoutEntries();
        resolveLayoutPositions();
        ready = true;
    }

    public GuiManager manager() {
        return manager;
    }

    public IManagedGUI gui() {
        return this;
    }

    public int width() {
        return lastScreenWidth;
    }

    public int height() {
        return lastScreenHeight;
    }
}
