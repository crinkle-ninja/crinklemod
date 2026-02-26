package ninja.crinkle.mod.client.gui.events.listeners;

import ninja.crinkle.mod.client.gui.events.*;

public interface EventListener {
    default void onEvent(Event event) {
        if (!event.propagate() && event.dispatched()) {
            return;
        }
        switch (event.type()) {
            case LayoutChanged -> {
                if (this instanceof LayoutListener listener)
                    listener.onLayoutChanged((LayoutChangedEvent) event);
            }
            case DoubleClick -> {
                if (this instanceof MouseListener listener) {
                    listener.onDoubleClick((DoubleClickEvent) event);
                }
            }
            case Click -> {
                if (this instanceof MouseListener listener)
                    listener.onClick((ClickEvent) event);
            }
            case MousePressed -> {
                if (this instanceof MouseListener listener) {
                    listener.onMousePressed((MousePressedEvent) event);
                }
            }
            case MouseReleased -> {
                if (this instanceof MouseListener listener)
                    listener.onMouseReleased((MouseReleasedEvent) event);
            }
            case Move -> {
                if (this instanceof MouseListener listener)
                    listener.onMove((MoveEvent) event);
            }
            case Key -> {
                if (this instanceof KeyListener listener)
                    listener.onKey((KeyEvent) event);
            }
            case CharTyped -> {
                if (this instanceof KeyListener listener)
                    listener.onCharTyped((CharTypedEvent) event);
            }
            case Scroll -> {
                if (this instanceof MouseListener listener)
                    listener.onScroll((ScrollEvent) event);
            }
            case DragStarted -> {
                if (this instanceof MouseListener listener) {
                    listener.onDragStarted((DragStartedEvent) event);
                }
            }
            case DragStopped -> {
                if (this instanceof MouseListener listener) {
                    listener.onDragStopped((DragStoppedEvent) event);
                }
            }
            case Dropped -> {
                if (this instanceof MouseListener listener) {
                    listener.onDropped((DroppedEvent) event);
                }
            }
            case Drag -> {
                if (this instanceof MouseListener listener) {
                    listener.onDrag((DragEvent) event);
                }
            }
            case Hover -> {
                if (this instanceof MouseListener listener)
                    listener.onHover((HoverEvent) event);
            }
            case Narrate -> {
                if (this instanceof NarrateListener listener)
                    listener.onNarrateEvent((NarrateEvent) event);
            }
            case TabOrder -> {
                if (this instanceof TabIndexListener listener)
                    listener.onTabIndexChanged((TabIndexEvent) event);
            }
            case FocusEntered -> {
                if (this instanceof FocusListener listener)
                    listener.onFocusEntered((FocusEnteredEvent) event);
            }
            case FocusLeft -> {
                if (this instanceof FocusListener listener)
                    listener.onFocusLeft((FocusLeftEvent) event);
            }
            case Focus -> {
                if (this instanceof FocusListener listener)
                    listener.onFocus((FocusEvent) event);
            }
            default -> throw new IllegalStateException("Unexpected event type: " + event.type());
        }
    }

    default int priority() {
        return 0;
    }

    String name();
}
