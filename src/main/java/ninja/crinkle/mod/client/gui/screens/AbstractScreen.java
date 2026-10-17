package ninja.crinkle.mod.client.gui.screens;

import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.navigation.ScreenDirection;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import ninja.crinkle.mod.client.gui.actions.DragAction;
import ninja.crinkle.mod.client.gui.actions.MouseAction;
import ninja.crinkle.mod.client.gui.actions.FocusAction;
import ninja.crinkle.mod.client.gui.actions.KeyPressAction;
import ninja.crinkle.mod.client.gui.events.*;
import ninja.crinkle.mod.client.gui.managers.*;
import ninja.crinkle.mod.client.gui.properties.ClickState;
import ninja.crinkle.mod.client.gui.properties.Point;
import ninja.crinkle.mod.client.gui.properties.Rect;
import ninja.crinkle.mod.client.gui.widgets.AbstractContainer;
import ninja.crinkle.mod.client.gui.widgets.AbstractWidget;
import ninja.crinkle.mod.client.gui.widgets.Container;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public abstract class AbstractScreen extends Screen implements GuiManager, IManagedGUI {
    private static final int DOUBLE_CLICK_OFFSET = 2;
    private static final int DOUBLE_CLICK_TIME = 300;
    private static final int SCAN_OFFSET = 10;
    private final EventManager eventManager = new EventManager();
    private final DragManager dragManager = new DragManager(eventManager);
    private final FocusManager focusManager = new FocusManager();
    private final List<GuiEventListener> focusedElements = new ArrayList<>();
    private final AbstractContainer root;
    protected boolean ready = false;
    private ClickState clickState;
    private int currentTabIndex = 0;
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
        clickState = new ClickState(point, pButton, System.currentTimeMillis());
    }

    abstract public String name();

    @Override
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        if (notReady()) {
            return super.keyPressed(pKeyCode, pScanCode, pModifiers);
        }
        AbstractWidget widget = root().topFocused();
        if (widget instanceof KeyPressAction action) {
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

    // mouse clicked handlers
    private boolean handlePressed(AbstractWidget widget, Point point, int pButton) {
        if (widget instanceof MouseAction action) {
            return action.onPressed(point, pButton);
        }
        return false;
    }

    private boolean handleDoubleClick(AbstractWidget widget, Point point, int pButton) {
        if (widget instanceof MouseAction action) {
            return action.onDoubleClick(point, pButton);
        }
        return false;
    }

    private void handleFocusLeft(AbstractWidget focusable) {
        if (focusable instanceof FocusAction action) {
            action.onBlur();
        }
    }

    private void handleFocusEntered(AbstractWidget focusable) {
        if (focusable instanceof FocusAction action) {
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
        AbstractWidget topMost = root().topClicked(point);

        // double click
        if (state.button() == pButton && System.currentTimeMillis() - state.clickTime() < DOUBLE_CLICK_TIME
                && state.position().distance(pMouseX, pMouseY) < DOUBLE_CLICK_OFFSET) {
            return handleDoubleClick(topMost, point, pButton);
        }

        clickState = new ClickState(point, pButton, System.currentTimeMillis());

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
        AbstractWidget topFocusable = root().topFocused();
        if ((topFocusable == null || topFocusable != focusManager().currentFocus()) && focusManager().currentFocus() != null) {
            handleFocusLeft(focusManager().currentFocus());
        }
        if (topFocusable != null && topFocusable.focusable()) {
            handleFocusEntered(topFocusable);
        }
        focusManager().currentFocus(topFocusable);

        // pressed
        return handlePressed(topMost, point, pButton) || super.mouseClicked(pMouseX, pMouseY, pButton);
    }

    // mouse released handlers
    private void handleDragStopped(AbstractWidget widget) {
        if (widget instanceof DragAction action) {
            action.onDragStopped();
        }
    }

    private boolean handleClicked(AbstractWidget widget, Point point, int pButton) {
        if (widget instanceof MouseAction action) {
            return action.onClick(point, pButton);
        }
        return false;
    }

    private boolean handleReleased(AbstractWidget widget, Point point, int pButton) {
        if (widget instanceof MouseAction action) {
            return action.onReleased(point, pButton);
        }
        return false;
    }


    @Override
    public boolean mouseReleased(double pMouseX, double pMouseY, int pButton) {
        if (notReady()) {
            return super.mouseReleased(pMouseX, pMouseY, pButton);
        }
        Point point = new Point(pMouseX, pMouseY);
        AbstractWidget topMost = root().topClicked(point);

        // drag
        if (dragManager().current() != null) {
            if (dragManager().dragging()) {
                dragManager().dragging(false);
                handleDragStopped(topMost);
            }
            dragManager().current(null);
        }

        // released & clicked
        ClickState state = clickState;
        return (state.button() == pButton && handleClicked(topMost, point, pButton))
                || handleReleased(topMost, point, pButton)
                || super.mouseReleased(pMouseX, pMouseY, pButton);
    }

    @Override
    public boolean mouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY) {
        if (notReady()) {
            return super.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY);
        }
        if (dragManager().current() != null && !dragManager().dragging()) {
            dragManager().dragging(true);
            Event event = new DragStartedEvent(this, pMouseX, pMouseY, pButton, pDragX,
                    pDragY, dragManager().current());
            eventManager().dispatchEvent(event);
        }
        List<EventNode> listeners = eventManager().listeners(onlyHoveredAndVisible(pMouseX, pMouseY, 0)).stream().toList();
        Event event = new DragEvent(this, pMouseX, pMouseY, pButton, pDragX, pDragY, listeners);
        eventManager().dispatchEvent(event);
        return event.success() || super.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY);
    }

    @Override
    public boolean mouseScrolled(double pMouseX, double pMouseY, double pDelta) {
        if (notReady()) {
            return super.mouseScrolled(pMouseX, pMouseY, pDelta);
        }
        Event event = new ScrollEvent(this, pMouseX, pMouseY, pDelta);
        eventManager().dispatchEvent(event);
        return event.success() || super.mouseScrolled(pMouseX, pMouseY, pDelta);
    }

    @Override
    public boolean keyReleased(int pKeyCode, int pScanCode, int pModifiers) {
        if (notReady()) {
            return super.keyReleased(pKeyCode, pScanCode, pModifiers);
        }
        Event event = new KeyEvent(this, pKeyCode, pScanCode, pModifiers, true);
        eventManager().dispatchEvent(event);
        return event.success() || super.keyReleased(pKeyCode, pScanCode, pModifiers);
    }

    @Override
    public boolean charTyped(char pCodePoint, int pModifiers) {
        if (notReady()) {
            return super.charTyped(pCodePoint, pModifiers);
        }
        Event event = new CharTypedEvent(this, pCodePoint, pModifiers);
        eventManager().dispatchEvent(event);
        return event.success() || super.charTyped(pCodePoint, pModifiers);
    }

    @Override
    public @Nullable ComponentPath getCurrentFocusPath() {
        return ComponentPath.leaf(this);
    }

    @Override
    public @Nullable ComponentPath nextFocusPath(FocusNavigationEvent pEvent) {
        if (focusedElements.isEmpty()) {
            return ComponentPath.leaf(this);
        }
        if (pEvent.getVerticalDirectionForInitialFocus() == ScreenDirection.DOWN) {
            currentTabIndex = (currentTabIndex + 1) % focusedElements.size();
        } else {
            currentTabIndex = (currentTabIndex - 1 + focusedElements.size()) % focusedElements.size();
        }
        return ComponentPath.leaf(focusedElements.get(currentTabIndex));
    }

    private Predicate<EventNode> onlyHoveredAndVisible(double mouseX, double mouseY, int offset) {
        if (offset < 0) {
            throw new IllegalArgumentException("Offset must be greater than or equal to 0");
        }
        if (offset == 0) {
            return (l) -> l instanceof AbstractWidget widget && !l.equals(root())
                    && widget.rect().contains(mouseX, mouseY);
        }
        return (l) -> {
            if (!(l instanceof AbstractWidget widget) || l.equals(root()) || !widget.visible()) return false;
            Rect r = widget.rect();
            return mouseX >= r.x() - offset && mouseX < r.right() + offset
                    && mouseY >= r.y() - offset && mouseY < r.bottom() + offset;
        };
    }

    @Override
    public void mouseMoved(double pMouseX, double pMouseY) {
        if (notReady()) {
            super.mouseMoved(pMouseX, pMouseY);
            return;
        }
        if (mouse.xInt() != Math.floor(pMouseX) || mouse.yInt() != Math.floor(pMouseY)) {
            List<EventNode> listeners =
                    eventManager().listeners(onlyHoveredAndVisible(pMouseX, pMouseY, 0)).stream().toList();
            eventManager().dispatchEvent(new MoveEvent(this, pMouseX, pMouseY, listeners),
                    onlyHoveredAndVisible(pMouseX, pMouseY, SCAN_OFFSET));
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
        return "AbstractScreen{" +
                "focusedElements=" + focusedElements +
                ", root=" + root +
                ", currentTabIndex=" + currentTabIndex +
                '}';
    }
}