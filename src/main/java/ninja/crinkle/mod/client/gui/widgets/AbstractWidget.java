package ninja.crinkle.mod.client.gui.widgets;

import com.mojang.logging.LogUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import ninja.crinkle.mod.client.color.Color;
import ninja.crinkle.mod.client.gui.builders.GenericBuilder;
import ninja.crinkle.mod.client.gui.events.*;
import ninja.crinkle.mod.client.gui.events.listeners.EventListener;
import ninja.crinkle.mod.client.gui.events.listeners.FocusListener;
import ninja.crinkle.mod.client.gui.events.listeners.MouseListener;
import ninja.crinkle.mod.client.gui.events.sources.FocusSource;
import ninja.crinkle.mod.client.gui.events.sources.LayoutSource;
import ninja.crinkle.mod.client.gui.events.sources.MouseSource;
import ninja.crinkle.mod.client.gui.events.sources.TabIndexSource;
import ninja.crinkle.mod.client.gui.properties.Sizing;
import ninja.crinkle.mod.client.gui.managers.DragManager;
import ninja.crinkle.mod.client.gui.managers.EventManager;
import ninja.crinkle.mod.client.gui.managers.GuiManager;
import ninja.crinkle.mod.client.gui.properties.*;
import ninja.crinkle.mod.client.gui.renderers.ThemeGraphics;
import ninja.crinkle.mod.client.gui.renderers.ThemeRenderable;
import ninja.crinkle.mod.client.gui.states.WidgetBehavior;
import ninja.crinkle.mod.client.gui.states.WidgetDisplay;
import ninja.crinkle.mod.client.gui.textures.ThemeAtlas;
import ninja.crinkle.mod.client.gui.themes.Style;
import ninja.crinkle.mod.client.gui.themes.StyleVariant;
import ninja.crinkle.mod.client.gui.themes.ThemeRegistry;
import ninja.crinkle.mod.config.ClientConfig;
import ninja.crinkle.mod.util.ClientUtil;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.*;
import java.util.function.Predicate;

public abstract class AbstractWidget implements Renderable, Widget, MouseSource, MouseListener,
        FocusSource, FocusListener, LayoutSource, TabIndexSource, ThemeRenderable, GuiEventListener {
    private static final Logger LOGGER = LogUtils.getLogger();
    private WidgetBehavior behavior;
    private WidgetDisplay display;
    private final GuiManager manager;
    private Predicate<AbstractWidget> activePredicate;
    private String name;
    private AbstractContainer parent;
    private int priority;
    private Style style;
    private int tabIndex;

    // New Godot-style layout fields
    private Rect rect = Rect.ZERO;
    private int minWidth;
    private int minHeight;
    private EnumSet<Sizing> horizontalSizing;
    private EnumSet<Sizing> verticalSizing;
    private float stretchRatio;
    private double dragAccumX;
    private double dragAccumY;

    protected AbstractWidget(@NotNull AbstractBuilder<?> builder) {
        this.activePredicate = builder.activePredicate();
        this.name = builder.name();
        this.style = builder.style();
        this.tabIndex = builder.tabIndex();
        this.manager = builder.manager();
        this.priority = builder.priority();
        this.parent = builder.parent();
        this.display = builder.display();
        this.behavior = builder.behavior();
        this.minWidth = builder.minWidth();
        this.minHeight = builder.minHeight();
        this.horizontalSizing = builder.horizontalSizing();
        this.verticalSizing = builder.verticalSizing();
        this.stretchRatio = builder.stretchRatio();

        if (builder.zIndex() == DragManager.Z_MIN) {
            parent().filter(p -> !p.equals(this)).ifPresent(p -> zIndex(nextZIndex()));
        }

        if (builder.priority() == 0) {
            parent().filter(p -> !p.equals(this)).ifPresent(p -> priority(nextPriority()));
        }
    }

    public AbstractWidget() {
        this.activePredicate = null;
        this.name = "Unnamed_" + hashCode();
        this.style = Style.defaultStyle();
        this.tabIndex = 0;
        this.manager = GuiManager.create();
        this.priority = 1;
        this.parent = null;
        this.display = new WidgetDisplay();
        this.behavior = new WidgetBehavior();
        this.minWidth = 0;
        this.minHeight = 0;
        this.horizontalSizing = EnumSet.of(Sizing.Fill);
        this.verticalSizing = EnumSet.of(Sizing.Fill);
        this.stretchRatio = 1.0f;
    }

    // --- Rect / Layout ---

    @Override
    public Rect rect() {
        return rect;
    }

    public void setRect(Rect rect) {
        this.rect = rect;
    }

    public int minimumWidth() {
        return minWidth;
    }

    public int minimumHeight() {
        return minHeight;
    }

    public void minimumSize(int width, int height) {
        this.minWidth = width;
        this.minHeight = height;
    }

    public EnumSet<Sizing> horizontalSizing() {
        return horizontalSizing;
    }

    public void horizontalSizing(Sizing... flags) {
        this.horizontalSizing = Sizing.of(flags);
    }

    public EnumSet<Sizing> verticalSizing() {
        return verticalSizing;
    }

    public void verticalSizing(Sizing... flags) {
        this.verticalSizing = Sizing.of(flags);
    }

    public float stretchRatio() {
        return stretchRatio;
    }

    public void stretchRatio(float ratio) {
        this.stretchRatio = ratio;
    }

    // --- Identity ---

    public String name() {
        return name;
    }

    public void name(String name) {
        this.name = name;
    }

    public Optional<AbstractContainer> parent() {
        return Optional.ofNullable(parent);
    }

    public AbstractContainer parentOrThrow() {
        if (parent == null) {
            throw new IllegalStateException("Parent cannot be null");
        }
        return parent;
    }

    public void parent(AbstractContainer parent) {
        this.parent = parent;
    }

    // --- State ---

    public boolean active() {
        if (activePredicate != null) {
            return activePredicate.test(this);
        }
        return behavior().active();
    }

    public void active(boolean active) {
        behavior(behavior().withActive(active));
    }

    public void active(Predicate<AbstractWidget> activePredicate) {
        this.activePredicate = activePredicate;
    }

    public float alpha() {
        return display().alpha();
    }

    @SuppressWarnings("unused")
    public void alpha(float alpha) {
        display(display().withAlpha(alpha));
    }

    public boolean draggable() {
        return behavior().draggable();
    }

    public void draggable(boolean draggable) {
        behavior(behavior().withDraggable(draggable));
    }

    public boolean focused() {
        return behavior().focused();
    }

    public void focused(boolean focused) {
        if (focused != focused()) {
            if (focused) {
                dispatchEvent(new FocusEnteredEvent(Scope.Local, this, true, this));
            } else {
                dispatchEvent(new FocusLeftEvent(Scope.Local, this, false, this));
            }
        }
        behavior(behavior().withFocused(focused));
    }

    public boolean hovered() {
        return behavior().hovered();
    }

    public void hovered(boolean hovered) {
        if (!behavior().hoverable())
            return;
        if (hovered != hovered()) {
            Point mouse = ClientUtil.getMousePosition();
            dispatchEvent(new HoverEvent(Scope.Local, this, mouse.x(), mouse.y(), hovered));
        }
        behavior(behavior().withHovered(hovered));
    }

    public boolean pressed() {
        return behavior().pressed();
    }

    public void pressed(boolean pressed) {
        behavior(behavior().withPressed(pressed));
    }

    public boolean focusable() {
        return behavior().focusable();
    }

    @SuppressWarnings("unused")
    public void focusable(boolean focusable) {
        behavior(behavior().withFocusable(focusable));
    }

    private boolean hoverable() {
        return behavior().hoverable();
    }

    @SuppressWarnings("unused")
    public void hoverable(boolean hoverable) {
        behavior(behavior().withHoverable(hoverable));
    }

    public boolean pressable() {
        return behavior().pressable();
    }

    @SuppressWarnings("unused")
    public void pressable(boolean pressable) {
        behavior(behavior().withPressable(pressable));
    }

    public boolean repositionable() {
        return behavior().repositionable();
    }

    public void repositionable(boolean repositionable) {
        behavior(behavior().withRepositionable(repositionable));
    }

    @Override
    public boolean dragged() {
        return behavior().dragged();
    }

    public void dragged(boolean dragged) {
        if (dragged == dragged()) return;
        behavior(behavior().withDragged(dragged));
    }

    public boolean visible() {
        return this.display.visible();
    }

    public void visible(boolean visible) {
        this.display = display().withVisible(visible);
    }

    // --- Behavior / Display ---

    public WidgetBehavior behavior() {
        return behavior;
    }

    public void behavior(WidgetBehavior behavior) {
        this.behavior = behavior;
    }

    public WidgetDisplay display() {
        return display;
    }

    public void display(WidgetDisplay display) {
        this.display = display;
    }

    public int zIndex() {
        return display().zIndex();
    }

    public void zIndex(int zIndex) {
        if (zIndex >= DragManager.Z_MAX) {
            throw new IllegalArgumentException("Z-index cannot be set to maximum value");
        }
        display(display().withZIndex(zIndex));
    }

    private int nextZIndex() {
        return parent().map(abstractContainer -> abstractContainer.children().stream()
                .mapToInt(AbstractWidget::zIndex).max()
                .orElse(abstractContainer.zIndex()) + DragManager.Z_STEP).orElse(DragManager.Z_STEP);
    }

    public int priority() {
        return priority;
    }

    public void priority(int priority) {
        this.priority = priority;
    }

    private int nextPriority() {
        return parent().map(abstractContainer -> abstractContainer.children().stream()
                .mapToInt(AbstractWidget::priority).max()
                .orElse(abstractContainer.priority()) - EventManager.PRIORITY_STEP)
                .orElseGet(() -> EventManager.PRIORITY_STEP);
    }

    public static int zIndexOf(AbstractWidget widget) {
        return widget.zIndex();
    }

    // --- Theme ---

    public Style style() {
        return style;
    }

    @SuppressWarnings("unused")
    public void style(Style style) {
        this.style = style;
    }

    public StyleVariant appearance() {
        return Style.Variant.from(this).stream()
                .sorted(Comparator.comparingInt(Style.Variant::rank))
                .map(variant -> style().appearance(variant))
                .filter(Objects::nonNull)
                .reduce(StyleVariant::coalesce)
                .orElse(StyleVariant.EMPTY);
    }

    // --- Events ---

    @Override
    public void setFocused(boolean pFocused) {
        focused(pFocused);
    }

    @Override
    public boolean isFocused() {
        return focused();
    }

    @Override
    public int tabIndex() {
        return tabIndex;
    }

    @Override
    public void tabIndex(int tabIndex) {
        if (tabIndex != tabIndex())
            dispatchEvent(new TabIndexEvent(Scope.Screen, this, focused(), tabIndex(), tabIndex, this));
        this.tabIndex = tabIndex;
    }

    public void dispatchEvent(AbstractEvent event) {
        switch (event.scope()) {
            case Screen -> manager.eventManager().ifPresent(manager -> manager.dispatchEvent(event));
            case Local -> eventManager().ifPresent(manager -> manager.dispatchEvent(event));
            case Global -> EventManager.global().dispatchEvent(event);
        }
    }

    @Override
    public Optional<EventManager> eventManager(Scope scope) {
        return switch (scope) {
            case Screen -> manager().eventManager();
            case Local -> eventManager();
            case Global -> Optional.of(EventManager.global());
        };
    }

    public GuiManager manager() {
        return manager;
    }

    public void addListener(EventListener listener) {
        eventManager().ifPresent(manager -> manager.addListener(listener));
    }

    @Override
    public void layoutChanged() {
        LayoutChangedEvent event = new LayoutChangedEvent(Event.Type.LayoutChanged, Scope.Screen, this);
        dispatchEvent(event);
    }

    // --- Mouse ---

    @Override
    public boolean mouseOver(Point position) {
        return rect.contains(position.x(), position.y());
    }

    @Override
    public void onDrag(DragEvent event) {
        if (event.cancelled()) return;
        if (!dragged()) return;
        dragAccumX += event.dragX();
        dragAccumY += event.dragY();
        int dx = (int) dragAccumX;
        int dy = (int) dragAccumY;
        if (dx == 0 && dy == 0) return;
        dragAccumX -= dx;
        dragAccumY -= dy;
        Rect old = rect;
        setRect(new Rect(rect.x() + dx, rect.y() + dy, rect.width(), rect.height()));
        if (rect.equals(old)) return;
        event.consumer(this);
    }

    @Override
    public void onDragStarted(DragStartedEvent event) {
        if (event.cancelled()) return;
        if (event.widget() == this) {
            dragAccumX = 0;
            dragAccumY = 0;
            dragged(true);
            event.consumer(this);
        }
    }

    @Override
    public void onDropped(DroppedEvent event) {
        if (event.cancelled()) return;
        if (event.widget() == this) {
            dragged(false);
            pressed(false);
            event.consumer(this);
            DroppedEvent localEvent = new DroppedEvent(Scope.Local, this,
                    event.position().x(),
                    event.position().y(),
                    event.button().button(),
                    this,
                    List.of()
            );
            dispatchEvent(localEvent);
        }
    }

    @Override
    public void onMousePressed(MousePressedEvent event) {
        if (event.cancelled()) return;
        if (mouseOver(event.position())) {
            if (event.isLeftButton()) {
                AbstractWidget topMost =
                        (AbstractWidget) event.listeners().stream()
                                .min(Comparator.comparingInt(EventListener::priority))
                                .filter(listener -> listener instanceof AbstractWidget)
                                .orElse(null);
                if (topMost == this) {
                    if (pressable()) pressed(true);
                    if (draggable()) dragged(true);
                    event.consumer(this);
                }
            }
        } else if (pressed()) {
            pressed(false);
        }
    }

    @Override
    public void onMouseReleased(MouseReleasedEvent event) {
        if (event.cancelled()) return;
        if (pressed()) {
            pressed(false);
        }
    }

    @Override
    public void onMove(MoveEvent event) {
        if (event.cancelled()) return;
        if (!hoverable()) return;
        AbstractWidget topMost =
                (AbstractWidget) event.listeners().stream()
                        .min(Comparator.comparingInt(EventListener::priority))
                        .filter(listener -> listener instanceof AbstractWidget)
                        .orElse(null);
        if (topMost != null && topMost != this) {
            hovered(false);
            return;
        }

        if (hovered()) {
            if (!mouseOver(event.position())) {
                hovered(false);
            }
        } else if (mouseOver(event.position())) {
            hovered(true);
        }
    }

    // --- Rendering ---

    /**
     * @implNote For legacy use only, use AbstractWidget#render(ThemeGraphics, Point, float) instead
     */
    @Override
    public void render(@NotNull GuiGraphics graphics, int pMouseX, int pMouseY, float pPartialTick) {
        render(new ThemeGraphics(graphics, ThemeAtlas.getAtlas()), new Point(pMouseX, pMouseY), pPartialTick);
    }

    @Override
    public void render(@NotNull ThemeGraphics graphics, Point pMouse, float pPartialTick) {
        if (!visible() || rect == null || rect.equals(Rect.ZERO)) return;
        if (!ClientConfig.debug()) style().render(graphics, rect, this);
        renderContent(graphics, pMouse, rect, pPartialTick);
        if (ClientConfig.debug()) {
            renderDebug(graphics);
        }
    }

    @Override
    public abstract void renderContent(ThemeGraphics graphics, Point pMouse, Rect renderedRect, float pPartialTick);

    @Override
    public void renderDebug(ThemeGraphics pGuiGraphics) {
        if (parent().isEmpty()) return;
        pGuiGraphics.drawRect(rect, Color.GREEN, zIndex());
    }

    // --- Visual Copy ---

    public AbstractWidget visualCopy(AbstractContainer newParent) {
        throw new UnsupportedOperationException(
            getClass().getSimpleName() + " does not support visualCopy");
    }

    protected void copyVisualProperties(AbstractWidget source) {
        this.name = source.name;
        this.style = source.style;
        this.minWidth = source.minWidth;
        this.minHeight = source.minHeight;
        this.horizontalSizing = EnumSet.copyOf(source.horizontalSizing);
        this.verticalSizing = EnumSet.copyOf(source.verticalSizing);
        this.stretchRatio = source.stretchRatio;
        this.display = source.display;
        this.behavior = new WidgetBehavior(
            source.behavior.draggable(), false, false, false, false,
            source.behavior.active(), source.behavior.focusable(),
            source.behavior.hoverable(), source.behavior.pressable(),
            source.behavior.repositionable());
    }

    // --- Lifecycle ---

    protected void init() {
    }

    protected void tick() {
    }

    // --- Equals / ToString ---

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (AbstractWidget) obj;
        return Objects.equals(this.activePredicate, that.activePredicate) &&
                Objects.equals(this.name, that.name) &&
                Objects.equals(this.style, that.style) &&
                this.tabIndex == that.tabIndex &&
                this.priority == that.priority &&
                Objects.equals(this.parent, that.parent) &&
                Objects.equals(this.display, that.display) &&
                Objects.equals(this.behavior, that.behavior) &&
                Objects.equals(this.rect, that.rect);
    }

    @Override
    public String toString() {
        return "Widget{name=" + name + ", rect=" + rect + "}";
    }

    // --- Builder ---

    @SuppressWarnings({"unused", "UnusedReturnValue"})
    public abstract static class AbstractBuilder<T extends AbstractBuilder<T>> extends GenericBuilder<T,
            AbstractWidget> {
        private final GuiManager manager;
        private final AbstractContainer parent;
        private Predicate<AbstractWidget> activePredicate;
        private WidgetBehavior behavior = new WidgetBehavior();
        private WidgetDisplay display = new WidgetDisplay();
        private String name = "Unnamed_" + hashCode();
        private int priority = 0;
        private Style style = null;
        private int tabIndex = 0;
        private boolean visible = true;

        // New layout fields
        private int minWidth = 0;
        private int minHeight = 0;
        private EnumSet<Sizing> horizontalSizing = EnumSet.of(Sizing.Fill);
        private EnumSet<Sizing> verticalSizing = EnumSet.of(Sizing.Fill);
        private float stretchRatio = 1.0f;

        protected AbstractBuilder(GuiManager manager) {
            this.manager = manager;
            this.parent = manager.root();
        }

        protected AbstractBuilder(GuiManager manager, AbstractContainer parent) {
            this.manager = manager;
            this.parent = parent;
        }

        protected AbstractBuilder(AbstractContainer parent) {
            if (parent == null || parent.manager() == null) {
                throw new IllegalArgumentException("Parent or GUI manager cannot be null");
            }
            this.manager = parent.manager();
            this.parent = parent;
        }

        // --- Layout builders ---

        public T minSize(int width, int height) {
            this.minWidth = width;
            this.minHeight = height;
            return self();
        }

        public int minWidth() {
            return minWidth;
        }

        public int minHeight() {
            return minHeight;
        }

        public T horizontalSizing(Sizing... flags) {
            this.horizontalSizing = Sizing.of(flags);
            return self();
        }

        public EnumSet<Sizing> horizontalSizing() {
            return horizontalSizing;
        }

        public T verticalSizing(Sizing... flags) {
            this.verticalSizing = Sizing.of(flags);
            return self();
        }

        public EnumSet<Sizing> verticalSizing() {
            return verticalSizing;
        }

        public T stretchRatio(float ratio) {
            this.stretchRatio = ratio;
            return self();
        }

        public float stretchRatio() {
            return stretchRatio;
        }

        // --- State builders ---

        public T active(boolean active) {
            behavior = behavior.withActive(active);
            return self();
        }

        public boolean active() {
            return behavior.active();
        }

        public T activePredicate(Predicate<AbstractWidget> activePredicate) {
            this.activePredicate = activePredicate;
            return self();
        }

        public Predicate<AbstractWidget> activePredicate() {
            return activePredicate;
        }

        public T alpha(float alpha) {
            display = display.withAlpha(alpha);
            return self();
        }

        public float alpha() {
            return display.alpha();
        }

        public WidgetBehavior behavior() {
            return behavior;
        }

        public WidgetDisplay display() {
            return display;
        }

        public T behavior(WidgetBehavior behavior) {
            this.behavior = behavior;
            return self();
        }

        public T display(WidgetDisplay display) {
            this.display = display;
            return self();
        }

        public GuiManager manager() {
            return manager;
        }

        public abstract AbstractWidget build();

        public boolean draggable() {
            return behavior.draggable();
        }

        public T draggable(boolean draggable) {
            behavior = behavior.withDraggable(draggable);
            return self();
        }

        public T focusable(boolean focusable) {
            behavior = behavior.withFocusable(focusable);
            return self();
        }

        public boolean focusable() {
            return behavior.focusable();
        }

        public boolean hoverable() {
            return behavior.hoverable();
        }

        public T hoverable(boolean hoverable) {
            behavior = behavior.withHoverable(hoverable);
            return self();
        }

        public String name() {
            return name;
        }

        public T name(String name) {
            this.name = name;
            return self();
        }

        public AbstractContainer parent() {
            return parent;
        }

        public boolean pressable() {
            return behavior.pressable();
        }

        public T pressable(boolean pressable) {
            behavior = behavior.withPressable(pressable);
            return self();
        }

        public boolean repositionable() {
            return behavior.repositionable();
        }

        public T repositionable(boolean repositionable) {
            behavior = behavior.withRepositionable(repositionable);
            return self();
        }

        public T priority(int priority) {
            this.priority = priority;
            return self();
        }

        public int priority() {
            return priority;
        }

        public abstract AbstractContainer push();

        public int tabIndex() {
            return tabIndex;
        }

        public T tabIndex(int tabIndex) {
            this.tabIndex = tabIndex;
            return self();
        }

        public boolean visible() {
            return visible;
        }

        public T visible(boolean visible) {
            this.visible = visible;
            this.display = display.withVisible(visible);
            return self();
        }

        public Style style() {
            return style != null ? style : Style.defaultStyle();
        }

        public T style(String style) {
            return style(ThemeRegistry.current().widgetTheme(style));
        }

        public T style(Style style) {
            this.style = style;
            return self();
        }

        public T zIndex(int z) {
            display = display.withZIndex(z);
            return self();
        }

        public int zIndex() {
            return display.zIndex();
        }
    }
}
