package ninja.crinkle.mod.client.gui.widgets;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import ninja.crinkle.mod.client.color.Color;
import ninja.crinkle.mod.client.gui.builders.GenericBuilder;
import ninja.crinkle.mod.client.gui.events.*;
import ninja.crinkle.mod.client.gui.managers.DragManager;
import ninja.crinkle.mod.client.gui.managers.EventManager;
import ninja.crinkle.mod.client.gui.managers.GuiManager;
import ninja.crinkle.mod.client.gui.properties.Point;
import ninja.crinkle.mod.client.gui.properties.Rect;
import ninja.crinkle.mod.client.gui.properties.Sizing;
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

import java.util.Comparator;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

public abstract class AbstractWidget implements Renderable, Widget, EventNode,
        ThemeRenderable, GuiEventListener {
    private final GuiManager manager;
    private Predicate<AbstractWidget> activePredicate;
    private WidgetBehavior behavior;
    private WidgetDisplay display;
    private double dragAccumX;
    private double dragAccumY;
    private EnumSet<Sizing> horizontalSizing;
    private int minHeight;
    private int minWidth;
    private String name;
    private AbstractContainer parent;
    private int priority;
    private Rect rect = Rect.ZERO;
    private float stretchRatio;
    private Style style;
    private String styleId;
    private int tabIndex;
    private EnumSet<Sizing> verticalSizing;

    protected AbstractWidget(@NotNull AbstractBuilder<?> builder) {
        this.activePredicate = builder.activePredicate();
        this.name = builder.name();
        this.styleId = builder.styleId();
        this.style = builder.resolvedStyle();
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

        registerEventHandlers();
    }

    public AbstractWidget() {
        this.activePredicate = null;
        this.name = "Unnamed_" + hashCode();
        this.style = null;
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

    public static int zIndexOf(AbstractWidget widget) {
        return widget.zIndex();
    }

    public int zIndex() {
        return display().zIndex();
    }

    public WidgetDisplay display() {
        return display;
    }

    public boolean active() {
        if (activePredicate != null) {
            final boolean result = activePredicate.test(this);
            if (result != behavior().active()) {
                active(result);
            }
        }
        return behavior().active();
    }

    public void active(boolean active) {
        behavior(behavior().withActive(active));
    }

    public void active(Predicate<AbstractWidget> activePredicate) {
        this.activePredicate = activePredicate;
    }

    public void addListener(EventNode listener) {
        eventManager().addListener(listener);
    }

    @Override
    public EventManager eventManager() {
        return manager().eventManager();
    }

    public int priority() {
        return priority;
    }

    public GuiManager manager() {
        return manager;
    }

    public float alpha() {
        return display().alpha();
    }

    @SuppressWarnings("unused")
    public void alpha(float alpha) {
        display(display().withAlpha(alpha));
    }

    public void display(WidgetDisplay display) {
        this.display = display;
    }

    public StyleVariant appearance() {
        Style s = style();
        final StyleVariant current;
        if (s != null) {
            current = Style.Variant.from(this).stream()
                    .sorted(Comparator.comparingInt(Style.Variant::rank).reversed())
                    .map(s::variant)
                    .filter(Objects::nonNull)
                    .reduce(StyleVariant::coalesce)
                    .orElse(StyleVariant.EMPTY);
        } else {
            current = StyleVariant.EMPTY;
        }
        return current;
    }

    public Style style() {
        if (styleId != null) {
            return ThemeRegistry.current().style(styleId);
        }
        return style;
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

    public boolean draggable() {
        return behavior().draggable();
    }

    public void draggable(boolean draggable) {
        behavior(behavior().withDraggable(draggable));
    }

    public void behavior(WidgetBehavior behavior) {
        final WidgetBehavior previous = this.behavior();
        if (previous.equals(behavior)) return;
        this.behavior = behavior;
        behaviorChanged(previous);
    }

    public WidgetBehavior behavior() {
        return behavior;
    }

    public void behaviorChanged(WidgetBehavior previous) {
        BehaviorChangedEvent event = new BehaviorChangedEvent(this, behavior(), previous);
        dispatchEvent(event);
    }

    public void dispatchEvent(AbstractEvent event) {
        eventManager().dispatchEvent(event);
    }

    public boolean dragged() {
        return behavior().dragged();
    }

    public void dragged(boolean dragged) {
        if (dragged == dragged()) return;
        behavior(behavior().withDragged(dragged));
    }

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

    public boolean focusable() {
        return behavior().focusable();
    }

    @SuppressWarnings("unused")
    public void focusable(boolean focusable) {
        behavior(behavior().withFocusable(focusable));
    }

    public void focused(boolean focused) {
        if (focused != focused()) {
            if (focused) {
                dispatchEvent(new FocusEnteredEvent(this, true, this));
            } else {
                dispatchEvent(new FocusLeftEvent(this, false, this));
            }
        }
        behavior(behavior().withFocused(focused && active() && visible()));
    }

    public boolean focused() {
        return behavior().focused();
    }

    public EnumSet<Sizing> horizontalSizing() {
        return horizontalSizing;
    }

    public void horizontalSizing(Sizing... flags) {
        this.horizontalSizing = Sizing.of(flags);
    }

    private boolean hoverable() {
        return behavior().hoverable();
    }

    @SuppressWarnings("unused")
    public void hoverable(boolean hoverable) {
        behavior(behavior().withHoverable(hoverable));
    }

    public boolean hovered() {
        return behavior().hovered();
    }

    public void hovered(boolean hovered) {
        if (!behavior().hoverable())
            return;
        if (hovered != hovered()) {
            Point mouse = ClientUtil.getMousePosition();
            dispatchEvent(new HoverEvent(this, mouse.x(), mouse.y(), hovered));
        }
        behavior(behavior().withHovered(hovered && active() && visible()));
    }

    protected void init() {
    }

    public void layoutChanged() {
        LayoutChangedEvent event = new LayoutChangedEvent(this);
        dispatchEvent(event);
    }

    public int minimumHeight() {
        return minHeight;
    }

    public void minimumSize(int width, int height) {
        this.minWidth = width;
        this.minHeight = height;
    }

    public int minimumWidth() {
        return minWidth;
    }

    public boolean mouseOver(Point position) {
        return rect.contains(position.x(), position.y());
    }

    public void name(String name) {
        this.name = name;
    }

    public String name() {
        return name;
    }

    @Override
    public Rect rect() {
        return rect;
    }

    @Override
    public abstract void renderContent(ThemeGraphics graphics, Point pMouse, Rect renderedRect, float pPartialTick);

    @Override
    public void renderDebug(ThemeGraphics pGuiGraphics) {
        if (parent().isEmpty()) return;
        pGuiGraphics.drawRect(rect, Color.GREEN, zIndex());
    }

    private int nextPriority() {
        return parent().map(abstractContainer -> abstractContainer.children().stream()
                        .mapToInt(AbstractWidget::priority).max()
                        .orElse(abstractContainer.priority()) - EventManager.PRIORITY_STEP)
                .orElseGet(() -> EventManager.PRIORITY_STEP);
    }

    private int nextZIndex() {
        return parent().map(abstractContainer -> abstractContainer.children().stream()
                .mapToInt(AbstractWidget::zIndex).max()
                .orElse(abstractContainer.zIndex()) + DragManager.Z_STEP).orElse(DragManager.Z_STEP);
    }

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

    public void onDragStarted(DragStartedEvent event) {
        if (event.cancelled()) return;
        if (event.widget() == this) {
            dragAccumX = 0;
            dragAccumY = 0;
            dragged(true);
            event.consumer(this);
        }
    }

    public void onDropped(DroppedEvent event) {
        if (event.cancelled()) return;
        if (event.widget() == this) {
            dragged(false);
            pressed(false);
            event.consumer(this);
        }
    }

    public void onMousePressed(MousePressedEvent event) {
        if (event.cancelled()) return;
        if (mouseOver(event.position())) {
            if (event.isLeftButton()) {
                AbstractWidget topMost =
                        (AbstractWidget) event.listeners().stream()
                                .min(Comparator.comparingInt(EventNode::priority))
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

    public void onMouseReleased(MouseReleasedEvent event) {
        if (event.cancelled()) return;
        if (pressed()) {
            pressed(false);
        }
    }

    public void onMove(MoveEvent event) {
        if (event.cancelled()) return;
        if (!hoverable()) return;
        AbstractWidget topMost =
                (AbstractWidget) event.listeners().stream()
                        .min(Comparator.comparingInt(EventNode::priority))
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

    public Optional<AbstractContainer> parent() {
        return Optional.ofNullable(parent);
    }

    public void parent(AbstractContainer parent) {
        this.parent = parent;
    }

    public AbstractContainer parentOrThrow() {
        if (parent == null) {
            throw new IllegalStateException("Parent cannot be null");
        }
        return parent;
    }

    public boolean pressable() {
        return behavior().pressable();
    }

    @SuppressWarnings("unused")
    public void pressable(boolean pressable) {
        behavior(behavior().withPressable(pressable));
    }

    public boolean pressed() {
        return behavior().pressed();
    }

    public void pressed(boolean pressed) {
        behavior(behavior().withPressed(pressed && active() && visible()));
    }

    public void priority(int priority) {
        this.priority = priority;
    }

    protected void registerEventHandlers() {
        eventManager().addListener(DragEvent.KEY, priority(), this::onDrag);
        eventManager().addListener(DragStartedEvent.KEY, priority(), this::onDragStarted);
        eventManager().addListener(DroppedEvent.KEY, priority(), this::onDropped);
        eventManager().addListener(MousePressedEvent.KEY, priority(), this::onMousePressed);
        eventManager().addListener(MouseReleasedEvent.KEY, priority(), this::onMouseReleased);
        eventManager().addListener(MoveEvent.KEY, priority(), this::onMove);
    }

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
        Style s = style();
        if (!ClientConfig.debug() && s != null) s.render(graphics, rect, this);
        renderContent(graphics, pMouse, rect, pPartialTick);
        if (ClientConfig.debug()) {
            renderDebug(graphics);
        }
    }

    public boolean repositionable() {
        return behavior().repositionable();
    }

    public void repositionable(boolean repositionable) {
        behavior(behavior().withRepositionable(repositionable));
    }

    public void setRect(Rect rect) {
        this.rect = rect;
    }

    @Override
    public void setFocused(boolean pFocused) {
        focused(pFocused);
    }

    public float stretchRatio() {
        return stretchRatio;
    }

    public void stretchRatio(float ratio) {
        this.stretchRatio = ratio;
    }

    public void style(String styleId) {
        this.styleId = styleId;
        this.style = null;
    }

    public void style(Style style) {
        this.style = style;
        this.styleId = null;
    }

    @Override
    public boolean isFocused() {
        return focused();
    }

    public void tabIndex(int tabIndex) {
        if (tabIndex != tabIndex())
            dispatchEvent(new TabIndexEvent(this, focused(), tabIndex(), tabIndex, this));
        this.tabIndex = tabIndex;
    }

    public int tabIndex() {
        return tabIndex;
    }

    protected void tick() {
    }

    public EnumSet<Sizing> verticalSizing() {
        return verticalSizing;
    }

    public void verticalSizing(Sizing... flags) {
        this.verticalSizing = Sizing.of(flags);
    }

    public boolean visible() {
        return this.display.visible() && (parent().isPresent() && parent().get().visible() || parent().isEmpty());
    }

    public void visible(boolean visible) {
        this.display = display().withVisible(visible);
    }

    public AbstractWidget visualCopy(AbstractContainer newParent) {
        throw new UnsupportedOperationException(
                getClass().getSimpleName() + " does not support visualCopy");
    }

    public void zIndex(int zIndex) {
        if (zIndex >= DragManager.Z_MAX) {
            throw new IllegalArgumentException("Z-index cannot be set to maximum value");
        }
        display(display().withZIndex(zIndex));
    }

    @SuppressWarnings({"unused", "UnusedReturnValue"})
    public abstract static class AbstractBuilder<T extends AbstractBuilder<T>> extends GenericBuilder<T,
            AbstractWidget> {
        private final GuiManager manager;
        private final AbstractContainer parent;
        private Predicate<AbstractWidget> activePredicate;
        private WidgetBehavior behavior = new WidgetBehavior();
        private WidgetDisplay display = new WidgetDisplay();
        private EnumSet<Sizing> horizontalSizing = EnumSet.of(Sizing.Fill);
        private int minHeight = 0;
        private int minWidth = 0;
        private String name = "Unnamed_" + hashCode();
        private int priority = 0;
        private float stretchRatio = 1.0f;
        private Style style = null;
        private String styleId = null;
        private int tabIndex = 0;
        private EnumSet<Sizing> verticalSizing = EnumSet.of(Sizing.Fill);
        private boolean visible = true;

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

        public T behavior(WidgetBehavior behavior) {
            this.behavior = behavior;
            return self();
        }

        public abstract AbstractWidget build();

        public WidgetDisplay display() {
            return display;
        }

        public T display(WidgetDisplay display) {
            this.display = display;
            return self();
        }

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

        public T horizontalSizing(Sizing... flags) {
            this.horizontalSizing = Sizing.of(flags);
            return self();
        }

        public EnumSet<Sizing> horizontalSizing() {
            return horizontalSizing;
        }

        public boolean hoverable() {
            return behavior.hoverable();
        }

        public T hoverable(boolean hoverable) {
            behavior = behavior.withHoverable(hoverable);
            return self();
        }

        public GuiManager manager() {
            return manager;
        }

        public int minHeight() {
            return minHeight;
        }

        public T minSize(int width, int height) {
            this.minWidth = width;
            this.minHeight = height;
            return self();
        }

        public int minWidth() {
            return minWidth;
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

        public T priority(int priority) {
            this.priority = priority;
            return self();
        }

        public int priority() {
            return priority;
        }

        public abstract AbstractContainer push();

        public boolean repositionable() {
            return behavior.repositionable();
        }

        public T repositionable(boolean repositionable) {
            behavior = behavior.withRepositionable(repositionable);
            return self();
        }

        public Style resolvedStyle() {
            if (styleId != null) {
                return ThemeRegistry.current().style(styleId);
            }
            return style;
        }

        public T stretchRatio(float ratio) {
            this.stretchRatio = ratio;
            return self();
        }

        public float stretchRatio() {
            return stretchRatio;
        }

        public T style(String styleId) {
            this.styleId = styleId;
            this.style = null;
            return self();
        }

        public T style(Style style) {
            this.style = style;
            this.styleId = null;
            return self();
        }

        public String styleId() {
            return styleId;
        }

        public int tabIndex() {
            return tabIndex;
        }

        public T tabIndex(int tabIndex) {
            this.tabIndex = tabIndex;
            return self();
        }

        public T verticalSizing(Sizing... flags) {
            this.verticalSizing = Sizing.of(flags);
            return self();
        }

        public EnumSet<Sizing> verticalSizing() {
            return verticalSizing;
        }

        public boolean visible() {
            return visible;
        }

        public T visible(boolean visible) {
            this.visible = visible;
            this.display = display.withVisible(visible);
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