package ninja.crinkle.mod.client.gui.screens;

import com.mojang.logging.LogUtils;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.navigation.ScreenDirection;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import ninja.crinkle.mod.client.ClientSetup;
import ninja.crinkle.mod.client.gui.editors.LayoutRegistry;
import ninja.crinkle.mod.client.gui.events.*;
import ninja.crinkle.mod.client.gui.events.listeners.EventListener;
import ninja.crinkle.mod.client.gui.events.listeners.InputListener;
import ninja.crinkle.mod.client.gui.events.listeners.TabIndexListener;
import ninja.crinkle.mod.client.gui.managers.*;
import ninja.crinkle.mod.client.gui.events.sources.KeySource;
import ninja.crinkle.mod.client.gui.events.sources.MouseSource;
import ninja.crinkle.mod.client.gui.properties.*;
import ninja.crinkle.mod.client.gui.widgets.AbstractWidget;
import ninja.crinkle.mod.client.gui.widgets.AbstractContainer;
import ninja.crinkle.mod.client.gui.widgets.Container;
import ninja.crinkle.mod.util.ClientUtil;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public abstract class AbstractScreen extends Screen implements TabIndexListener, KeySource, MouseSource, GuiManager,
        IManagedGUI {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int SCAN_OFFSET = 10;
    private static final int DOUBLE_CLICK_TIME = 300;
    private static final int DOUBLE_CLICK_OFFSET = 2;
    private final EventManager eventManager = EventManager.createScreen();
    private final FocusManager focusManager = new FocusManager();
    private final DragManager dragManager = new DragManager(eventManager);
    private ClickState clickState;
    private final List<GuiEventListener> focusedElements = new ArrayList<>();
    private final AbstractContainer root;
    protected boolean ready = false;
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
        addListener(root());
        addListener(focusManager());
        addListener(dragManager());
        clickState = new ClickState();
    }

    protected void addListener(InputListener inputListener) {
        if (inputListener instanceof Renderable renderable) {
            this.addRenderableOnly(renderable);
        }
        eventManager().ifPresent(m -> m.addListener(inputListener));
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
    public DragManager dragManager() {
        return dragManager;
    }

    public Point mouse() {
        return mouse;
    }

    @Override
    public AbstractContainer root() {
        return root;
    }

    @Override
    public Optional<EventManager> eventManager() {
        return Optional.of(eventManager);
    }

    @Override
    abstract public String name();

    @Override
    public FocusManager focusManager() {
        return focusManager;
    }

    @Override
    public int getTabOrderGroup() {
        return super.getTabOrderGroup();
    }

    @Override
    public GuiManager manager() {
        return this;
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
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        if (!ready()) {
            return super.keyPressed(pKeyCode, pScanCode, pModifiers);
        }
        Event event = new KeyEvent(Scope.Screen, this, pKeyCode, pScanCode, pModifiers, false);
        eventManager().ifPresent(m -> m.dispatchEvent(event));
        return event.success() || super.keyPressed(pKeyCode, pScanCode, pModifiers);
    }

    @Override
    public void onClose() {
        super.onClose();
        eventManager().ifPresent(EventManager::clear);
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        if (!ready()) {
            return super.mouseClicked(pMouseX, pMouseY, pButton);
        }
        ClickState state = clickState;
        if (state.button() == pButton && System.currentTimeMillis() - state.clickTime() < DOUBLE_CLICK_TIME
                && state.position().distance(pMouseX, pMouseY) < DOUBLE_CLICK_OFFSET) {
            List<EventListener> listeners = eventManager()
                    .map(m -> m.listeners(onlyHovered(pMouseX, pMouseY, 0)).stream().toList())
                    .orElse(List.of());
            Event event = new DoubleClickEvent(Scope.Screen, this, pMouseX, pMouseY, pButton, listeners);
            eventManager().ifPresent(m -> m.dispatchEvent(event, l -> state.listeners().contains(l)));
            clickState = new ClickState(Point.ZERO, -1, 0, List.of());
            return event.success() || super.mouseClicked(pMouseX, pMouseY, pButton);
        }
        List<EventListener> listeners = eventManager()
                .map(m -> m.listeners(onlyHovered(pMouseX, pMouseY, 0)).stream().toList())
                .orElse(List.of());
        AbstractWidget topMost = listeners.stream()
                .filter(l -> l instanceof AbstractWidget)
                .map(l -> (AbstractWidget) l)
                .filter(c -> !c.equals(root()))
                .reduce((a, b) -> a.zIndex() > b.zIndex() ? a : b)
                .orElse(null);
        clickState = new ClickState(new Point(pMouseX, pMouseY), pButton, System.currentTimeMillis(), listeners);
        AbstractWidget dragTarget = topMost;
        while (dragTarget != null && !dragTarget.draggable()) {
            dragTarget = dragTarget.parent().filter(p -> !p.equals(root())).orElse(null);
        }
        if (dragTarget != null) {
            dragManager().current(dragTarget);
            dragManager().dragging(false);
        }
        AbstractWidget topFocusable = listeners.stream()
                .filter(l -> l instanceof AbstractWidget)
                .map(l -> (AbstractWidget) l)
                .filter(c -> !c.equals(root()))
                .filter(AbstractWidget::focusable)
                .min(Comparator.comparingInt(AbstractWidget::priority))
                .orElse(null);
        if ((topFocusable == null || topFocusable != focusManager().currentFocus()) && focusManager().currentFocus() != null) {
            Event event = new FocusLeftEvent(Scope.Screen, this, false, focusManager().currentFocus());
            eventManager().ifPresent(m -> m.dispatchEvent(event));
            focusManager().currentFocus(null);
        }
        if (topFocusable != null && topFocusable.focusable()) {
            Event event = new FocusEnteredEvent(Scope.Screen, this, true, topFocusable);
            eventManager().ifPresent(m -> m.dispatchEvent(event));
            focusManager().currentFocus(topFocusable);
        }
        Event mousePressedEvent = new MousePressedEvent(Scope.Screen, this, pMouseX, pMouseY, pButton, listeners);
        eventManager().ifPresent(m -> m.dispatchEvent(mousePressedEvent));
        return mousePressedEvent.success() || super.mouseClicked(pMouseX, pMouseY, pButton);
    }

    public boolean ready() {
        return ready;
    }

    @Override
    public boolean mouseReleased(double pMouseX, double pMouseY, int pButton) {
        if (!ready()) {
            return super.mouseReleased(pMouseX, pMouseY, pButton);
        }
        if (dragManager().current() != null) {
            if (dragManager().dragging()) {
                Event event = new DragStoppedEvent(Scope.Screen, this, pMouseX, pMouseY, pButton,
                        0, 0, dragManager().current());
                eventManager().ifPresent(m -> m.dispatchEvent(event));
                dragManager().dragging(false);
            }
            dragManager().current(null);
        }
        List<EventListener> listeners = eventManager().map(m -> m.listeners(onlyHovered(pMouseX, pMouseY, 0)).stream().toList()).orElse(List.of());
        Event event = new MouseReleasedEvent(Scope.Screen, this, pMouseX, pMouseY, pButton, listeners);
        eventManager().ifPresent(m -> m.dispatchEvent(event));
        ClickState state = clickState;
        if (state.button() == pButton) {
            List<EventListener> filteredListeners = listeners.stream()
                    .filter(l -> state.listeners().contains(l))
                    .toList();
            Event clickEvent = new ClickEvent(Scope.Screen, this, pMouseX, pMouseY, pButton, true, filteredListeners);
            eventManager().ifPresent(m -> m.dispatchEvent(clickEvent, l -> state.listeners().contains(l)));
            return event.success() || clickEvent.success() || super.mouseReleased(pMouseX, pMouseY, pButton);
        }
        return event.success() || super.mouseReleased(pMouseX, pMouseY, pButton);
    }

    @Override
    public boolean mouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY) {
        if (!ready()) {
            return super.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY);
        }
        if (dragManager().current() != null && !dragManager().dragging()) {
            dragManager().dragging(true);
            Event event = new DragStartedEvent(Scope.Screen, this, pMouseX, pMouseY, pButton, pDragX,
                    pDragY, dragManager().current());
            eventManager().ifPresent(m -> m.dispatchEvent(event));
        }
        List<EventListener> listeners = eventManager().map(m -> m.listeners(onlyHovered(pMouseX, pMouseY, 0)).stream().toList()).orElse(List.of());
        Event event = new DragEvent(Scope.Screen, this, pMouseX, pMouseY, pButton, pDragX, pDragY, listeners);
        eventManager().ifPresent(m -> m.dispatchEvent(event));
        return event.success() || super.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY);
    }

    @Override
    public boolean mouseScrolled(double pMouseX, double pMouseY, double pDelta) {
        if (!ready()) {
            return super.mouseScrolled(pMouseX, pMouseY, pDelta);
        }
        Event event = new ScrollEvent(Scope.Screen, this, pMouseX, pMouseY, pDelta);
        eventManager().ifPresent(m -> m.dispatchEvent(event));
        return event.success() || super.mouseScrolled(pMouseX, pMouseY, pDelta);
    }

    @Override
    public boolean keyReleased(int pKeyCode, int pScanCode, int pModifiers) {
        if (!ready()) {
            return super.keyReleased(pKeyCode, pScanCode, pModifiers);
        }
        Event event = new KeyEvent(Scope.Screen, this, pKeyCode, pScanCode, pModifiers, true);
        eventManager().ifPresent(m -> m.dispatchEvent(event));
        return event.success() || super.keyReleased(pKeyCode, pScanCode, pModifiers);
    }

    @Override
    public boolean charTyped(char pCodePoint, int pModifiers) {
        if (!ready()) {
            return super.charTyped(pCodePoint, pModifiers);
        }
        Event event = new CharTypedEvent(Scope.Screen, this, pCodePoint, pModifiers);
        eventManager().ifPresent(m -> m.dispatchEvent(event));
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

    private Predicate<EventListener> onlyHovered(double mouseX, double mouseY, int offset) {
        if (offset < 0) {
            throw new IllegalArgumentException("Offset must be greater than or equal to 0");
        }
        if (offset == 0) {
            return (l) -> l instanceof AbstractWidget widget && !l.equals(root())
                    && widget.rect().contains(mouseX, mouseY);
        }
        Rect expanded = new Rect(0, 0, 0, 0); // unused, expand inline below
        return (l) -> {
            if (!(l instanceof AbstractWidget widget) || l.equals(root())) return false;
            Rect r = widget.rect();
            return mouseX >= r.x() - offset && mouseX < r.right() + offset
                    && mouseY >= r.y() - offset && mouseY < r.bottom() + offset;
        };
    }

    @Override
    public void mouseMoved(double pMouseX, double pMouseY) {
        if (!ready()) {
            super.mouseMoved(pMouseX, pMouseY);
            return;
        }
        if (mouse.xInt() != Math.floor(pMouseX) || mouse.yInt() != Math.floor(pMouseY)) {
            List<EventListener> listeners =
                    eventManager().map(m -> m.listeners(onlyHovered(pMouseX, pMouseY, 0)).stream().toList())
                            .orElse(List.of());
            eventManager().ifPresent(m ->
                    m.dispatchEvent(new MoveEvent(Scope.Screen, this, pMouseX, pMouseY, listeners),
                    onlyHovered(pMouseX, pMouseY, SCAN_OFFSET)));
            mouse = new Point(pMouseX, pMouseY);
        }

    }

    @Override
    public void onTabIndexChanged(TabIndexEvent event) {
        this.focusedElements.sort(Comparator.comparingInt(GuiEventListener::getTabOrderGroup));
    }

    @Override
    public String toString() {
        return "AbstractScreen{" +
                "focusedElements=" + focusedElements +
                ", root=" + root +
                ", currentTabIndex=" + currentTabIndex +
                '}';
    }

    @Override
    public Rect size() {
        return new Rect(0, 0, width, height);
    }
}
