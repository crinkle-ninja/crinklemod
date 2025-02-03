package ninja.crinkle.mod.client.gui.widgets;

import com.mojang.logging.LogUtils;
import ninja.crinkle.mod.client.gui.events.DragEvent;
import ninja.crinkle.mod.client.gui.events.DragStartedEvent;
import ninja.crinkle.mod.client.gui.events.DroppedEvent;
import ninja.crinkle.mod.client.gui.events.sources.InputSource;
import ninja.crinkle.mod.client.gui.layouts.AbstractLayout;
import ninja.crinkle.mod.client.gui.layouts.Layout;
import ninja.crinkle.mod.client.gui.managers.EventManager;
import ninja.crinkle.mod.client.gui.managers.GuiManager;
import ninja.crinkle.mod.client.gui.managers.StateManager;
import ninja.crinkle.mod.client.gui.properties.*;
import ninja.crinkle.mod.client.gui.renderers.ThemeGraphics;
import ninja.crinkle.mod.client.gui.states.references.StateStorageRef;
import ninja.crinkle.mod.client.gui.states.references.ValueRef;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public abstract class AbstractContainer extends AbstractWidget implements InputSource {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final List<AbstractWidget> children = new ArrayList<>();
    private final StateStorageRef stateStorage = StateManager.local();
    private final EventManager eventManager = EventManager.createLocal();
    private ValueRef<Layout> layoutManager;
    private boolean flex;

    protected AbstractContainer(@NotNull AbstractContainerBuilder<?> builder) {
        super(builder);
        this.layoutManager = manager().stateStorage().createValue(Layout.class, builder.layoutManager());
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

    public void clear() {
        deregisterListeners();
        children.clear();
    }

    private void deregisterListeners() {
        children().forEach(EventManager.global()::removeListener);
        children().forEach(l -> manager().eventManager().ifPresent(m -> m.removeListener(l)));
        eventManager().ifPresent(EventManager::clear);
    }

    @Override
    public Optional<EventManager> eventManager() {
        return Optional.ofNullable(eventManager);
    }

    public boolean flex() {
        return flex;
    }

    public void flex(boolean flex) {
        this.flex = flex;
    }

    public Optional<Layout> layoutManager() {
        return Optional.ofNullable(layoutManager.get());
    }

    public void layoutManager(@Nullable Layout layout) {
        this.layoutManager.set(layout);
    }

    public ValueRef<Layout> layoutManagerRef() {
        return layoutManager;
    }

    @Override
    public void onDrag(DragEvent event) {
        super.onDrag(event);
        children().stream().filter(c -> c.position().absolute()).forEach(c -> c.onDrag(event));
        updateLayout();
    }

    @Override
    public void onDragStarted(DragStartedEvent event) {
        super.onDragStarted(event);
        children().forEach(c -> c.onDragStarted(event));
        updateLayout();
    }

    @Override
    public void onDropped(DroppedEvent event) {
        super.onDropped(event);
        children().forEach(c -> c.onDropped(event));
        updateLayout();
    }

    public int totalInnerHeight() {
        if (layoutManager().isPresent() && !renderedPosition().absolute()) {
            return layoutManager().get().totalInnerHeight(this);
        }
        return children().stream().mapToInt(AbstractWidget::totalHeight).sum();
    }

    public int totalInnerWidth() {
        if (layoutManager().isPresent() && !renderedPosition().absolute()) {
            return layoutManager().get().totalInnerWidth(this);
        }
        return children().stream().mapToInt(AbstractWidget::totalWidth).sum();
    }

    public boolean isRoot() {
        return parent().isEmpty();
    }

    protected void registerListener(AbstractWidget widget) {
        for (Scope scope : widget.scopes()) {
            Optional<EventManager> manager = switch (scope) {
                case Global -> Optional.of(EventManager.global());
                case Screen -> manager().eventManager();
                case Local -> eventManager();
            };
            manager.ifPresent(m -> m.addListener(widget));
        }
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

    public StateStorageRef stateStorage() {
        return stateStorage;
    }

    @Override
    public void tick() {
        children().forEach(AbstractWidget::tick);
    }

    @Override
    public void init() {
        super.init();
        children().forEach(AbstractWidget::init);
        updateLayout();
    }

    public void updateLayout() {
        if (layoutManager.get() != null) {
            layoutManager.get().arrange(this);
        }
        children().stream()
                .filter(c -> c instanceof AbstractContainer)
                .map(c -> (AbstractContainer) c)
                .forEach(AbstractContainer::updateLayout);
    }

    public static abstract class AbstractContainerBuilder<T extends AbstractContainerBuilder<T>>
            extends AbstractBuilder<T> {
        private Layout layout;
        private boolean flex = false;

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

        public T flex(boolean flex) {
            this.flex = flex;
            return self();
        }

        public boolean flex() {
            return flex;
        }

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
