package ninja.crinkle.mod.client.gui.managers;

import com.mojang.logging.LogUtils;
import ninja.crinkle.mod.client.gui.properties.Point;
import ninja.crinkle.mod.client.gui.properties.Rect;
import ninja.crinkle.mod.client.gui.widgets.AbstractWidget;
import org.slf4j.Logger;

public class DragManager {
    public static final int Z_MAX = Integer.MAX_VALUE - 1;
    public static final int Z_MIN = 0;
    public static final int Z_STEP = 10;
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
        dragging(false);
        current().dragged(false);
        current().pressed(false);
    }

    public void dragged(Point dragPoint) {
        if (current() == null) return;
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
        if (!widget.draggable()) return;
        dragging(true);
        current(widget);
        dragAccumulatorX = 0;
        dragAccumulatorY = 0;
        widget.dragged(true);
        widget.pressable(true);
    }
}