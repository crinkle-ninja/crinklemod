package ninja.crinkle.mod.client.gui.screens;

import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.navigation.ScreenDirection;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import ninja.crinkle.mod.client.gui.managers.DragManager;
import ninja.crinkle.mod.client.gui.managers.FocusManager;
import ninja.crinkle.mod.client.gui.managers.GuiManager;
import ninja.crinkle.mod.client.gui.managers.IManagedGUI;
import ninja.crinkle.mod.client.gui.mixins.ManagedScreen;
import ninja.crinkle.mod.client.gui.properties.ClickState;
import ninja.crinkle.mod.client.gui.properties.Point;
import ninja.crinkle.mod.client.gui.properties.Rect;
import ninja.crinkle.mod.client.gui.widgets.AbstractContainer;
import ninja.crinkle.mod.client.gui.widgets.Container;
import ninja.crinkle.mod.client.gui.widgets.Widget;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public abstract class AbstractScreen extends Screen implements ManagedScreen {
    private final DragManager dragManager = new DragManager();
    private final FocusManager focusManager = new FocusManager();
    private final AbstractContainer root;
    private final List<Widget> widgetCache = new ArrayList<>();
    protected boolean ready = false;
    private ClickState clickState;
    private int currentTabIndex = 0;
    private Widget hovered = null;
    private Rect lastScreenRect;
    private Point mouse = Point.ZERO;

    protected AbstractScreen(Component pTitle, int screenWidth, int screenHeight) {
        super(pTitle);
        this.width = screenWidth;
        this.height = screenHeight;
        this.root = new Container.Builder(this)
                .name("root")
                .minSize(screenWidth, screenHeight)
                .build();
        this.root.setRect(new Rect(0, 0, screenWidth, screenHeight));
        clickState = new ClickState(mouse(), 0, System.currentTimeMillis(), null);
    }

    @Override
    public DragManager dragManager() {
        return dragManager;
    }

    @Override
    public FocusManager focusManager() {
        return focusManager;
    }

    @Override
    public AbstractContainer root() {
        return root;
    }

    @Override
    public Rect size() {
        return new Rect(0, 0, width, height);
    }

    public Widget hovered() {
        return hovered;
    }

    public void hovered(Widget hovered) {
        this.hovered = hovered;
    }

    public boolean notReady() {
        return !ready;
    }

    public List<Widget> widgetCache() {
        return widgetCache;
    }

    public Point mouse() {
        return mouse;
    }

    @Override public void clickState(ClickState clickState) {
        this.clickState = clickState;
    }

    @Override public ClickState clickState() {
        return clickState;
    }

    @Override public void mouse(Point mouse) {
        this.mouse = mouse;
    }

    @Override
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        return onKeyPressed(pKeyCode, pScanCode, pModifiers) || super.keyPressed(pKeyCode, pScanCode, pModifiers);
    }

    @Override
    public void onClose() {
        super.onClose();
    }

    @Override
    public void init() {
        super.init();
        // Re-add root as a renderable — clearWidgets() removes it on resize
        addRenderableOnly(root());
        root().setRect(new Rect(0, 0, width, height));
        root().init();
        registerLayoutEntries();
        resolveLayoutPositions();
        ready = true;
    }

    @Override
    public void tick() {
        Rect currentRect = new Rect(0, 0, width, height);
        if (lastScreenRect != null && !lastScreenRect.equals(currentRect)) {
            // we've resized the screen
            resolveLayoutPositions();
        }
        lastScreenRect = currentRect;
        super.tick();
        root().tick();
    }

    @Override
    public GuiManager manager() {
        return this;
    }

    public IManagedGUI gui() {
        return this;
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        return onMouseClicked(pMouseX, pMouseY, pButton) || super.mouseClicked(pMouseX, pMouseY, pButton);
    }

    @Override
    public boolean mouseReleased(double pMouseX, double pMouseY, int pButton) {
        return onMouseReleased(pMouseX, pMouseY, pButton) || super.mouseReleased(pMouseX, pMouseY, pButton);
    }

    @Override
    public boolean mouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY) {
        return onMouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY) || super.mouseDragged(pMouseX, pMouseY,
                pButton, pDragX, pDragY);
    }

    @Override
    public boolean mouseScrolled(double pMouseX, double pMouseY, double pDelta) {
        return onMouseScrolled(pMouseX, pMouseY, pDelta) || super.mouseScrolled(pMouseX, pMouseY, pDelta);
    }

    @Override
    public boolean keyReleased(int pKeyCode, int pScanCode, int pModifiers) {
        return onKeyReleased(pKeyCode, pScanCode, pModifiers) || super.keyReleased(pKeyCode, pScanCode, pModifiers);
    }

    @Override
    public boolean charTyped(char pCodePoint, int pModifiers) {
        return onCharTyped(pCodePoint, pModifiers) || super.charTyped(pCodePoint, pModifiers);
    }

    @Override
    public @Nullable ComponentPath getCurrentFocusPath() {
        return ComponentPath.leaf(this);
    }

    @Override
    public @Nullable ComponentPath nextFocusPath(@NotNull FocusNavigationEvent pEvent) {
        List<Widget> focusElements = widgets().stream()
                .filter(Widget::focusable)
                .sorted(Comparator.comparingInt(Widget::tabIndex))
                .toList();
        if (focusElements.isEmpty()) {
            return ComponentPath.leaf(this);
        }
        if (pEvent.getVerticalDirectionForInitialFocus() == ScreenDirection.DOWN) {
            currentTabIndex = (currentTabIndex + 1) % focusElements.size();
        } else {
            currentTabIndex = (currentTabIndex - 1 + focusElements.size()) % focusElements.size();
        }
        return ComponentPath.leaf(focusElements.get(currentTabIndex));
    }

    @Override
    public void mouseMoved(double pMouseX, double pMouseY) {
        if (!onMouseMoved(pMouseX, pMouseY)) {
            super.mouseMoved(pMouseX, pMouseY);
        }
    }

    @Override
    public String toString() {
        return "AbstractScreen{root=" + root +
                ", currentTabIndex=" + currentTabIndex +
                '}';
    }
}