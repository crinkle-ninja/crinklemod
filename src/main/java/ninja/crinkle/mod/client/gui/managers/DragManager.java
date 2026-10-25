package ninja.crinkle.mod.client.gui.managers;

import com.mojang.logging.LogUtils;
import ninja.crinkle.mod.client.gui.properties.Point;
import ninja.crinkle.mod.client.gui.properties.Rect;
import ninja.crinkle.mod.client.gui.widgets.AbstractWidget;
import org.slf4j.Logger;

public class DragManager {
    public static final int Z_MIN = 0;
    public static final int Z_STEP = 1;
    public static final int Z_MAX = Z_STEP * 64;
    public static final int Z_MODAL = Z_MAX / 2;
    private static final Logger LOGGER = LogUtils.getLogger();
    private AbstractWidget current;
    private boolean dragging;
    private double dragAccumulatorX = 0;
    private double dragAccumulatorY = 0;

    public void dragging(boolean dragging) {
        this.dragging = dragging;
    }

    public AbstractWidget current() {
        return current;
    }

    public void drop() {
        if (current() == null) return;
        LOGGER.trace("dropped {}", current().name());
        dragging(false);
        current().dragged(false);
        current().pressed(false);
    }

    public void dragged(Point dragPoint) {
        if (current() == null) return;
        LOGGER.trace("dragging {} to {}", current().name(), dragPoint);
        dragAccumulatorX += dragPoint.x();
        dragAccumulatorY += dragPoint.y();
        int dx = (int) dragAccumulatorX;
        int dy = (int) dragAccumulatorY;
        if (dx == 0 && dy == 0) return;
        dragAccumulatorX -= dx;
        dragAccumulatorY -= dy;
        Rect rect = current().rect();
        current().setRect(new Rect(rect.x() + dx, rect.y() + dy, rect.width(), rect.height()));
    }

    public void current(AbstractWidget current) {
        this.current = current;
    }

    public boolean dragging() {
        return dragging;
    }

    public void startDragging(AbstractWidget widget) {
        if (widget == null || !widget.draggable()) return;
        LOGGER.trace("start dragging: {}", widget.name());
        dragging(true);
        current(widget);
        dragAccumulatorX = 0;
        dragAccumulatorY = 0;
        widget.dragged(true);
        widget.pressable(true);
    }
}