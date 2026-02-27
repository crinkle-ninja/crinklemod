package ninja.crinkle.mod.client.gui.addons;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import ninja.crinkle.mod.client.gui.properties.Point;
import ninja.crinkle.mod.client.gui.renderers.ThemeGraphics;
import ninja.crinkle.mod.client.gui.screens.AbstractScreen;
import ninja.crinkle.mod.client.gui.textures.ThemeAtlas;
import ninja.crinkle.mod.client.gui.themes.Theme;
import ninja.crinkle.mod.client.gui.themes.ThemeRegistry;
import ninja.crinkle.mod.util.ClientUtil;

public abstract class AbstractAddOn extends AbstractScreen {
    private final Class<? extends Screen> screenClass;

    protected AbstractAddOn(Class<? extends Screen> screenClass) {
        super(Component.literal(screenClass.getSimpleName() + "AddOn"), ClientUtil.screenWidth(), ClientUtil.screenHeight());
        this.screenClass = screenClass;
        this.root().active(false);
        this.root().visible(false);
    }

    @SubscribeEvent
    public void onScreenTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (ClientUtil.getMinecraft() == null || ClientUtil.getMinecraft().screen == null) return;
        if (screenClass != ClientUtil.getMinecraft().screen.getClass()) return;
        tick();
    }

    @SubscribeEvent
    public void onScreenOpening(ScreenEvent.Opening event) {
        if (event.isCanceled()) return;
        if (ClientUtil.getMinecraft() == null) return;
        if (screenClass != event.getScreen().getClass()) return;
        root().visible(true);
        root().active(true);
        root().init();
    }

    @SubscribeEvent
    public void onScreenClosing(ScreenEvent.Closing event) {
        if (event.isCanceled()) return;
        if (ClientUtil.getMinecraft() == null || ClientUtil.getMinecraft().screen == null) return;
        if (screenClass != event.getScreen().getClass()) return;
        root().visible(false);
        root().active(false);
    }

    @SubscribeEvent
    public void onScreenRenderPost(ScreenEvent.Render.Post event) {
        if (event.isCanceled()) return;
        if (!root().active() || !root().visible()) return;
        if (ClientUtil.getMinecraft() == null || ClientUtil.getMinecraft().screen == null) return;
        if (screenClass != event.getScreen().getClass()) return;
        ThemeGraphics themeGraphics = new ThemeGraphics(event.getGuiGraphics(), ThemeAtlas.getAtlas());
        root().render(themeGraphics, Point.of(event.getMouseX(), event.getMouseY()), event.getPartialTick());
    }

    @SubscribeEvent
    public void onScreenMouseClickedPre(InputEvent.MouseButton.Pre event) {
        if (!root().active()) return;
        if (event.isCanceled()) return;
        if (ClientUtil.getMinecraft() == null) return;
        if (ClientUtil.getMinecraft().screen == null || ClientUtil.getMinecraft().screen.getClass() != screenClass) return;
        boolean handled = root().mouseClicked(ClientUtil.getMinecraft().mouseHandler.xpos(),
                ClientUtil.getMinecraft().mouseHandler.ypos(),
                event.getButton());
        if (handled) {
            event.setCanceled(true);
        }
    }

    public Class<? extends Screen> screenClass() {
        return screenClass;
    }

    @Override
    public String name() {
        return screenClass.getSimpleName() + "AddOn";
    }
}
