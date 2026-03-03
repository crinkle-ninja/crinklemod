package ninja.crinkle.mod.client.gui.addons;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import com.mojang.logging.LogUtils;
import ninja.crinkle.mod.client.ClientSetup;
import ninja.crinkle.mod.client.gui.events.FocusLeftEvent;
import ninja.crinkle.mod.client.gui.events.MoveEvent;
import ninja.crinkle.mod.client.gui.properties.Point;
import ninja.crinkle.mod.client.gui.properties.Rect;
import ninja.crinkle.mod.client.gui.properties.Scope;
import ninja.crinkle.mod.client.gui.renderers.ThemeGraphics;
import ninja.crinkle.mod.client.gui.screens.AbstractScreen;
import ninja.crinkle.mod.client.gui.screens.LayoutEditorScreen;
import ninja.crinkle.mod.client.gui.textures.ThemeAtlas;
import ninja.crinkle.mod.client.gui.themes.Theme;
import ninja.crinkle.mod.client.gui.themes.ThemeRegistry;
import ninja.crinkle.mod.util.ClientUtil;
import org.slf4j.Logger;

public abstract class AbstractAddOn extends AbstractScreen {
    private static final Logger LOGGER = LogUtils.getLogger();
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
        if (screenClass() != ClientUtil.getMinecraft().screen.getClass()) return;
        int currentWidth = ClientUtil.screenWidth();
        int currentHeight = ClientUtil.screenHeight();
        if (currentWidth != this.width || currentHeight != this.height) {
            this.width = currentWidth;
            this.height = currentHeight;
            root().setRect(new Rect(0, 0, width, height));
            root().init();
        }
        tick();
    }

    @SubscribeEvent
    public void onScreenOpening(ScreenEvent.Opening event) {
        if (event.isCanceled()) return;
        if (ClientUtil.getMinecraft() == null) return;
        if (screenClass() != event.getScreen().getClass()) return;
        root().visible(true);
        root().active(true);
        this.width = ClientUtil.screenWidth();
        this.height = ClientUtil.screenHeight();
        root().setRect(new Rect(0, 0, width, height));
        root().init();
        registerLayoutEntries();
        resolveLayoutPositions();
        ready = true;
    }

    @SubscribeEvent
    public void onScreenClosing(ScreenEvent.Closing event) {
        if (event.isCanceled()) return;
        if (ClientUtil.getMinecraft() == null || ClientUtil.getMinecraft().screen == null) return;
        if (screenClass() != event.getScreen().getClass()) return;
        root().visible(false);
        root().active(false);
        // Feels hacky, but clears hovered states
        mouseMoved(-1, -1);
    }

    @SubscribeEvent
    public void onScreenRenderPost(ScreenEvent.Render.Post event) {
        if (event.isCanceled()) return;
        if (!root().active() || !root().visible()) return;
        if (ClientUtil.getMinecraft() == null || ClientUtil.getMinecraft().screen == null) return;
        if (screenClass() != event.getScreen().getClass()) return;
        mouseMoved(event.getMouseX(), event.getMouseY());
        ThemeGraphics themeGraphics = new ThemeGraphics(event.getGuiGraphics(), ThemeAtlas.getAtlas());
        root().render(themeGraphics, Point.of(event.getMouseX(), event.getMouseY()), event.getPartialTick());
    }

    @SubscribeEvent
    public void onScreenMouseClickedPre(InputEvent.MouseButton.Pre event) {
        if (!root().active()) return;
        if (event.isCanceled()) return;
        if (ClientUtil.getMinecraft() == null) return;
        if (ClientUtil.getMinecraft().screen == null || ClientUtil.getMinecraft().screen.getClass() != screenClass()) return;
        var window = ClientUtil.getMinecraft().getWindow();
        double mouseX = ClientUtil.getMinecraft().mouseHandler.xpos()
                * (double) window.getGuiScaledWidth() / (double) window.getScreenWidth();
        double mouseY = ClientUtil.getMinecraft().mouseHandler.ypos()
                * (double) window.getGuiScaledHeight() / (double) window.getScreenHeight();
        boolean handled;
        if (event.getAction() == InputConstants.PRESS) {
            handled = mouseClicked(mouseX, mouseY, event.getButton());
        } else if (event.getAction() == InputConstants.RELEASE) {
            handled = mouseReleased(mouseX, mouseY, event.getButton());
        } else {
            return;
        }
        if (handled) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onScreenKeyPressed(ScreenEvent.KeyPressed.Pre event) {
        if (!root().active()) return;
        if (ClientUtil.getMinecraft() == null || ClientUtil.getMinecraft().screen == null) return;
        if (screenClass() != ClientUtil.getMinecraft().screen.getClass()) return;
        if (ClientSetup.LAYOUT_EDITOR_KEY != null
                && ClientSetup.LAYOUT_EDITOR_KEY.matches(event.getKeyCode(), event.getScanCode())) {
            ClientUtil.getMinecraft().setScreen(new LayoutEditorScreen(this));
            event.setCanceled(true);
        }
    }

    public Class<? extends Screen> screenClass() {
        return screenClass;
    }

    @Override
    public String name() {
        return screenClass().getSimpleName() + "AddOn";
    }
}
