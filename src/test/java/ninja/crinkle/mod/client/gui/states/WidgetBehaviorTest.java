package ninja.crinkle.mod.client.gui.states;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("WidgetBehavior Test")
public class WidgetBehaviorTest {

    @Nested
    @DisplayName("WithDraggable Tests")
    class WithDraggableTests {

        @Test
        @DisplayName("should create a new WidgetBehavior with draggable set to true")
        void shouldCreateWidgetBehaviorWithDraggableTrue() {
            WidgetBehavior original = new WidgetBehavior();
            WidgetBehavior updated = original.withDraggable(true);

            assertAll(
                    () -> assertNotNull(updated, "Updated WidgetBehavior should not be null"),
                    () -> assertTrue(updated.draggable(), "Draggable should be true"),
                    () -> assertFalse(updated.dragged(), "Dragged should remain false"),
                    () -> assertEquals(original.pressed(), updated.pressed(), "Pressed state should remain unchanged"),
                    () -> assertEquals(original.focused(), updated.focused(), "Focused state should remain unchanged"),
                    () -> assertEquals(original.hovered(), updated.hovered(), "Hovered state should remain unchanged"),
                    () -> assertEquals(original.active(), updated.active(), "Active state should remain unchanged"),
                    () -> assertEquals(original.focusable(), updated.focusable(), "Focusable state should remain unchanged"),
                    () -> assertEquals(original.hoverable(), updated.hoverable(), "Hoverable state should remain unchanged"),
                    () -> assertEquals(original.pressable(), updated.pressable(), "Pressable state should remain unchanged")
            );
        }

        @Test
        @DisplayName("should create a new WidgetBehavior with draggable set to false")
        void shouldCreateWidgetBehaviorWithDraggableFalse() {
            WidgetBehavior original = new WidgetBehavior(true, false, false, false, false, false, false, false, false);
            WidgetBehavior updated = original.withDraggable(false);

            assertAll(
                    () -> assertNotNull(updated, "Updated WidgetBehavior should not be null"),
                    () -> assertFalse(updated.draggable(), "Draggable should be false"),
                    () -> assertEquals(original.dragged(), updated.dragged(), "Dragged state should remain unchanged"),
                    () -> assertEquals(original.pressed(), updated.pressed(), "Pressed state should remain unchanged"),
                    () -> assertEquals(original.focused(), updated.focused(), "Focused state should remain unchanged"),
                    () -> assertEquals(original.hovered(), updated.hovered(), "Hovered state should remain unchanged"),
                    () -> assertEquals(original.active(), updated.active(), "Active state should remain unchanged"),
                    () -> assertEquals(original.focusable(), updated.focusable(), "Focusable state should remain unchanged"),
                    () -> assertEquals(original.hoverable(), updated.hoverable(), "Hoverable state should remain unchanged"),
                    () -> assertEquals(original.pressable(), updated.pressable(), "Pressable state should remain unchanged")
            );
        }

        @Test
        @DisplayName("should throw IllegalArgumentException if trying to set dragged to true with draggable false")
        void shouldThrowExceptionForInvalidDraggedState() {
            WidgetBehavior original = new WidgetBehavior(false, false, false, false, false, false, false, false, false);

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> original.withDragged(true),
                    "Expected IllegalArgumentException not thrown when drag is true but not draggable"
            );

            assertEquals("Cannot be dragged if not draggable", exception.getMessage(), "Exception message mismatch");
        }
    }

    @Nested
    @DisplayName("WithDragged Tests")
    class WithDraggedTests {

        @Test
        @DisplayName("should create a new WidgetBehavior with dragged set to true")
        void shouldCreateWidgetBehaviorWithDraggedTrue() {
            WidgetBehavior original = new WidgetBehavior(true, false, false, false, false, false, false, false, false);
            WidgetBehavior updated = original.withDragged(true);

            assertAll(
                    () -> assertNotNull(updated, "Updated WidgetBehavior should not be null"),
                    () -> assertTrue(updated.dragged(), "Dragged should be true"),
                    () -> assertEquals(original.draggable(), updated.draggable(), "Draggable state should remain unchanged"),
                    () -> assertEquals(original.pressed(), updated.pressed(), "Pressed state should remain unchanged"),
                    () -> assertEquals(original.focused(), updated.focused(), "Focused state should remain unchanged"),
                    () -> assertEquals(original.hovered(), updated.hovered(), "Hovered state should remain unchanged"),
                    () -> assertEquals(original.active(), updated.active(), "Active state should remain unchanged"),
                    () -> assertEquals(original.focusable(), updated.focusable(), "Focusable state should remain unchanged"),
                    () -> assertEquals(original.hoverable(), updated.hoverable(), "Hoverable state should remain unchanged"),
                    () -> assertEquals(original.pressable(), updated.pressable(), "Pressable state should remain unchanged")
            );
        }

        @Test
        @DisplayName("should throw IllegalArgumentException when dragged is true but draggable is false")
        void shouldThrowExceptionWhenDraggedTrueAndDraggableFalse() {
            WidgetBehavior original = new WidgetBehavior(false, false, false, false, false, false, false, false, false);

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> original.withDragged(true),
                    "Expected IllegalArgumentException not thrown when dragged is true but not draggable"
            );

            assertEquals("Cannot be dragged if not draggable", exception.getMessage(), "Exception message mismatch");
        }
    }

    @Nested
    @DisplayName("WithPressed Tests")
    class WithPressedTests {

        @Test
        @DisplayName("should create a new WidgetBehavior with pressed set to true")
        void shouldCreateWidgetBehaviorWithPressedTrue() {
            WidgetBehavior original = new WidgetBehavior();
            WidgetBehavior updated = original.withPressed(true);

            assertAll(
                    () -> assertNotNull(updated, "Updated WidgetBehavior should not be null"),
                    () -> assertTrue(updated.pressed(), "Pressed should be true"),
                    () -> assertEquals(original.draggable(), updated.draggable(), "Draggable state should remain unchanged"),
                    () -> assertEquals(original.dragged(), updated.dragged(), "Dragged state should remain unchanged"),
                    () -> assertEquals(original.focused(), updated.focused(), "Focused state should remain unchanged"),
                    () -> assertEquals(original.hovered(), updated.hovered(), "Hovered state should remain unchanged"),
                    () -> assertEquals(original.active(), updated.active(), "Active state should remain unchanged"),
                    () -> assertEquals(original.focusable(), updated.focusable(), "Focusable state should remain unchanged"),
                    () -> assertEquals(original.hoverable(), updated.hoverable(), "Hoverable state should remain unchanged")
            );
        }
    }

    @Nested
    @DisplayName("WithFocused Tests")
    class WithFocusedTests {

        @Test
        @DisplayName("should create a new WidgetBehavior with focused set to true")
        void shouldCreateWidgetBehaviorWithFocusedTrue() {
            WidgetBehavior original = new WidgetBehavior();
            WidgetBehavior updated = original.withFocused(true);

            assertAll(
                    () -> assertNotNull(updated, "Updated WidgetBehavior should not be null"),
                    () -> assertTrue(updated.focused(), "Focused should be true"),
                    () -> assertEquals(original.draggable(), updated.draggable(), "Draggable state should remain unchanged"),
                    () -> assertEquals(original.dragged(), updated.dragged(), "Dragged state should remain unchanged"),
                    () -> assertEquals(original.pressed(), updated.pressed(), "Pressed state should remain unchanged"),
                    () -> assertEquals(original.hovered(), updated.hovered(), "Hovered state should remain unchanged"),
                    () -> assertEquals(original.active(), updated.active(), "Active state should remain unchanged"),
                    () -> assertEquals(original.focusable(), updated.focusable(), "Focusable state should remain unchanged"),
                    () -> assertEquals(original.hoverable(), updated.hoverable(), "Hoverable state should remain unchanged")
            );
        }
    }

    @Nested
    @DisplayName("WithHovered Tests")
    class WithHoveredTests {

        @Test
        @DisplayName("should create a new WidgetBehavior with hovered set to true")
        void shouldCreateWidgetBehaviorWithHoveredTrue() {
            WidgetBehavior original = new WidgetBehavior();
            WidgetBehavior updated = original.withHovered(true);

            assertAll(
                    () -> assertNotNull(updated, "Updated WidgetBehavior should not be null"),
                    () -> assertTrue(updated.hovered(), "Hovered should be true"),
                    () -> assertEquals(original.draggable(), updated.draggable(), "Draggable state should remain unchanged"),
                    () -> assertEquals(original.dragged(), updated.dragged(), "Dragged state should remain unchanged"),
                    () -> assertEquals(original.pressed(), updated.pressed(), "Pressed state should remain unchanged"),
                    () -> assertEquals(original.focused(), updated.focused(), "Focused state should remain unchanged"),
                    () -> assertEquals(original.active(), updated.active(), "Active state should remain unchanged"),
                    () -> assertEquals(original.focusable(), updated.focusable(), "Focusable state should remain unchanged"),
                    () -> assertEquals(original.hoverable(), updated.hoverable(), "Hoverable state should remain unchanged")
            );
        }
    }

    @Test
    @DisplayName("should return correct toString representation of WidgetBehavior")
    void shouldReturnCorrectToStringRepresentation() {
        WidgetBehavior behavior = new WidgetBehavior(true, true, false, false, true, false, true, false, true);

        String expected = "WidgetBehavior{draggable=true, dragged=true, pressed=false, focused=false, hovered=true, active=false, focusable=true, highlightable=false, pressable=true}";
        assertEquals(expected, behavior.toString(), "toString output mismatch");
    }
}