package ninja.crinkle.mod.client.gui.mixins;

import com.mojang.logging.LogUtils;
import ninja.crinkle.mod.client.gui.actors.*;
import ninja.crinkle.mod.client.gui.managers.GuiManager;
import ninja.crinkle.mod.client.gui.managers.IManagedGUI;
import ninja.crinkle.mod.client.gui.properties.ClickState;
import ninja.crinkle.mod.client.gui.properties.Point;
import ninja.crinkle.mod.client.gui.widgets.AbstractContainer;
import ninja.crinkle.mod.client.gui.widgets.Widget;
import org.slf4j.Logger;

import java.util.Comparator;
import java.util.List;

public interface ManagedScreen extends GuiManager, IManagedGUI {
    Logger LOGGER = LogUtils.getLogger();
    int DOUBLE_CLICK_OFFSET = 2;
    int DOUBLE_CLICK_TIME = 300;

    Widget hovered();
    void hovered(Widget hovered);
    String name();

    default boolean onKeyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        if (notReady()) {
            return false;
        }
        Widget widget = focusedWidget();
        if (widget != null)
            LOGGER.trace("onKeyPressed on {}", widget.name());
        if (widget instanceof KeyActor action) {
            return action.onKeyPressed(pKeyCode, pScanCode, pModifiers);
        }
        return false;
    }

    boolean notReady();

    List<Widget> widgetCache();

    Point mouse();

    default List<Widget> widgets() {
        if (notReady()) return List.of();
        if (!widgetCache().isEmpty()) return List.copyOf(widgetCache());
        LOGGER.trace("Building widget cache...");
        widgetCache().clear();
        widgetCache().addAll(widgets(root()));
        LOGGER.trace("Widgets cached: {}", widgetCache().size());
        return List.copyOf(widgetCache());
    }


    default List<Widget> widgets(AbstractContainer parent) {
        List<Widget> widgets = new java.util.ArrayList<>();
        widgets.add(parent);
        for(Widget widget : parent.children().stream().toList()) {
            if (widget instanceof AbstractContainer container) {
                widgets.addAll(widgets(container));
            } else {
                widgets.add(widget);
            }
        }
        return widgets;
    }

    default Widget hoveredWidget(Point point) {
        return widgets().stream()
                .filter(Widget::visible)
                .filter(Widget::hoverable)
                .filter(w -> w.rect().contains(point))
                .max(Comparator.comparingInt(Widget::zIndex))
                .orElse(null);
    }

    default Widget clickedWidget(Point point) {
        return widgets().stream()
                .filter(Widget::active)
                .filter(Widget::visible)
                .filter(Widget::pressable)
                .filter(w -> w.rect().contains(point))
                .max(Comparator.comparingInt(Widget::zIndex))
                .orElse(null);
    }

    default Widget focusableWidget(Point point) {
        return widgets().stream()
                .filter(Widget::active)
                .filter(Widget::visible)
                .filter(Widget::focusable)
                .filter(w -> w.rect().contains(point))
                .max(Comparator.comparingInt(Widget::zIndex))
                .orElse(null);
    }

    default Widget focusedWidget() {
        return widgets().stream()
                .filter(Widget::active)
                .filter(Widget::visible)
                .filter(Widget::focused)
                .max(Comparator.comparingInt(Widget::zIndex))
                .orElse(null);
    }

    default Widget scrollableWidget(Point point) {
        return widgets().stream()
                .filter(Widget::active)
                .filter(Widget::visible)
                .filter(Widget::scrollable)
                .filter(w -> w.rect().contains(point))
                .max(Comparator.comparingInt(Widget::zIndex))
                .orElse(null);
    }

    default Widget draggableWidget(Point point) {
        return widgets().stream()
                .filter(Widget::active)
                .filter(Widget::visible)
                .filter(Widget::draggable)
                .filter(w -> w.rect().contains(point))
                .min(Comparator.comparingInt(Widget::zIndex))
                .orElse(null);
    }

    // mouse clicked handlers
    default boolean handlePressed(Widget widget, Point point, int pButton) {
        if (widget == null || !widget.pressable()) return false;
        widget.pressed(true);
        if (widget instanceof MouseActor action) {
            return action.onPressed(point, MouseButton.fromInt(pButton));
        }
        return false;
    }

    default boolean handleDoubleClick(Widget widget, Point point, int pButton) {
        if (widget instanceof MouseActor action) {
            return action.onDoubleClick(point, MouseButton.fromInt(pButton));
        }
        return false;
    }

    default void handleFocusLeft(Widget focusable) {
        if (focusable instanceof FocusActor action) {
            action.onBlur();
        }
    }

    default void handleFocusEntered(Widget focusable) {
        if (focusable instanceof FocusActor action) {
            action.onFocus();
        }
    }

    default boolean onMouseClicked(double pMouseX, double pMouseY, int pButton) {
        if (notReady()) {
            return false;
        }
        ClickState state = clickState();
        Point point = new Point(pMouseX, pMouseY);
        Widget topMost = clickedWidget(point);
        if (topMost != null)
            LOGGER.trace("mouseClicked on {}, zIndex: {}, {}", topMost.name(), topMost.zIndex(), topMost);
        // double click
        if (topMost != null && state.button() == pButton && System.currentTimeMillis() - state.clickTime() < DOUBLE_CLICK_TIME
                && state.position().distance(pMouseX, pMouseY) < DOUBLE_CLICK_OFFSET) {
            return handleDoubleClick(topMost, point, pButton);
        }

        clickState(new ClickState(point, pButton, System.currentTimeMillis(), topMost));

        // drag
        Widget dragTarget = draggableWidget(point);
        if (dragTarget != null) {
            dragManager().current(dragTarget);
            dragManager().dragging(false);
        }

        // focus
        Widget topFocusable = focusableWidget(point);
        if ((topFocusable == null || topFocusable != focusManager().currentFocus()) && focusManager().currentFocus() != null) {
            handleFocusLeft(focusManager().currentFocus());
        }
        if (topFocusable != null && topFocusable.focusable()) {
            handleFocusEntered(topFocusable);
        }
        focusManager().currentFocus(topFocusable);

        // pressed
        return handlePressed(topFocusable, point, pButton);
    }

    void clickState(ClickState clickState);

    ClickState clickState();

    // mouse released handlers
    default void handleDragStopped(Widget widget, Point point) {
        if (widget instanceof DragActor action) {
            action.onDragStopped(widget, point);
        }
    }

    default boolean handleClicked(Widget widget, Point point, int pButton) {
        if (widget instanceof MouseActor action) {
            return action.onClick(point, MouseButton.fromInt(pButton));
        }
        return false;
    }

    default boolean handleReleased(Widget widget, Point point, int pButton) {
        if (widget == null || !widget.pressed()) return false;
        widget.pressed(false);
        if (widget instanceof MouseActor action) {
            return action.onReleased(point, MouseButton.fromInt(pButton));
        }
        return false;
    }

    default boolean onMouseReleased(double pMouseX, double pMouseY, int pButton) {
        if (notReady()) {
            return false;
        }
        Point point = new Point(pMouseX, pMouseY);
        // drag
        if (dragManager().current() != null) {
            if (dragManager().dragging()) {
                Widget dropped = dragManager().current();
                dragManager().drop();
                handleDragStopped(dropped, point);
            }
            dragManager().current(null);
        }
        Widget topMost = clickedWidget(point);
        if (topMost != null)
            LOGGER.trace("mouseReleased on {}, zIndex: {}", topMost.name(), topMost.zIndex());

        // released & clicked
        boolean result = false;
        ClickState state = clickState();
        if (topMost != null && state.button() == pButton && topMost == state.widget())
            result = handleClicked(topMost, point, pButton);
        return (state.widget() != null && handleReleased(state.widget(), point, pButton)) || result;
    }

    // mouse dragged handlers
    default boolean handleDragStarted(Widget widget, Point mousePoint, Point dragPoint, int button) {
        if (widget instanceof DragActor action) {
            return action.onDragStarted(widget, mousePoint, dragPoint, button);
        }
        return false;
    }

    default boolean handleDrag(Widget widget, Point mousePoint, Point dragPoint, int button) {
        if (widget instanceof DragActor action) {
            return action.onDrag(widget, mousePoint, dragPoint, button);
        }
        return false;
    }

    default boolean onMouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY) {
        if (notReady()) {
            return false;
        }
        Point mousePoint = new Point(pMouseX, pMouseY);
        Point dragPoint = new Point(pDragX, pDragY);
        Widget topMost = draggableWidget(mousePoint);
        if (dragManager().current() != null && !dragManager().dragging()) {
            dragManager().startDragging(topMost);
            return handleDragStarted(topMost, mousePoint, dragPoint, pButton);
        } else {
            dragManager().dragged(dragPoint);
        }
        return handleDrag(topMost, mousePoint, dragPoint, pButton);
    }

    default boolean onMouseScrolled(double pMouseX, double pMouseY, double pDelta) {
        if (notReady()) {
            return false;
        }
        Point mousePoint = new Point(pMouseX, pMouseY);
        Widget topMost = scrollableWidget(mousePoint);
        if (topMost instanceof MouseActor action) {
            return action.onScroll(mousePoint, pDelta);
        }
        return false;
    }

    default boolean onKeyReleased(int pKeyCode, int pScanCode, int pModifiers) {
        if (notReady()) {
            return false;
        }
        Widget focused = focusedWidget();
        if (focused instanceof KeyActor action) {
            return action.onKeyReleased(pKeyCode, pScanCode, pModifiers);
        }
        return false;
    }

    default boolean onCharTyped(char pCodePoint, int pModifiers) {
        if (notReady()) {
            return false;
        }
        Widget focused = focusedWidget();
        if (focused instanceof KeyActor action) {
            return action.onCharTyped(pCodePoint, pModifiers);
        }
        return false;
    }

    default boolean onMouseMoved(double pMouseX, double pMouseY) {
        if (notReady()) {
            return false;
        }
        if (mouse().xInt() != Math.floor(pMouseX) || mouse().yInt() != Math.floor(pMouseY)) {
            Point mousePoint = new Point(pMouseX, pMouseY);
            Widget hoveredWidget = hoveredWidget(mousePoint);
            if ((hoveredWidget == null || hoveredWidget != hovered()) && hovered() != null) {
                hovered().hovered(false);
                hovered(null);
            }
            if (hoveredWidget != null) {
                hovered(hoveredWidget);
                hoveredWidget.hovered(true);
                LOGGER.trace("mouseMoved, hovering over {}", hoveredWidget.name());
            }
            if (focusedWidget() instanceof MouseActor action) {
                action.onMoved(mousePoint);
            }
            mouse(new Point(pMouseX, pMouseY));
            return true;
        }
        return false;
    }

    void mouse(Point point);
}
