package ninja.crinkle.mod.client.gui.containers.screens;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import ninja.crinkle.mod.client.gui.containers.menus.ThemedContainerMenu;
import ninja.crinkle.mod.client.gui.managers.DragManager;
import ninja.crinkle.mod.client.gui.managers.FocusManager;
import ninja.crinkle.mod.client.gui.managers.GuiManager;
import ninja.crinkle.mod.client.gui.managers.IManagedGUI;
import ninja.crinkle.mod.client.gui.mixins.ManagedScreen;
import ninja.crinkle.mod.client.gui.properties.ClickState;
import ninja.crinkle.mod.client.gui.properties.Point;
import ninja.crinkle.mod.client.gui.properties.Rect;
import ninja.crinkle.mod.client.gui.renderers.ThemeGraphics;
import ninja.crinkle.mod.client.gui.textures.ThemeAtlas;
import ninja.crinkle.mod.client.gui.themes.ThemeRegistry;
import ninja.crinkle.mod.client.gui.widgets.AbstractContainer;
import ninja.crinkle.mod.client.gui.widgets.Container;
import ninja.crinkle.mod.client.gui.widgets.Widget;
import ninja.crinkle.mod.util.ClientUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public abstract class ThemedContainerScreen<T extends ThemedContainerMenu>
        extends AbstractContainerScreen<T> implements ManagedScreen {
    private final DragManager dragManager = new DragManager();
    private final FocusManager focusManager = new FocusManager();
    private final Container root;
    private ClickState clickState;
    private Widget hovered;
    private Point mouse = Point.ZERO;
    private boolean ready;
    private final List<Widget> widgetCache = new ArrayList<>();

    public ThemedContainerScreen(T pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
        this.root = new Container.Builder(this)
                .name("root")
                .minSize(ClientUtil.screenWidth(), ClientUtil.screenHeight())
                .build();
    }

    @Override public DragManager dragManager() {
        return dragManager;
    }

    @Override public FocusManager focusManager() {
        return focusManager;
    }

    @Override public GuiManager manager() {
        return this;
    }

    @Override public IManagedGUI gui() {
        return this;
    }

    @Override public int width() {
        return width;
    }

    @Override public int height() {
        return height;
    }

    @Override public AbstractContainer root() {
        return root;
    }

    @Override public Rect size() {
        return root().rect();
    }

    @Override public Widget hovered() {
        return hovered;
    }

    @Override public void hovered(Widget hovered) {
        this.hovered = hovered;
    }

    @Override public boolean notReady() {
        return !this.ready;
    }

    @Override public List<Widget> widgetCache() {
        return widgetCache;
    }

    @Override public Point mouse() {
        return mouse;
    }

    @Override public void clickState(ClickState clickState) {
        this.clickState = clickState;
    }

    @Override public ClickState clickState() {
        return clickState;
    }

    @Override public void mouse(Point point) {
        this.mouse = point;
    }

    @Override protected void init() {
        super.init();
        addRenderableOnly(root());
        root().setRect(new Rect(0, 0, width(), height()));
        root().init();
        registerLayoutEntries();
        resolveLayoutPositions();
        ready = true;
    }

    @Override protected void renderBg(@NotNull GuiGraphics pGuiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
        if (notReady()) {
            init();
        }
        ThemeGraphics themeGraphics = new ThemeGraphics(pGuiGraphics, ThemeAtlas.getAtlas(), ThemeRegistry.current());
        Point point = Point.of(pMouseX, pMouseY);
        manager().root().render(themeGraphics, point, pPartialTick);
    }
}
