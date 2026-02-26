package ninja.crinkle.mod.client.gui.widgets;

import com.mojang.logging.LogUtils;
import ninja.crinkle.mod.client.gui.events.DragEvent;
import ninja.crinkle.mod.client.gui.events.DragStartedEvent;
import ninja.crinkle.mod.client.gui.events.DroppedEvent;
import ninja.crinkle.mod.client.gui.events.LayoutChangedEvent;
import ninja.crinkle.mod.client.gui.events.listeners.LayoutListener;
import ninja.crinkle.mod.client.gui.events.sources.InputSource;
import ninja.crinkle.mod.client.gui.layouts.AbstractLayout;
import ninja.crinkle.mod.client.gui.layouts.Layout;
import ninja.crinkle.mod.client.gui.managers.EventManager;
import ninja.crinkle.mod.client.gui.managers.GuiManager;
import ninja.crinkle.mod.client.gui.properties.*;
import ninja.crinkle.mod.client.gui.renderers.ThemeGraphics;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public abstract class AbstractContainer extends AbstractWidget implements InputSource, LayoutListener {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final List<AbstractWidget> children = new ArrayList<>();
    private final EventManager eventManager = EventManager.createLocal();
    private Layout layoutManager;

    protected AbstractContainer(@NotNull AbstractContainerBuilder<?> builder) {
        super(builder);
        this.layoutManager = builder.layoutManager();
    }

    public AbstractContainer() {
        super();
    }

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

    @SuppressWarnings( "unused" )
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
    public Optional<EventManager> eventManager() {
        return Optional.ofNullable(eventManager);
    }

    public Optional<Layout> layoutManager() {
        return Optional.ofNullable(layoutManager);
    }

    @Override
    public void onDrag(DragEvent event) {
        super.onDrag(event);
        children().stream().filter(c -> c.layout().position().absolute()).forEach(c -> c.onDrag(event));
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

    public boolean isRoot() {
        return manager().root() == this;
    }

    protected void registerListener(AbstractWidget widget) {
        manager().eventManager().ifPresent(m -> m.addListener(widget));
        EventManager.global().addListener(widget);
        eventManager().ifPresent(m -> m.addListener(widget));
    }

    public void remove(AbstractWidget widget) {
        EventManager.global().removeListener(widget);
        manager().eventManager().ifPresent(m -> m.removeListener(widget));
        eventManager().ifPresent(m -> m.removeListener(widget));
        children.remove(widget);
        widget.parent(null);
    }

    @Override
    public void renderContent(ThemeGraphics graphics, Point pMouse, Box renderBox, float pPartialTick) {
        children().stream().sorted(Comparator.comparingInt(AbstractWidget::zIndexOf))
                .forEach(child -> child.render(graphics, pMouse, pPartialTick));
    }

    @Override
    public String toString() {
        return "Container{" +
                "children=[" + children().stream().map(AbstractWidget::toString)
                .collect(Collectors.joining(",")) +
                "](" + children().size() + ")" +
                ", layout=" + layoutManager +
                ", " + super.toString() +
                '}';
    }

    @Override
    public void onLayoutChanged(LayoutChangedEvent event) {
        if (event.cancelled() || event.consumed() || event.old().equals(layout())) return;
        updateLayout();
    }

    @Override
    public void tick() {
        children().forEach(AbstractWidget::tick);
    }

    @Override
    public void init() {
        super.init();
        children().forEach(AbstractWidget::init);
    }

    public void updateLayout() {
        if (cachedBoxes() == null) {
            LOGGER.warn("Attempted to update layout before cached boxes were set for container: {}", this.name());
            return;
        }
        if (layoutManager().isPresent()) {
            layoutManager().get().arrange(this);
        }
        children(c -> c instanceof AbstractContainer)
                .forEach(c -> ((AbstractContainer) c).updateLayout());
    }

    public static abstract class AbstractContainerBuilder<T extends AbstractContainerBuilder<T>>
            extends AbstractBuilder<T> {
        private Layout layout;

        public AbstractContainerBuilder(AbstractContainer container) {
            super(container.manager(), container);
        }

        public AbstractContainerBuilder(GuiManager abstractScreen) {
            super(abstractScreen);
        }

        @Override
        public abstract AbstractContainer build();

        @Override
        public abstract AbstractContainer push();

        public T layoutManager(AbstractLayout.AbstractBuilder<?> layout) {
            return layoutManager(layout.build());
        }

        public T layoutManager(Layout layout) {
            this.layout = layout;
            return self();
        }

        @Override
        protected abstract T self();

        public Layout layoutManager() {
            return layout;
        }

        public abstract AbstractContainer pushAndReturn();
    }
}
