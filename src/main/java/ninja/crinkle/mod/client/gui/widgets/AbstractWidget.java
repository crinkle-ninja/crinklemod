package ninja.crinkle.mod.client.gui.widgets;

import com.mojang.logging.LogUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import ninja.crinkle.mod.client.color.Color;
import ninja.crinkle.mod.client.gui.builders.GenericBuilder;
import ninja.crinkle.mod.client.gui.events.*;
import ninja.crinkle.mod.client.gui.events.listeners.EventListener;
import ninja.crinkle.mod.client.gui.events.listeners.FocusListener;
import ninja.crinkle.mod.client.gui.events.listeners.MouseListener;
import ninja.crinkle.mod.client.gui.events.sources.FocusSource;
import ninja.crinkle.mod.client.gui.events.sources.MouseSource;
import ninja.crinkle.mod.client.gui.events.sources.TabIndexSource;
import ninja.crinkle.mod.client.gui.layouts.BoxModel;
import ninja.crinkle.mod.client.gui.layouts.Layout;
import ninja.crinkle.mod.client.gui.managers.DragManager;
import ninja.crinkle.mod.client.gui.managers.EventManager;
import ninja.crinkle.mod.client.gui.managers.GuiManager;
import ninja.crinkle.mod.client.gui.properties.*;
import ninja.crinkle.mod.client.gui.renderers.ThemeGraphics;
import ninja.crinkle.mod.client.gui.states.WidgetBehavior;
import ninja.crinkle.mod.client.gui.states.WidgetDisplay;
import ninja.crinkle.mod.client.gui.states.WidgetLayout;
import ninja.crinkle.mod.client.gui.states.references.Ref;
import ninja.crinkle.mod.client.gui.textures.Texture;
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

public abstract class AbstractWidget implements BoxModel, Renderable, Layout.Widget, MouseSource, MouseListener,
        FocusSource, FocusListener, TabIndexSource {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Ref<WidgetBehavior> behavior;
    private final Ref<WidgetDisplay> display;
    private final Ref<WidgetLayout> layout;
    private final GuiManager manager;
    private final Set<Scope> scopes = new HashSet<>();
    private Predicate<AbstractWidget> activePredicate;
    private String name;
    private AbstractContainer parent;
    private int priority;
    private Style style;
    private int tabIndex;

    protected AbstractWidget(@NotNull AbstractBuilder<?> builder) {
        this.activePredicate = builder.activePredicate();
        this.name = builder.name();
        this.scopes.addAll(builder.scopes());
        this.style = builder.widgetTheme();
        this.tabIndex = builder.tabIndex();
        this.manager = builder.manager();
        this.priority = builder.priority();
        this.parent = builder.parent();
        this.display = builder.display();
        this.behavior = builder.behaviorRef();
        this.layout = builder.layout();
        layout().widget(this);

        if (builder.zIndex() == DragManager.Z_MIN) {
            parent().filter(p -> !p.equals(this)).ifPresent(p -> zIndex(nextZIndex()));
        }

        if (builder.priority() == 0) {
            parent().filter(p -> !p.equals(this)).ifPresent(p -> priority(nextPriority()));
        }
    }

    public WidgetLayout layout() {
        WidgetLayout l = layout.value();
        l.widget(this);
        return l;
    }

    public Position position() {
        return layout().position();
    }

    public void position(Position position) {
        layout(layout().withPosition(position));
    }

    @Override
    public void resetPosition() {
        layout(layout().withPosition(layout.original().position()));
    }

    public Size size() {
        return layout().size();
    }

    public void size(Size size) {
        layout(layout().withSize(size));
    }

    public String name() {
        return name;
    }

    public void layout(WidgetLayout layout) {
        this.layout.value(layout);
    }

    public Optional<AbstractContainer> parent() {
        return Optional.ofNullable(parent);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (AbstractWidget) obj;
        return Objects.equals(this.activePredicate, that.activePredicate) &&
                Objects.equals(this.name, that.name) &&
                Objects.equals(this.scopes, that.scopes) &&
                Objects.equals(this.style, that.style) &&
                this.tabIndex == that.tabIndex &&
                this.priority == that.priority &&
                Objects.equals(this.parent, that.parent) &&
                Objects.equals(this.display, that.display) &&
                Objects.equals(this.behavior, that.behavior) &&
                Objects.equals(this.layout, that.layout);
    }

    @Override
    public String toString() {
        return "AbstractWidget{" + "active=" + active() + ", activePredicate=" + activePredicate + ", alpha=" + alpha() + ", border=" + border() + ", size=" + size() + ", clicked=" + pressed() + ", draggable=" + draggable() + ", dragged=" + dragged() + ", focused=" + focused() + ", hovered=" + hovered() + ", margin=" + margin() + ", name='" + name() + "'" + ", padding=" + padding() + ", position=" + position() + ", status=" + status() + ", tabIndex=" + tabIndex() + ", visible=" + visible() + ", widgetTheme=" + style + ", zIndex=" + zIndex() + '}';
    }

    public boolean active() {
        if (activePredicate != null) {
            return activePredicate.test(this);
        }
        return behavior().active();
    }

    public float alpha() {
        return display().alpha();
    }

    public boolean draggable() {
        return behavior().draggable();
    }

    public boolean focused() {
        return behavior().focused();
    }

    public boolean hovered() {
        return behavior().hovered();
    }

    public boolean pressed() {
        return behavior().pressed();
    }

    @Override
    public boolean mouseOver(Point position) {
        return layout().boxes().rendered().borderBox().contains(position);
    }

    @Override
    public boolean dragged() {
        return behavior().dragged();
    }

    @Override
    public Margin margin() {
        return layout().margin();
    }

    @Override
    public void margin(Margin margin) {
        layout(layout().withMargin(margin));
    }

    @Override
    public Padding padding() {
        return layout().padding();
    }

    @Override
    public void padding(Padding padding) {
        layout(layout().withPadding(padding));
    }

    @Override
    public Border border() {
        return layout().border();
    }

    @Override
    public void border(Border border) {
        layout(layout().withBorder(border));
    }

    @Override
    public Position renderedPosition() {
        Position position = layout().position();
        if (position.absolute()) return position;
        return position.withBase(parent().map(p -> p.layout().boxes().rendered().contentBox().position().point()).orElse(ImmutablePoint.ZERO)).withType(Position.Type.Absolute);
    }

    @Override
    public Position parentPosition() {
        return Optional.ofNullable(parentOrThrow()).map(AbstractWidget::renderedPosition).orElse(Position.absolute(0,
                0));
    }

    @Override
    public int totalHeight() {
        return this.layout().size().height();
    }

    @Override
    public int totalWidth() {
        return this.layout().size().width();
    }

    @Override
    public abstract void renderContent(ThemeGraphics graphics, Point pMouse, Box renderedBox, float pPartialTick);

    @Override
    public void renderDebug(ThemeGraphics pGuiGraphics) {
        if (parent().isEmpty()) return;
        Box pOuter = layout().boxes().rendered().box();
        Box pBorder = layout().boxes().rendered().borderBox();
        Box pBackground = layout().boxes().rendered().backgroundBox();
        Box pPadding = layout().boxes().rendered().paddingBox();
        Box pInner = layout().boxes().rendered().contentBox();
        // Render an outline around the widget
        pGuiGraphics.drawBox(pOuter.subtract(1, 1, -2, -2), Color.WHITE, zIndex());
        pGuiGraphics.drawBox(pOuter, Color.RED, zIndex());
        pGuiGraphics.drawBox(pBorder, Color.GREEN, zIndex());
        pGuiGraphics.drawBox(pBackground, Color.CYAN, zIndex());
        pGuiGraphics.drawBox(pPadding, Color.YELLOW, zIndex());
        pGuiGraphics.drawBox(pInner, Color.BLUE, zIndex());
    }

    public AbstractContainer parentOrThrow() {
        if (parent == null) {
            throw new IllegalStateException("Parent cannot be null");
        }
        return parent;
    }

    public Style.Variant status() {
        return Style.Variant.from(this).stream().max(Comparator.comparingInt(Style.Variant::rank)).orElse(Style.Variant.base);
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

    public boolean visible() {
        return this.display.value().visible();
    }

    public WidgetBehavior behavior() {
        return behavior.value();
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

    public void behavior(WidgetBehavior behavior) {
        this.behavior.value(behavior);
    }

    public void zIndex(int zIndex) {
        if (zIndex >= DragManager.Z_MAX) {
            throw new IllegalArgumentException("Z-index cannot be set to maximum value");
        }
        display(display().withZIndex(zIndex));
    }

    private int nextZIndex() {
        return parent().map(abstractContainer -> abstractContainer.children().stream().mapToInt(AbstractWidget::zIndex).max().orElse(abstractContainer.zIndex()) + DragManager.Z_STEP).orElse(DragManager.Z_STEP);
    }

    public void priority(int priority) {
        this.priority = priority;
    }

    private int nextPriority() {
        return parent().map(abstractContainer -> abstractContainer.children().stream().mapToInt(AbstractWidget::priority).max().orElse(abstractContainer.priority()) - EventManager.PRIORITY_STEP).orElseGet(() -> EventManager.PRIORITY_STEP);
    }

    public void display(WidgetDisplay display) {
        this.display.value(display);
    }

    public WidgetDisplay display() {
        return display.value();
    }

    public int zIndex() {
        return display().zIndex();
    }

    public int priority() {
        return priority;
    }

    public AbstractWidget() {
        this.activePredicate = null;
        this.name = "Unnamed_" + hashCode();
        this.scopes.addAll(List.of(Scope.Local, Scope.Screen));
        this.style = Style.getDefault();
        this.tabIndex = 0;
        this.manager = GuiManager.create();
        this.priority = 1;
        this.parent = null;
        this.display = null;
        this.behavior = null;
        this.layout = null;
    }

    public static int zIndexOf(AbstractWidget widget) {
        return widget.zIndex();
    }

    public void active(boolean active) {
        behavior(behavior().withActive(active));
    }

    public void active(Predicate<AbstractWidget> activePredicate) {
        this.activePredicate = activePredicate;
    }

    public void addListener(EventListener listener) {
        eventManager().ifPresent(manager -> manager.addListener(listener));
    }

    public void alpha(float alpha) {
        display(display().withAlpha(alpha));
    }

    public Ref<WidgetBehavior> behaviorRef() {
        return behavior;
    }

    @SafeVarargs
    public final Optional<AbstractContainer> bottomMostParent(Predicate<AbstractContainer>... predicates) {
        AbstractContainer parent = parentOrThrow();
        while (parent.parent().isPresent()) {
            final AbstractContainer finalParent = parent;
            if (Arrays.stream(predicates).allMatch(predicate -> predicate.test(finalParent))) {
                return Optional.of(parent);
            }
            parent = parent.parentOrThrow();
        }
        return Optional.of(parent);
    }

    public Ref<WidgetDisplay> displayRef() {
        return display;
    }

    public void draggable(boolean draggable) {
        behavior(behavior().withDraggable(draggable));
    }

    @Override
    public Optional<EventManager> eventManager(Scope scope) {
        return switch (scope) {
            case Screen -> manager().eventManager();
            case Local -> eventManager();
            case Global -> Optional.ofNullable(EventManager.global());
        };
    }

    public GuiManager manager() {
        return manager;
    }

    public boolean focusable() {
        return behavior().focusable();
    }

    public void focusable(boolean focusable) {
        behavior(behavior().withFocusable(focusable));
    }

    private boolean hoverable() {
        return behavior().hoverable();
    }

    public void hoverable(boolean hoverable) {
        behavior(behavior().withHoverable(hoverable));
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

    protected void init() {
    }

    public Ref<WidgetLayout> layoutRef() {
        return layout;
    }

    public void name(String name) {
        this.name = name;
    }

    @Override
    public void onDrag(DragEvent event) {
        if (event.cancelled()) return;
        if (!dragged()) return;
        Position newPosition = position().offsetBy(event.dragX(), event.dragY());
        if (newPosition.equals(position())) return;
        layout(layout().withPosition(newPosition));
        event.consumer(this);
    }

    @Override
    public void onDragStarted(DragStartedEvent event) {
        if (event.cancelled()) return;
        if (event.widget() == this) {
            dragged(true);
            event.consumer(this);
        }
    }

    public void dragged(boolean dragged) {
        if (dragged == dragged()) return;
        behavior(behavior().withDragged(dragged));
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
                        (AbstractWidget) event.listeners().stream().min(Comparator.comparingInt(EventListener::priority)).filter(listener -> listener instanceof AbstractWidget).orElse(null);
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

    public boolean pressable() {
        return behavior().pressable();
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

    public void pressed(boolean pressed) {
        behavior(behavior().withPressed(pressed));
    }

    public void parent(AbstractContainer parent) {
        this.parent = parent;
    }

    public void pressable(boolean pressable) {
        behavior(behavior().withPressable(pressable));
    }

    /**
     * @param graphics     the GuiGraphics object used for rendering.
     * @param pMouseX      the x-coordinate of the mouse cursor.
     * @param pMouseY      the y-coordinate of the mouse cursor.
     * @param pPartialTick the partial tick time.
     *
     * @implNote For legacy use only, use AbstractWidget#render(ThemeGraphics, Point, float) instead
     */
    @Override
    public void render(@NotNull GuiGraphics graphics, int pMouseX, int pMouseY, float pPartialTick) {
        render(new ThemeGraphics(graphics, ThemeAtlas.getAtlas()), new MutablePoint(pMouseX, pMouseY), pPartialTick);
    }

    public void render(@NotNull ThemeGraphics graphics, Point pMouse, float pPartialTick) {
        if (!visible()) return;
        Box borderBox = layout().boxes().rendered().borderBox();
        border().render(graphics, borderBox, zIndex() - 10);
        if (!ClientConfig.debug()) widgetTheme().render(graphics, borderBox, this);
        renderContent(graphics, pMouse, layout().boxes().rendered().contentBox(), pPartialTick);
        if (ClientConfig.debug()) {
            renderDebug(graphics);
        }
    }

    public Style widgetTheme() {
        return style;
    }

    public void resetSize() {
        layout(layout().withSize(layout.original().size()));
    }

    protected void resize(Size size) {
        layout(layout().withSize(size));
    }

    public List<Scope> scopes() {
        return scopes.stream().toList();
    }

    public Box screenPositionOf(Box box) {
        assert box != null && box.position().relative();
        return box.add(renderedPosition());
    }

    public Border textureBorder() {
        Size top =
                Optional.ofNullable(appearance().getBackgroundTexture()).orElse(Texture.EMPTY).boundsOf(Texture.Slice.Location.top);
        Size right =
                Optional.ofNullable(appearance().getBackgroundTexture()).orElse(Texture.EMPTY).boundsOf(Texture.Slice.Location.right);
        Size bottom =
                Optional.ofNullable(appearance().getBackgroundTexture()).orElse(Texture.EMPTY).boundsOf(Texture.Slice.Location.bottom);
        Size left =
                Optional.ofNullable(appearance().getBackgroundTexture()).orElse(Texture.EMPTY).boundsOf(Texture.Slice.Location.left);
        return new Border(top.height(), right.width(), bottom.height(), left.width(), Color.BLACK);
    }

    public StyleVariant appearance() {
        return Style.Variant.from(this).stream()
                .sorted(Comparator.comparingInt(Style.Variant::rank))
                .map(variant -> widgetTheme().getAppearance(variant))
                .filter(Objects::nonNull)
                .reduce(StyleVariant::coalesce)
                .orElse(StyleVariant.EMPTY);
    }

    protected void tick() {
    }

    public void visible(boolean visible) {
        this.display.value(display.value().withVisible(visible));
    }

    public void widgetTheme(Style style) {
        this.style = style;
    }

    @SuppressWarnings("unused")
    public abstract static class AbstractBuilder<T extends AbstractBuilder<T>> extends GenericBuilder<T,
            AbstractWidget> {
        private final GuiManager manager;
        private final AbstractContainer parent;
        private Predicate<AbstractWidget> activePredicate;
        private WidgetBehavior behavior = new WidgetBehavior();
        private WidgetDisplay display = new WidgetDisplay();
        private WidgetLayout layout = new WidgetLayout();
        private String name = "Unnamed_" + hashCode();
        private int priority = 0;
        private List<Scope> scopes = List.of(Scope.Local, Scope.Screen);
        private Style style = Style.getDefault();
        private int tabIndex = 0;
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

        public T absolute(int x, int y) {
            return absolute(new ImmutablePoint(x, y));
        }

        public T absolute(ImmutablePoint position) {
            layout = layout.withPosition(Position.absolute(position));
            return self();
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

        public Ref<WidgetBehavior> behaviorRef() {
            return new Ref<>(behavior);
        }

        public Ref<WidgetDisplay> display() {
            return new Ref<>(display);
        }

        public Ref<WidgetLayout> layout() {
            return new Ref<>(layout);
        }

        public GuiManager manager() {
            return manager;
        }

        public T border(int all, Color color) {
            return border(new Border(all, color));
        }

        public T border(Border border) {
            layout = layout.withBorder(border);
            return self();
        }

        public T border(int top, int right, int left, int bottom, Color color) {
            return border(new Border(top, right, bottom, left, color));
        }

        public T border(int horizontal, int vertical, Color color) {
            return border(new Border(horizontal, vertical, color));
        }

        public T border(int all) {
            return border(new Border(all, Color.of(0)));
        }

        public Border border() {
            return layout.border();
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

        public Margin margin() {
            return layout.margin();
        }

        public T margin(int all) {
            return margin(new Margin(all));
        }

        public T margin(Margin margin) {
            layout = layout.withMargin(margin);
            return self();
        }

        public T margin(int top, int right, int bottom, int left) {
            return margin(new Margin(top, right, bottom, left));
        }

        public T margin(int vertical, int horizontal) {
            return margin(new Margin(vertical, horizontal));
        }

        public String name() {
            return name;
        }

        public T name(String name) {
            this.name = name;
            return self();
        }

        public Padding padding() {
            return layout.padding();
        }

        public T padding(int all) {
            return padding(new Padding(all));
        }

        public T padding(Padding padding) {
            layout = layout.withPadding(padding);
            return self();
        }

        public T padding(int vertical, int horizontal) {
            return padding(new Padding(vertical, horizontal));
        }

        public T padding(int top, int right, int bottom, int left) {
            return padding(new Padding(top, right, bottom, left));
        }

        public AbstractContainer parent() {
            return parent;
        }

        public Position position() {
            return layout.position();
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

        public T relative(int x, int y) {
            return relative(new ImmutablePoint(x, y));
        }

        public T relative(ImmutablePoint position) {
            return position(Position.relative(position));
        }

        public T position(Position position) {
            layout = layout.withPosition(position);
            return self();
        }

        public T relative() {
            return relative(new ImmutablePoint(0, 0));
        }

        public T scopes(Scope... scopes) {
            this.scopes = Arrays.asList(scopes);
            return self();
        }

        public List<Scope> scopes() {
            return scopes;
        }

        public T scopes(List<Scope> scopes) {
            this.scopes = scopes;
            return self();
        }

        public T size(int width, int height) {
            return size(new Size(width, height));
        }

        public T size(Size size) {
            layout = layout.withSize(size);
            return self();
        }

        public T size(int all) {
            return size(new Size(all, all));
        }

        public Size size() {
            return layout.size();
        }

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
            return self();
        }

        public Style widgetTheme() {
            return style;
        }

        public T widgetTheme(String widgetTheme) {
            return widgetTheme(ThemeRegistry.current().getWidgetTheme(widgetTheme));
        }

        public T widgetTheme(Style style) {
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
