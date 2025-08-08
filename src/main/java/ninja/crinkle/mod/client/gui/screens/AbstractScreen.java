package ninja.crinkle.mod.client.gui.screens;

import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.navigation.ScreenDirection;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import ninja.crinkle.mod.client.gui.events.*;
import ninja.crinkle.mod.client.gui.events.listeners.EventListener;
import ninja.crinkle.mod.client.gui.events.listeners.InputListener;
import ninja.crinkle.mod.client.gui.events.listeners.TabIndexListener;
import ninja.crinkle.mod.client.gui.managers.*;
import ninja.crinkle.mod.client.gui.events.sources.KeySource;
import ninja.crinkle.mod.client.gui.events.sources.MouseSource;
import ninja.crinkle.mod.client.gui.properties.ClickState;
import ninja.crinkle.mod.client.gui.properties.ImmutablePoint;
import ninja.crinkle.mod.client.gui.properties.Point;
import ninja.crinkle.mod.client.gui.properties.Scope;
import ninja.crinkle.mod.client.gui.states.references.Ref;
import ninja.crinkle.mod.client.gui.widgets.AbstractWidget;
import ninja.crinkle.mod.client.gui.widgets.AbstractContainer;
import ninja.crinkle.mod.client.gui.widgets.Container;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public abstract class AbstractScreen extends Screen implements TabIndexListener, KeySource, MouseSource, GuiManager {
    private static final int SCAN_OFFSET = 10;
    private static final int DOUBLE_CLICK_TIME = 300;
    private static final int DOUBLE_CLICK_OFFSET = 2;
    private final EventManager eventManager = EventManager.createScreen();
    private final FocusManager focusManager = new FocusManager();
    private final DragManager dragManager = new DragManager(eventManager);
    private final Ref<ClickState> clickState;
    private final List<GuiEventListener> focusedElements = new ArrayList<>();
    private final AbstractContainer root;
    private boolean ready = false;
    private int currentTabIndex = 0;

    private Point mouse = ImmutablePoint.ZERO;

    protected AbstractScreen(Component pTitle) {
        super(pTitle);
        this.root = new Container.Builder(this)
                .name("root")
                .size(this.width, this.height)
                .absolute(ImmutablePoint.ZERO)
                .build();
        addListener(root());
        addListener(focusManager());
        addListener(dragManager());
        clickState = new Ref<>(new ClickState());
    }

    protected void addListener(InputListener inputListener) {
        if (inputListener instanceof Renderable renderable) {
            this.addRenderableOnly(renderable);
        }
        eventManager().ifPresent(m -> m.addListener(inputListener));
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
    public void tick() {
        super.tick();
        root().tick();
    }

    @Override
    protected void init() {
        super.init();
        root().init();
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
        ClickState state = clickState.value();
        if (state.button() == pButton && System.currentTimeMillis() - state.clickTime() < DOUBLE_CLICK_TIME
                && state.position().distance(pMouseX, pMouseY) < DOUBLE_CLICK_OFFSET) {
            List<EventListener> listeners = eventManager()
                    .map(m -> m.listeners(onlyHovered(pMouseX, pMouseY, 0)).stream().toList())
                    .orElse(List.of());
            Event event = new DoubleClickEvent(Scope.Screen, this, pMouseX, pMouseY, pButton, listeners);
            eventManager().ifPresent(m -> m.dispatchEvent(event, l -> state.listeners().contains(l)));
            clickState.value(new ClickState(ImmutablePoint.ZERO, -1, 0, List.of()));
            return event.success() || super.mouseClicked(pMouseX, pMouseY, pButton);
        }
        List<EventListener> listeners = eventManager()
                .map(m -> m.listeners(onlyHovered(pMouseX, pMouseY, 0)).stream().toList())
                .orElse(List.of());
        // We want to prep to drag the top-most draggable parent container.
        AbstractWidget topMost = listeners.stream()
                .filter(l -> l instanceof AbstractWidget)
                .map(l -> (AbstractWidget) l)
                .filter(c -> !c.equals(root()))
                .reduce((a, b) -> a.zIndex() > b.zIndex() ? a : b)
                .orElse(null);
        clickState.value(new ClickState(new ImmutablePoint(pMouseX, pMouseY), pButton, System.currentTimeMillis(), listeners));
        if (topMost != null && topMost.draggable()) {
            dragManager().current(topMost);
            dragManager().dragging(false); // Reset dragging state
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
        ClickState state = clickState.value();
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
            return (l) -> l instanceof AbstractWidget widget && !l.equals(root()) && widget.layout().boxes().rendered().box().contains(mouseX, mouseY);
        }
        return (l) -> l instanceof AbstractWidget widget && !l.equals(root()) &&
                widget.layout().boxes().rendered().box().add(-offset, -offset, offset * 2, offset * 2).contains(mouseX, mouseY);
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
            mouse = new ImmutablePoint(pMouseX, pMouseY);
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
}
