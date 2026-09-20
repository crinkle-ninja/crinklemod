package ninja.crinkle.mod.client.gui.widgets;

import ninja.crinkle.mod.client.color.Color;
import ninja.crinkle.mod.client.gui.events.ScrollEvent;
import ninja.crinkle.mod.client.gui.managers.GuiManager;
import ninja.crinkle.mod.client.gui.properties.Point;
import ninja.crinkle.mod.client.gui.properties.Rect;
import ninja.crinkle.mod.client.gui.renderers.ThemeGraphics;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * A container that clips its children to a viewport and supports vertical scrolling.
 * <p>
 * Children are arranged as if the container had unlimited height, then offset
 * vertically by the scroll amount. Rendering is clipped to this container's rect
 * using GL scissor.
 */
public class ScrollContainer extends AbstractContainer {
    private static final int SCROLL_SPEED = 10;
    private static final int SCROLLBAR_WIDTH = 4;
    private static final Color SCROLLBAR_TRACK = Color.of(0, 0, 0, 0.3f);
    private static final Color SCROLLBAR_THUMB = Color.of(255, 255, 255, 0.6f);

    private int scrollOffset;

    protected ScrollContainer(@NotNull Builder builder) {
        super(builder);
        this.scrollOffset = 0;
    }

    @Override
    protected void registerEventHandlers() {
        super.registerEventHandlers();
        eventManager().addListener(ScrollEvent.KEY, priority(), this::onScroll);
    }

    private void onScroll(ScrollEvent event) {
        if (event.cancelled() || event.consumed()) return;
        if (!visible() || !active()) return;
        if (!mouseOver(event.position())) return;

        int contentHeight = contentHeight();
        int viewportHeight = rect().height();
        if (contentHeight <= viewportHeight) return;

        int maxOffset = contentHeight - viewportHeight;
        int oldOffset = scrollOffset;
        scrollOffset = Math.max(0, Math.min(maxOffset, scrollOffset - (int) (event.delta() * SCROLL_SPEED)));

        if (scrollOffset != oldOffset) {
            arrange();
            event.consumed(true);
        }
    }

    @Override
    public void arrange() {
        List<AbstractWidget> kids = children();
        if (kids.isEmpty() || rect().equals(Rect.ZERO)) return;

        // Give each child the full viewport width but its own natural height,
        // positioned as a vertical stack offset by -scrollOffset.
        int offset = rect().y() - scrollOffset;
        for (AbstractWidget child : kids) {
            int childH = child.minimumHeight();
            Rect allocated = new Rect(rect().x(), offset, rect().width(), childH);
            Rect fitted = fitChildInRect(child, allocated);
            child.setRect(fitted);
            offset += childH + separation();
        }
    }

    @Override
    public void renderContent(ThemeGraphics graphics, Point pMouse, Rect renderRect, float pPartialTick) {
        graphics.enableScissor(rect().x(), rect().y(), rect().right(), rect().bottom());
        renderChildren(graphics, pMouse, pPartialTick);
        graphics.disableScissor();
        renderScrollbar(graphics);
    }

    private void renderScrollbar(ThemeGraphics graphics) {
        int contentH = contentHeight();
        int trackH = rect().height();
        if (contentH <= trackH) return;

        int trackX = rect().right() - SCROLLBAR_WIDTH;
        int trackY = rect().y();

        graphics.fill(trackX, trackY, trackX + SCROLLBAR_WIDTH, trackY + trackH,
                SCROLLBAR_TRACK, zIndex() + 1);

        float ratio = (float) trackH / contentH;
        int thumbH = Math.max(10, (int) (trackH * ratio));
        float scrollRatio = (float) scrollOffset / (contentH - trackH);
        int thumbY = trackY + (int) ((trackH - thumbH) * scrollRatio);

        graphics.fill(trackX, thumbY, trackX + SCROLLBAR_WIDTH, thumbY + thumbH,
                SCROLLBAR_THUMB, zIndex() + 2);
    }

    @Override
    public void setRect(Rect rect) {
        super.setRect(rect);
        clampScroll();
    }

    /**
     * Total height of all children plus separations.
     */
    private int contentHeight() {
        List<AbstractWidget> kids = children();
        if (kids.isEmpty()) return 0;
        int sum = kids.stream().mapToInt(AbstractWidget::minimumHeight).sum();
        int gaps = separation() * Math.max(0, kids.size() - 1);
        return sum + gaps;
    }

    private void clampScroll() {
        int maxOffset = Math.max(0, contentHeight() - rect().height());
        scrollOffset = Math.max(0, Math.min(maxOffset, scrollOffset));
    }

    @Override
    public AbstractWidget visualCopy(AbstractContainer newParent) {
        ScrollContainer copy = new ScrollContainer.Builder(newParent)
                .separation(separation()).build();
        copy.copyVisualProperties(this);
        visualCopyChildrenInto(copy);
        return copy;
    }

    @Override
    public String toString() {
        return "ScrollContainer{scrollOffset=" + scrollOffset + ", " + super.toString() + '}';
    }

    public static class Builder extends AbstractContainerBuilder<Builder> {

        public Builder(AbstractContainer container) {
            super(container);
            active(true);
        }

        public Builder(GuiManager manager) {
            super(manager);
            active(true);
        }

        @Override
        public ScrollContainer build() {
            return new ScrollContainer(this);
        }

        @Override
        public AbstractContainer push() {
            return parent().add(this);
        }

        @Override
        protected Builder self() {
            return this;
        }

        @Override
        public ScrollContainer pushAndReturn() {
            ScrollContainer container = new ScrollContainer(this);
            parent().add(container);
            return container;
        }
    }
}