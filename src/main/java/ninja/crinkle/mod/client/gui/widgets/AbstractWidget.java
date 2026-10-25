package ninja.crinkle.mod.client.gui.widgets;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;
import ninja.crinkle.mod.client.color.Color;
import ninja.crinkle.mod.client.gui.builders.GenericBuilder;
import ninja.crinkle.mod.client.gui.managers.DragManager;
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
import ninja.crinkle.mod.client.gui.themes.Theme;
import ninja.crinkle.mod.client.gui.themes.ThemeRegistry;
import ninja.crinkle.mod.config.ClientConfig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;

public abstract class AbstractWidget implements Renderable, Widget, ThemeRenderable, GuiEventListener {
    private final GuiManager manager;
    private Predicate<AbstractWidget> activePredicate;
    private Predicate<AbstractWidget> visiblePredicate;
    private WidgetBehavior behavior;
    private WidgetDisplay display;
    private EnumSet<Sizing> horizontalSizing;
    private int minHeight;
    private int minWidth;
    private String name;
    private AbstractContainer parent;
    private Rect rect = Rect.ZERO;
    private float stretchRatio;
    private String styleId;
    private final int tabIndex;
    private EnumSet<Sizing> verticalSizing;
    private final Color debugColor;
    private List<Component> tooltip;

    protected AbstractWidget(@NotNull AbstractBuilder<?> builder) {
        this.activePredicate = builder.activePredicate();
        this.visiblePredicate = builder.visiblePredicate();
        this.name = builder.name();
        this.styleId = builder.styleId();
        this.tabIndex = builder.tabIndex();
        this.manager = builder.manager();
        this.parent = builder.parent();
        this.display = builder.display();
        this.behavior = builder.behavior();
        this.minWidth = builder.minWidth();
        this.minHeight = builder.minHeight();
        this.horizontalSizing = builder.horizontalSizing();
        this.verticalSizing = builder.verticalSizing();
        this.stretchRatio = builder.stretchRatio();
        this.debugColor = Color.random();
        this.tooltip = builder.tooltip();
    }

    public AbstractWidget() {
        this.activePredicate = null;
        this.visiblePredicate = null;
        this.name = "Unnamed_" + hashCode();
        this.tabIndex = 0;
        this.manager = GuiManager.create();
        this.parent = null;
        this.display = new WidgetDisplay();
        this.behavior = new WidgetBehavior();
        this.minWidth = 0;
        this.minHeight = 0;
        this.horizontalSizing = EnumSet.of(Sizing.Fill);
        this.verticalSizing = EnumSet.of(Sizing.Fill);
        this.stretchRatio = 1.0f;
        this.debugColor = Color.random();
    }

    @SuppressWarnings("unused")
    public Predicate<AbstractWidget> visiblePredicate() {
        return visiblePredicate;
    }

    @SuppressWarnings("unused")
    public void visiblePredicate(Predicate<AbstractWidget> visiblePredicate) {
        this.visiblePredicate = visiblePredicate;
    }

    public int zOffset() {
        return display().zOffset();
    }

    @SuppressWarnings("unused")
    public void zOffset(int zOffset) {
        display(display().withZOffset(zOffset));
    }

    public int zIndex() {
        return display().zIndex() + zOffset();
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

    @Nullable
    public Style style() {
        if (styleId != null) {
            Theme theme = ThemeRegistry.current();
            return theme != null ? theme.style(styleId) : null;
        }
        return null;
    }

    protected void copyVisualProperties(AbstractWidget source) {
        this.name = source.name;
        this.styleId = source.styleId;
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
                source.behavior.repositionable(), source.behavior.scrollable());
    }

    public boolean draggable() {
        return behavior().draggable();
    }

    public void draggable(boolean draggable) {
        behavior(behavior().withDraggable(draggable));
    }

    public boolean scrollable() {
        return behavior().scrollable();
    }

    @SuppressWarnings("unused")
    public void scrollable(boolean scrollable) {
        behavior(behavior().withScrollable(scrollable));
    }

    public void behavior(WidgetBehavior behavior) {
        final WidgetBehavior previous = this.behavior();
        if (previous.equals(behavior)) return;
        this.behavior = behavior;
    }

    public WidgetBehavior behavior() {
        return behavior;
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
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (AbstractWidget) obj;
        return Objects.equals(this.activePredicate, that.activePredicate) &&
                Objects.equals(this.name, that.name) &&
                Objects.equals(this.styleId, that.styleId) &&
                this.tabIndex == that.tabIndex &&
                this.zIndex() == that.zIndex() &&
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
        return behavior().focusable() && visible();
    }

    @SuppressWarnings("unused")
    public void focusable(boolean focusable) {
        behavior(behavior().withFocusable(focusable));
    }

    public void focused(boolean focused) {
        behavior(behavior().withFocused(focused));
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

    public boolean hoverable() {
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
        if (!behavior().hoverable() || !behavior().active())
            return;
        behavior(behavior().withHovered(hovered));
    }

    protected void init() {
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
        pGuiGraphics.drawBox(rect, debugColor, zIndex() + 1);
    }

    public Optional<AbstractContainer> parent() {
        return Optional.ofNullable(parent);
    }

    public void parent(AbstractContainer parent) {
        this.parent = parent;
        if (parent != null) {
            zIndex(parent.zIndex() + DragManager.Z_STEP);
            if (this instanceof AbstractContainer container) {
                // We need to make sure all children get their zIndex updated now
                container.children().forEach(w -> w.zIndex(zIndex() + DragManager.Z_STEP));
            }
        } else
            zIndex(DragManager.Z_MIN);
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
        behavior(behavior().withPressed(pressed));
    }

    /**
     * @implNote For legacy use only, use AbstractWidget#render(ThemeGraphics, Point, float) instead
     */
    @Override
    public void render(@NotNull GuiGraphics graphics, int pMouseX, int pMouseY, float pPartialTick) {
        render(new ThemeGraphics(graphics, ThemeAtlas.getAtlas(), ThemeRegistry.current()), new Point(pMouseX, pMouseY), pPartialTick);
    }

    public void renderTooltip(@NotNull ThemeGraphics graphics, Point pMouse) {
        if (hovered() && tooltip() != null && !tooltip().isEmpty()) {
            graphics.renderTooltip(tooltip(), Point.of(pMouse.xInt(), pMouse.yInt()));
        }
    }

    @Override
    public void render(@NotNull ThemeGraphics graphics, Point pMouse, float pPartialTick) {
        if (!visible() || rect == null || rect.equals(Rect.ZERO)) return;
        Style s = style();
        if (s != null) s.render(graphics, rect, this);
        renderContent(graphics, pMouse, rect, pPartialTick);
        if (ClientConfig.debug()) {
            renderDebug(graphics);
        }
    }

    public void tooltip(List<Component> tooltip) {
        this.tooltip = tooltip;
    }

    public List<Component> tooltip() {
        return tooltip;
    }

    @SuppressWarnings("unused")
    public boolean repositionable() {
        return behavior().repositionable();
    }

    @SuppressWarnings("unused")
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

    @SuppressWarnings("unused")
    public void stretchRatio(float ratio) {
        this.stretchRatio = ratio;
    }

    public void style(String styleId) {
        this.styleId = styleId;
    }

    @Override
    public boolean isFocused() {
        return focused();
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
        boolean result = visiblePredicate == null || visiblePredicate.test(this);
        return result && this.display.visible() && (parent().isPresent() && parent().get().visible() || parent().isEmpty());
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
        private String name = "Unnamed_" + Math.round((Math.random() * hashCode()));
        private float stretchRatio = 1.0f;
        private String styleId = null;
        private int tabIndex = 0;
        private List<Component> tooltip;
        private EnumSet<Sizing> verticalSizing = EnumSet.of(Sizing.Fill);
        private boolean visible = true;
        private Predicate<AbstractWidget> visiblePredicate;

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

        public abstract AbstractContainer push();

        public boolean repositionable() {
            return behavior.repositionable();
        }

        public T repositionable(boolean repositionable) {
            behavior = behavior.withRepositionable(repositionable);
            return self();
        }

        public boolean scrollable() {
            return behavior.scrollable();
        }

        public T scrollable(boolean scrollable) {
            behavior = behavior.withScrollable(scrollable);
            return self();
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

        public T tooltip(List<Component> tooltip) {
            this.tooltip = tooltip;
            return self();
        }

        public List<Component> tooltip() {
            return this.tooltip;
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

        public T visiblePredicate(Predicate<AbstractWidget> predicate) {
            this.visiblePredicate = predicate;
            return self();
        }

        public Predicate<AbstractWidget> visiblePredicate() {
            return this.visiblePredicate;
        }

        public T zOffset(int z) {
            display = display.withZOffset(z);
            return self();
        }

        public int zOffset() {
            return display.zOffset();
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