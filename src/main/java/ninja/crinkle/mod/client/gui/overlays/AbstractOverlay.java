package ninja.crinkle.mod.client.gui.overlays;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import ninja.crinkle.mod.client.gui.managers.GuiManager;
import ninja.crinkle.mod.client.gui.properties.Point;
import ninja.crinkle.mod.client.gui.renderers.ThemeGraphics;
import ninja.crinkle.mod.client.gui.textures.ThemeAtlas;

public class AbstractOverlay implements IGuiOverlay {
    private final GuiManager manager;

    public AbstractOverlay(GuiManager manager) {
        this.manager = manager;
    }

    public AbstractOverlay() {
        this(GuiManager.create());
    }

    public GuiManager manager() {
        return manager;
    }

    @Override
    public void render(ForgeGui gui, GuiGraphics guiGraphics, float partialTick, int screenWidth, int screenHeight) {
        ThemeGraphics themeGraphics = new ThemeGraphics(guiGraphics, ThemeAtlas.getAtlas());
        Point point = Point.of(screenWidth / 2, screenHeight / 2);
        manager().root().render(themeGraphics, point, partialTick);
    }
}
