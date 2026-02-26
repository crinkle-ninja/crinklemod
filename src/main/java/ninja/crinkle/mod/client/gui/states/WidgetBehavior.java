package ninja.crinkle.mod.client.gui.states;

import org.jetbrains.annotations.NotNull;

public record WidgetBehavior(boolean draggable, boolean dragged, boolean pressed, boolean focused, boolean hovered,
                             boolean active, boolean focusable, boolean hoverable, boolean pressable) {
    public WidgetBehavior {
        if (dragged && !draggable) {
            throw new IllegalArgumentException("Cannot be dragged if not draggable");
        }
    }

    public WidgetBehavior() {
        this(false, false, false, false, false,
                false, false, false, false);
    }

    public WidgetBehavior withDraggable(boolean draggable) {
        return new WidgetBehavior(draggable, dragged, pressed, focused, hovered, active, focusable, hoverable, pressable);
    }

    public WidgetBehavior withDragged(boolean dragged) {
        return new WidgetBehavior(draggable, dragged, pressed, focused, hovered, active, focusable, hoverable, pressable);
    }

    public WidgetBehavior withPressed(boolean pressed) {
        return new WidgetBehavior(draggable, dragged, pressed, focused, hovered, active, focusable, hoverable, pressable);
    }

    public WidgetBehavior withFocused(boolean focused) {
        return new WidgetBehavior(draggable, dragged, pressed, focused, hovered, active, focusable, hoverable, pressable);
    }

    public WidgetBehavior withHovered(boolean hovered) {
        return new WidgetBehavior(draggable, dragged, pressed, focused, hovered, active, focusable, hoverable, pressable);
    }

    public WidgetBehavior withActive(boolean active) {
        return new WidgetBehavior(draggable, dragged, pressed, focused, hovered, active, focusable, hoverable, pressable);
    }

    public WidgetBehavior withFocusable(boolean focusable) {
        return new WidgetBehavior(draggable, dragged, pressed, focused, hovered, active, focusable, hoverable, pressable);
    }

    public WidgetBehavior withHoverable(boolean highlightable) {
        return new WidgetBehavior(draggable, dragged, pressed, focused, hovered, active, focusable, highlightable, pressable);
    }

    public WidgetBehavior withPressable(boolean pressable) {
        return new WidgetBehavior(draggable, dragged, pressed, focused, hovered, active, focusable, hoverable, pressable);
    }

    @Override
    public @NotNull String toString() {
        return "WidgetBehavior{" +
                "draggable=" + draggable +
                ", dragged=" + dragged +
                ", pressed=" + pressed +
                ", focused=" + focused +
                ", hovered=" + hovered +
                ", active=" + active +
                ", focusable=" + focusable +
                ", highlightable=" + hoverable +
                ", pressable=" + pressable +
                '}';
    }
}
