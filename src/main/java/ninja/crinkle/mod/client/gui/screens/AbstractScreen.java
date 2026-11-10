package ninja.crinkle.mod.client.gui.screens;

import com.mojang.logging.LogUtils;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.navigation.ScreenDirection;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import ninja.crinkle.mod.client.gui.actors.*;
import ninja.crinkle.mod.client.gui.managers.*;
import ninja.crinkle.mod.client.gui.properties.ClickState;
import ninja.crinkle.mod.client.gui.properties.Point;
import ninja.crinkle.mod.client.gui.properties.Rect;
import ninja.crinkle.mod.client.gui.widgets.AbstractContainer;
import ninja.crinkle.mod.client.gui.widgets.Widget;
import ninja.crinkle.mod.client.gui.widgets.Container;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public abstract class AbstractScreen extends Screen implements GuiManager, IManagedGUI {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int DOUBLE_CLICK_OFFSET = 2;
    private static final int DOUBLE_CLICK_TIME = 300;
    private final DragManager dragManager = new DragManager();
    private final FocusManager focusManager = new FocusManager();
    private final AbstractContainer root;
    protected boolean ready = false;
    private ClickState clickState;
    private int currentTabIndex = 0;
    private Rect lastScreenRect;
    private Point mouse = Point.ZERO;
    private Widget hovered = null;
    private final List<Widget> widgetCache = new ArrayList<>();

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

    public Widget hovered() {
        return hovered;
    }

    public void hovered(Widget hovered) {
        this.hovered = hovered;
    }

    abstract public String name();

    @Override
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        if (notReady()) {
            return super.keyPressed(pKeyCode, pScanCode, pModifiers);
        }
        Widget widget = focusedWidget();
        if (widget != null)
            LOGGER.trace("keyPressed on {}", widget.name());
        if (widget instanceof KeyActor action) {
            return action.onKeyPressed(pKeyCode, pScanCode, pModifiers);
        }
        return super.keyPressed(pKeyCode, pScanCode, pModifiers);
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

    public boolean notReady() {
        return !ready;
    }

    @Override
    public GuiManager manager() {
        return this;
    }

    public List<Widget> widgetCache() {
        return widgetCache;
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    public IManagedGUI gui() {
        return this;
    }

    public Point mouse() {
        return mouse;
    }

    private List<Widget> widgets() {
        if (!ready) return List.of();
        if (!widgetCache().isEmpty()) return List.copyOf(widgetCache());
        LOGGER.trace("Building widget cache...");
        widgetCache().clear();
        widgetCache().addAll(widgets(root()));
        LOGGER.trace("Widgets cached: {}", widgetCache().size());
        return List.copyOf(widgetCache());
    }

    private List<Widget> widgets(AbstractContainer parent) {
        List<Widget> widgets = new java.util.ArrayList<>();
        widgets.add(parent);
        for(Widget widget : parent.children().stream().toList()) {
            if (widget instanceof AbstractContainer container) {
                widgets.addAll(widgets(container));
            } else {
                widgets.add(widget);
            }
        }
        return widgets;
    }

    private Widget hoveredWidget(Point point) {
        return widgets().stream()
                .filter(Widget::visible)
                .filter(Widget::hoverable)
                .filter(w -> w.rect().contains(point))
                .max(Comparator.comparingInt(Widget::zIndex))
                .orElse(null);
    }

    private Widget clickedWidget(Point point) {
        return widgets().stream()
                .filter(Widget::active)
                .filter(Widget::visible)
                .filter(Widget::pressable)
                .filter(w -> w.rect().contains(point))
                .max(Comparator.comparingInt(Widget::zIndex))
                .orElse(null);
    }

    private Widget focusableWidget(Point point) {
        return widgets().stream()
                .filter(Widget::active)
                .filter(Widget::visible)
                .filter(Widget::focusable)
                .filter(w -> w.rect().contains(point))
                .max(Comparator.comparingInt(Widget::zIndex))
                .orElse(null);
    }

    private Widget focusedWidget() {
        return widgets().stream()
                .filter(Widget::active)
                .filter(Widget::visible)
                .filter(Widget::focused)
                .max(Comparator.comparingInt(Widget::zIndex))
                .orElse(null);
    }

    private Widget scrollableWidget(Point point) {
        return widgets().stream()
                .filter(Widget::active)
                .filter(Widget::visible)
                .filter(Widget::scrollable)
                .filter(w -> w.rect().contains(point))
                .max(Comparator.comparingInt(Widget::zIndex))
                .orElse(null);
    }

    private Widget draggableWidget(Point point) {
        return widgets().stream()
                .filter(Widget::active)
                .filter(Widget::visible)
                .filter(Widget::draggable)
                .filter(w -> w.rect().contains(point))
                .min(Comparator.comparingInt(Widget::zIndex))
                .orElse(null);
    }

    // mouse clicked handlers
    private boolean handlePressed(Widget widget, Point point, int pButton) {
        if (widget == null || !widget.pressable()) return false;
        widget.pressed(true);
        if (widget instanceof MouseActor action) {
            return action.onPressed(point, MouseButton.fromInt(pButton));
        }
        return false;
    }

    private boolean handleDoubleClick(Widget widget, Point point, int pButton) {
        if (widget instanceof MouseActor action) {
            return action.onDoubleClick(point, MouseButton.fromInt(pButton));
        }
        return false;
    }

    private void handleFocusLeft(Widget focusable) {
        if (focusable instanceof FocusActor action) {
            action.onBlur();
        }
    }

    private void handleFocusEntered(Widget focusable) {
        if (focusable instanceof FocusActor action) {
            action.onFocus();
        }
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        if (notReady()) {
            return super.mouseClicked(pMouseX, pMouseY, pButton);
        }
        ClickState state = clickState;
        Point point = new Point(pMouseX, pMouseY);
        Widget topMost = clickedWidget(point);
        if (topMost != null)
            LOGGER.trace("mouseClicked on {}, zIndex: {}, {}", topMost.name(), topMost.zIndex(), topMost);
        // double click
        if (topMost != null && state.button() == pButton && System.currentTimeMillis() - state.clickTime() < DOUBLE_CLICK_TIME
                && state.position().distance(pMouseX, pMouseY) < DOUBLE_CLICK_OFFSET) {
            return handleDoubleClick(topMost, point, pButton);
        }

        clickState = new ClickState(point, pButton, System.currentTimeMillis(), topMost);

        // drag
        Widget dragTarget = draggableWidget(point);
        if (dragTarget != null) {
            dragManager().current(dragTarget);
            dragManager().dragging(false);
        }

        // focus
        Widget topFocusable = focusableWidget(point);
//        LOGGER.trace("mouseClicked:focus on {}, zIndex: {}, calculated zIndex: {}", topFocusable.name(), topFocusable.zIndex(), Widget.zIndexOf(topFocusable));
        if ((topFocusable == null || topFocusable != focusManager().currentFocus()) && focusManager().currentFocus() != null) {
            handleFocusLeft(focusManager().currentFocus());
        }
        if (topFocusable != null && topFocusable.focusable()) {
            handleFocusEntered(topFocusable);
        }
        focusManager().currentFocus(topFocusable);

        // pressed
        return handlePressed(topFocusable, point, pButton) || super.mouseClicked(pMouseX, pMouseY, pButton);
    }

    // mouse released handlers
    private void handleDragStopped(Widget widget, Point point) {
        if (widget instanceof DragActor action) {
            action.onDragStopped(widget, point);
        }
    }

    private boolean handleClicked(Widget widget, Point point, int pButton) {
        if (widget instanceof MouseActor action) {
            return action.onClick(point, MouseButton.fromInt(pButton));
        }
        return false;
    }

    private boolean handleReleased(Widget widget, Point point, int pButton) {
        if (widget == null || !widget.pressed()) return false;
        widget.pressed(false);
        if (widget instanceof MouseActor action) {
            return action.onReleased(point, MouseButton.fromInt(pButton));
        }
        return false;
    }


    @Override
    public boolean mouseReleased(double pMouseX, double pMouseY, int pButton) {
        if (notReady()) {
            return super.mouseReleased(pMouseX, pMouseY, pButton);
        }
        Point point = new Point(pMouseX, pMouseY);
        // drag
        if (dragManager().current() != null) {
            if (dragManager().dragging()) {
                Widget dropped = dragManager().current();
                dragManager().drop();
                handleDragStopped(dropped, point);
            }
            dragManager().current(null);
        }
        Widget topMost = clickedWidget(point);
        if (topMost != null)
            LOGGER.trace("mouseReleased on {}, zIndex: {}", topMost.name(), topMost.zIndex());

        // released & clicked
        boolean result = false;
        ClickState state = clickState;
        if (topMost != null && state.button() == pButton && topMost == state.widget())
            result = handleClicked(topMost, point, pButton);
        return (state.widget() != null && handleReleased(state.widget(), point, pButton)) || result || super.mouseReleased(pMouseX, pMouseY, pButton);
    }

    // mouse dragged handlers
    private boolean handleDragStarted(Widget widget, Point mousePoint, Point dragPoint, int button) {
        if (widget instanceof DragActor action) {
            return action.onDragStarted(widget, mousePoint, dragPoint, button);
        }
        return false;
    }

    private boolean handleDrag(Widget widget, Point mousePoint, Point dragPoint, int button) {
        if (widget instanceof DragActor action) {
            return action.onDrag(widget, mousePoint, dragPoint, button);
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY) {
        if (notReady()) {
            return super.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY);
        }
        Point mousePoint = new Point(pMouseX, pMouseY);
        Point dragPoint = new Point(pDragX, pDragY);
        Widget topMost = draggableWidget(mousePoint);
        if (dragManager().current() != null && !dragManager().dragging()) {
            dragManager().startDragging(topMost);
            return handleDragStarted(topMost, mousePoint, dragPoint, pButton);
        } else {
            dragManager().dragged(dragPoint);
        }
        return handleDrag(topMost, mousePoint, dragPoint, pButton) || super.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY);
    }

    @Override
    public boolean mouseScrolled(double pMouseX, double pMouseY, double pDelta) {
        if (notReady()) {
            return super.mouseScrolled(pMouseX, pMouseY, pDelta);
        }
        Point mousePoint = new Point(pMouseX, pMouseY);
        Widget topMost = scrollableWidget(mousePoint);
        if (topMost instanceof MouseActor action) {
            return action.onScroll(mousePoint, pDelta);
        }
        return super.mouseScrolled(pMouseX, pMouseY, pDelta);
    }

    @Override
    public boolean keyReleased(int pKeyCode, int pScanCode, int pModifiers) {
        if (notReady()) {
            return super.keyReleased(pKeyCode, pScanCode, pModifiers);
        }
        Widget focused = focusedWidget();
        if (focused instanceof KeyActor action) {
            return action.onKeyReleased(pKeyCode, pScanCode, pModifiers);
        }
        return super.keyReleased(pKeyCode, pScanCode, pModifiers);
    }

    @Override
    public boolean charTyped(char pCodePoint, int pModifiers) {
        if (notReady()) {
            return super.charTyped(pCodePoint, pModifiers);
        }
        Widget focused = focusedWidget();
        if (focused instanceof KeyActor action) {
            return action.onCharTyped(pCodePoint, pModifiers);
        }
        return super.charTyped(pCodePoint, pModifiers);
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
        if (notReady()) {
            super.mouseMoved(pMouseX, pMouseY);
            return;
        }
        if (mouse.xInt() != Math.floor(pMouseX) || mouse.yInt() != Math.floor(pMouseY)) {
            Point mousePoint = new Point(pMouseX, pMouseY);
            Widget hoveredWidget = hoveredWidget(mousePoint);
            if ((hoveredWidget == null || hoveredWidget != hovered()) && hovered() != null) {
                hovered().hovered(false);
                hovered(null);
            }
            if (hoveredWidget != null) {
                hovered(hoveredWidget);
                hoveredWidget.hovered(true);
                LOGGER.trace("mouseMoved, hovering over {}", hoveredWidget.name());
            }
            if (focusedWidget() instanceof MouseActor action) {
                action.onMoved(mousePoint);
            }
            mouse = new Point(pMouseX, pMouseY);
        }
    }

    @Override
    public Rect size() {
        return new Rect(0, 0, width, height);
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
    public String toString() {
        return "AbstractScreen{root=" + root +
                ", currentTabIndex=" + currentTabIndex +
                '}';
    }
}