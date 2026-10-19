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
import ninja.crinkle.mod.client.gui.widgets.AbstractWidget;
import ninja.crinkle.mod.client.gui.widgets.Container;
import ninja.crinkle.mod.client.gui.widgets.ScrollContainer;
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
    private static final int SCAN_OFFSET = 10;
    private final DragManager dragManager = new DragManager();
    private final FocusManager focusManager = new FocusManager();
    private final AbstractContainer root;
    protected boolean ready = false;
    private ClickState clickState;
    private int currentTabIndex = 0;
    private Rect lastScreenRect;
    private Point mouse = Point.ZERO;
    private AbstractWidget hovered = null;
    private final List<AbstractWidget> widgetCache = new ArrayList<>();

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

    public AbstractWidget hovered() {
        return hovered;
    }

    public void hovered(AbstractWidget hovered) {
        this.hovered = hovered;
    }

    abstract public String name();

    @Override
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        if (notReady()) {
            return super.keyPressed(pKeyCode, pScanCode, pModifiers);
        }
        AbstractWidget widget = focusedWidget();
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

    public List<AbstractWidget> widgetCache() {
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

    private List<AbstractWidget> widgets(AbstractContainer parent) {
        List<AbstractWidget> widgets = new java.util.ArrayList<>();
        widgets.add(parent);
        for(AbstractWidget widget : parent.children().stream().toList()) {
            if (widget instanceof AbstractContainer container) {
                widgets.addAll(widgets(container));
            } else {
                widgets.add(widget);
            }
        }
        return widgets;
    }

    private AbstractWidget hoveredWidget(Point point) {
        return widgets(root()).stream()
                .filter(AbstractWidget::active)
                .filter(AbstractWidget::visible)
                .filter(AbstractWidget::hoverable)
                .filter(w -> w.rect().contains(point))
                .max(Comparator.comparingInt(AbstractWidget::zIndexOf))
                .orElse(null);
    }

    private AbstractWidget clickedWidget(Point point) {
        return widgets(root()).stream()
                .filter(AbstractWidget::active)
                .filter(AbstractWidget::visible)
                .filter(AbstractWidget::pressable)
                .filter(w -> w.rect().contains(point))
                .max(Comparator.comparingInt(AbstractWidget::zIndexOf))
                .orElse(null);
    }

    private AbstractWidget focusableWidget(Point point) {
        return widgets(root()).stream()
                .filter(AbstractWidget::active)
                .filter(AbstractWidget::visible)
                .filter(AbstractWidget::focusable)
                .filter(w -> w.rect().contains(point))
                .max(Comparator.comparingInt(AbstractWidget::zIndexOf))
                .orElse(null);
    }

    private AbstractWidget focusedWidget() {
        return widgets(root()).stream()
                .filter(AbstractWidget::active)
                .filter(AbstractWidget::visible)
                .filter(AbstractWidget::focused)
                .max(Comparator.comparingInt(AbstractWidget::zIndexOf))
                .orElse(null);
    }

    private AbstractWidget scrollableWidget(Point point) {
        return widgets(root()).stream()
                .filter(AbstractWidget::active)
                .filter(AbstractWidget::visible)
                .filter(AbstractWidget::scrollable)
                .filter(w -> w.rect().contains(point))
                .max(Comparator.comparingInt(AbstractWidget::zIndexOf))
                .orElse(null);
    }

    // mouse clicked handlers
    private boolean handlePressed(AbstractWidget widget, Point point, int pButton) {
        if (widget == null || !widget.pressable()) return false;
        widget.pressed(true);
        if (widget instanceof MouseActor action) {
            return action.onPressed(point, MouseButton.fromInt(pButton));
        }
        return false;
    }

    private boolean handleDoubleClick(AbstractWidget widget, Point point, int pButton) {
        if (widget instanceof MouseActor action) {
            return action.onDoubleClick(point, MouseButton.fromInt(pButton));
        }
        return false;
    }

    private void handleFocusLeft(AbstractWidget focusable) {
        if (focusable instanceof FocusActor action) {
            action.onBlur();
        }
    }

    private void handleFocusEntered(AbstractWidget focusable) {
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
        AbstractWidget topMost = clickedWidget(point);
        if (topMost != null)
            LOGGER.trace("mouseClicked on {}, zIndex: {}, calculated zIndex: {}", topMost.name(), topMost.zIndex(), AbstractWidget.zIndexOf(topMost));
        // double click
        if (topMost != null && state.button() == pButton && System.currentTimeMillis() - state.clickTime() < DOUBLE_CLICK_TIME
                && state.position().distance(pMouseX, pMouseY) < DOUBLE_CLICK_OFFSET) {
            return handleDoubleClick(topMost, point, pButton);
        }

        clickState = new ClickState(point, pButton, System.currentTimeMillis(), topMost);

        // drag
        AbstractWidget dragTarget = topMost;
        while (dragTarget != null && !dragTarget.draggable()) {
            dragTarget = dragTarget.parent().filter(p -> !p.equals(root())).orElse(null);
        }
        if (dragTarget != null) {
            dragManager().current(dragTarget);
            dragManager().dragging(false);
        }

        // focus
        AbstractWidget topFocusable = focusableWidget(point);
//        LOGGER.trace("mouseClicked:focus on {}, zIndex: {}, calculated zIndex: {}", topFocusable.name(), topFocusable.zIndex(), AbstractWidget.zIndexOf(topFocusable));
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
    private void handleDragStopped(AbstractWidget widget, Point point) {
        if (widget instanceof DragActor action) {
            action.onDragStopped(widget, point);
        }
    }

    private boolean handleClicked(AbstractWidget widget, Point point, int pButton) {
        if (widget instanceof MouseActor action) {
            return action.onClick(point, MouseButton.fromInt(pButton));
        }
        return false;
    }

    private boolean handleReleased(AbstractWidget widget, Point point, int pButton) {
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
        AbstractWidget topMost = clickedWidget(point);
        if (topMost != null)
            LOGGER.trace("mouseReleased on {}, zIndex: {}, calculated zIndex: {}", topMost.name(), topMost.zIndex(), AbstractWidget.zIndexOf(topMost));
        // drag
        if (dragManager().current() != null) {
            if (dragManager().dragging()) {
                AbstractWidget dropped = dragManager().current();
                dragManager().drop();
                handleDragStopped(dropped, point);
            }
            dragManager().current(null);
        }

        // released & clicked
        boolean result = false;
        ClickState state = clickState;
        if (topMost != null && state.button() == pButton && topMost == state.widget())
            result = handleClicked(topMost, point, pButton);
        return (state.widget() != null && handleReleased(state.widget(), point, pButton)) || result || super.mouseReleased(pMouseX, pMouseY, pButton);
    }

    // mouse dragged handlers
    private boolean handleDragStarted(AbstractWidget widget, Point mousePoint, Point dragPoint, int button) {
        if (widget instanceof DragActor action) {
            return action.onDragStarted(widget, mousePoint, dragPoint, button);
        }
        return false;
    }

    private boolean handleDrag(AbstractWidget widget, Point mousePoint, Point dragPoint, int button) {
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
        AbstractWidget topMost = clickedWidget(mousePoint);
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
        AbstractWidget topMost = scrollableWidget(mousePoint);
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
        AbstractWidget focused = focusedWidget();
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
        AbstractWidget focused = focusedWidget();
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
        List<AbstractWidget> focusElements = widgets(root()).stream()
                .filter(AbstractWidget::focusable)
                .sorted(Comparator.comparingInt(AbstractWidget::tabIndex))
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
            AbstractWidget hoveredWidget = hoveredWidget(mousePoint);
            if ((hoveredWidget == null || hoveredWidget != hovered()) && hovered() != null) {
                hovered().hovered(false);
                hovered(null);
            }
            if (hoveredWidget != null) {
                hovered(hoveredWidget);
                hoveredWidget.hovered(true);
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