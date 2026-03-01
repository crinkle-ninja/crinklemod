package ninja.crinkle.mod.client.gui.widgets;

import com.mojang.logging.LogUtils;
import ninja.crinkle.mod.client.ClientSetup;
import ninja.crinkle.mod.client.gui.events.*;
import ninja.crinkle.mod.client.gui.events.listeners.KeyListener;
import ninja.crinkle.mod.client.gui.events.listeners.LayoutListener;
import ninja.crinkle.mod.client.gui.events.sources.InputSource;
import ninja.crinkle.mod.client.gui.properties.Sizing;
import ninja.crinkle.mod.client.gui.managers.EventManager;
import ninja.crinkle.mod.client.gui.managers.GuiManager;
import ninja.crinkle.mod.client.gui.properties.Point;
import ninja.crinkle.mod.client.gui.properties.Rect;
import ninja.crinkle.mod.client.gui.renderers.ThemeGraphics;
import ninja.crinkle.mod.client.gui.managers.IManagedGUI;
import ninja.crinkle.mod.client.gui.screens.LayoutEditorScreen;
import ninja.crinkle.mod.util.ClientUtil;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public abstract class AbstractContainer extends AbstractWidget implements InputSource, LayoutListener, KeyListener {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final List<AbstractWidget> children = new ArrayList<>();
    private final EventManager eventManager = EventManager.createLocal();
    private int separation;

    protected AbstractContainer(@NotNull AbstractContainerBuilder<?> builder) {
        super(builder);
        this.separation = builder.separation();
    }

    public AbstractContainer() {
        super();
        this.separation = 0;
    }

    // --- Children ---

    public AbstractContainer add(AbstractBuilder<?> builder) {
        return add(builder.build());
    }

    public AbstractContainer add(AbstractWidget widget) {
        if (widget == this) {
            LOGGER.warn("Attempted to add widget to itself: {}", widget);
            return this;
        }
        if (children().contains(widget)) {
            LOGGER.warn("Attempted to add duplicate widget: {}", widget);
            return this;
        }
        children.add(widget);
        registerListener(widget);
        if (widget.parentOrThrow() != this && widget.parentOrThrow() != null) {
            widget.parentOrThrow().remove(widget);
            widget.parent(this);
        }
        return this;
    }

    @SuppressWarnings("unused")
    public AnimatedWidget.Builder addAnimation() {
        return new AnimatedWidget.Builder(this);
    }

    public Button.Builder addButton() {
        return new Button.Builder(this);
    }

    public Container.Builder addContainer() {
        return new Container.Builder(this);
    }

    public List<AbstractWidget> children(Predicate<? super AbstractWidget> predicate) {
        return children().stream().filter(predicate).toList();
    }

    public List<AbstractWidget> children() {
        return children.stream().toList();
    }

    @Override
    public void onKey(KeyEvent event) {
        if (ClientSetup.LAYOUT_EDITOR_KEY != null
                && ClientSetup.LAYOUT_EDITOR_KEY.matches(event.keyCode(), event.scanCode())) {
            if (ClientUtil.getMinecraft().screen instanceof LayoutEditorScreen) return;
            if (manager() instanceof IManagedGUI gui) {
                ClientUtil.getMinecraft().setScreen(new LayoutEditorScreen(gui));
                event.consumed(true);
            }
        }
    }

    public void remove(AbstractWidget widget) {
        EventManager.global().removeListener(widget);
        manager().eventManager().ifPresent(m -> m.removeListener(widget));
        eventManager().ifPresent(m -> m.removeListener(widget));
        children.remove(widget);
        widget.parent(null);
    }

    // --- Visual Copy ---

    protected void visualCopyChildrenInto(AbstractContainer target) {
        for (AbstractWidget child : children()) {
            AbstractWidget copied = child.visualCopy(target);
            target.add(copied);
        }
    }

    // --- Layout ---

    public int separation() {
        return separation;
    }

    public void separation(int separation) {
        this.separation = separation;
    }

    @Override
    public void setRect(Rect rect) {
        super.setRect(rect);
        arrange();
    }

    /**
     * Place children within this container's rect. Subclasses override to implement
     * specific layout strategies (HBox, VBox, etc.). Default does nothing.
     */
    public void arrange() {
        // Default: no arrangement. Subclasses override.
    }

    /**
     * Applies a child's sizing to an allocated rectangle.
     * Per-axis: if FILL → use allocated size; if SHRINK_* → use min size + align within allocated rect.
     */
    protected static Rect fitChildInRect(AbstractWidget child, Rect allocated) {
        int x = allocated.x();
        int y = allocated.y();
        int w = allocated.width();
        int h = allocated.height();

        EnumSet<Sizing> hFlags = child.horizontalSizing();
        EnumSet<Sizing> vFlags = child.verticalSizing();
        int childMinW = child.minimumWidth();
        int childMinH = child.minimumHeight();

        // Horizontal axis
        if (!hFlags.contains(Sizing.Fill)) {
            w = childMinW;
            if (hFlags.contains(Sizing.ShrinkCenter)) {
                x += (allocated.width() - w) / 2;
            } else if (hFlags.contains(Sizing.ShrinkEnd)) {
                x += allocated.width() - w;
            }
            // SHRINK_BEGIN: x stays at allocated.x()
        }

        // Vertical axis
        if (!vFlags.contains(Sizing.Fill)) {
            h = childMinH;
            if (vFlags.contains(Sizing.ShrinkCenter)) {
                y += (allocated.height() - h) / 2;
            } else if (vFlags.contains(Sizing.ShrinkEnd)) {
                y += allocated.height() - h;
            }
            // SHRINK_BEGIN: y stays at allocated.y()
        }

        return new Rect(x, y, w, h);
    }

    // --- Events ---

    @Override
    public Optional<EventManager> eventManager() {
        return Optional.ofNullable(eventManager);
    }

    @Override
    public void onDrag(DragEvent event) {
        if (!dragged()) return;
        // super.onDrag calls setRect which calls arrange() — that repositions children
        // relative to this container's new rect, so no manual child-moving needed.
        super.onDrag(event);
    }

    @Override
    public void onDragStarted(DragStartedEvent event) {
        super.onDragStarted(event);
        children().forEach(c -> c.onDragStarted(event));
    }

    @Override
    public void onDropped(DroppedEvent event) {
        super.onDropped(event);
        children().forEach(c -> c.onDropped(event));
    }

    @Override
    public void onLayoutChanged(LayoutChangedEvent event) {
        if (event.cancelled() || event.consumed()) return;
        arrange();
    }

    public boolean isRoot() {
        return manager().root() == this;
    }

    protected void registerListener(AbstractWidget widget) {
        manager().eventManager().ifPresent(m -> m.addListener(widget));
        EventManager.global().addListener(widget);
        eventManager().ifPresent(m -> m.addListener(widget));
    }


    // --- Rendering ---

    @Override
    public void renderContent(ThemeGraphics graphics, Point pMouse, Rect renderRect, float pPartialTick) {
        children().stream().sorted(Comparator.comparingInt(AbstractWidget::zIndexOf))
                .forEach(child -> child.render(graphics, pMouse, pPartialTick));
    }

    // --- Lifecycle ---

    @Override
    public void init() {
        super.init();
        arrange();
        children().forEach(AbstractWidget::init);
    }

    @Override
    public void tick() {
        children().forEach(AbstractWidget::tick);
    }

    // --- Object ---

    @Override
    public String toString() {
        return "Container{" +
                "children=[" + children().stream().map(AbstractWidget::toString)
                .collect(Collectors.joining(",")) +
                "](" + children().size() + ")" +
                ", separation=" + separation +
                ", " + super.toString() +
                '}';
    }

    // --- Builder ---

    public static abstract class AbstractContainerBuilder<T extends AbstractContainerBuilder<T>>
            extends AbstractBuilder<T> {
        private int separation = 0;

        public AbstractContainerBuilder(AbstractContainer container) {
            super(container.manager(), container);
        }

        public AbstractContainerBuilder(GuiManager abstractScreen) {
            super(abstractScreen);
        }

        public T separation(int separation) {
            this.separation = separation;
            return self();
        }

        public int separation() {
            return separation;
        }

        @Override
        public abstract AbstractContainer build();

        @Override
        public abstract AbstractContainer push();

        @Override
        protected abstract T self();

        public abstract AbstractContainer pushAndReturn();
    }
}
