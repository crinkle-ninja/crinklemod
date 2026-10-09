package ninja.crinkle.mod.client.gui.states;

import ninja.crinkle.mod.client.gui.managers.DragManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("WidgetDisplay Tests")
class WidgetDisplayTest {

    @Nested
    @DisplayName("Constructor validation tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should throw IllegalArgumentException when alpha is greater than 1")
        void testConstructorAlphaGreaterThanOne_Exception() {
            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> new WidgetDisplay(true, 1.2f, 5),
                    "Constructor should throw an exception when alpha is greater than 1.");
            assertEquals("Alpha must be between 0 and 1", exception.getMessage(), "Unexpected exception message for " +
                    "invalid alpha.");
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when alpha is less than 0")
        void testConstructorAlphaLessThanZero_Exception() {
            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> new WidgetDisplay(true, -0.1f, 5),
                    "Constructor should throw an exception when alpha is less than 0.");
            assertEquals("Alpha must be between 0 and 1", exception.getMessage(), "Unexpected exception message for " +
                    "invalid alpha.");
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when Z-index is negative")
        void testConstructorZIndexNegative_Exception() {
            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> new WidgetDisplay(true, 0.5f, -1),
                    "Constructor should throw an exception when Z-index is negative.");
            assertEquals("Z-Index must be greater than or equal to 0", exception.getMessage(), "Unexpected exception " +
                    "message for invalid Z-index.");
        }
    }

    @Nested
    @DisplayName("Default constructor tests")
    class DefaultConstructorTests {

        @Test
        @DisplayName("Should initialize WidgetDisplay with default values")
        void testDefaultConstructor_Success() {
            // When
            WidgetDisplay widget = new WidgetDisplay();

            // Then
            assertTrue(widget.visible(), "Default visibility should be true.");
            assertEquals(1.0f, widget.alpha(), "Default alpha should be 1.0.");
            assertEquals(DragManager.Z_MIN, widget.zIndex(), "Default Z-index should be DragManager.Z_MIN.");
        }
    }

    @Nested
    @DisplayName("withVisible() tests")
    class WithVisibleTests {

        @Test
        @DisplayName("Should return a new instance with visibility set to false")
        void testWithVisibleFalse_Success() {
            // Given
            WidgetDisplay widget = new WidgetDisplay(true, 0.8f, 15);

            // When
            WidgetDisplay updatedWidget = widget.withVisible(false);

            // Then
            assertNotSame(widget, updatedWidget, "The method should create a new instance.");
            assertFalse(updatedWidget.visible(), "Visibility should be updated to false.");
            assertEquals(0.8f, updatedWidget.alpha(), "Alpha should remain unchanged.");
            assertEquals(15, updatedWidget.zIndex(), "Z-index should remain unchanged.");
        }

        @Test
        @DisplayName("Should return a new instance with visibility set to true")
        void testWithVisibleTrue_Success() {
            // Given
            WidgetDisplay widget = new WidgetDisplay(false, 0.5f, 10);

            // When
            WidgetDisplay updatedWidget = widget.withVisible(true);

            // Then
            assertNotSame(widget, updatedWidget, "The method should create a new instance.");
            assertTrue(updatedWidget.visible(), "Visibility should be updated to true.");
            assertEquals(0.5f, updatedWidget.alpha(), "Alpha should remain unchanged.");
            assertEquals(10, updatedWidget.zIndex(), "Z-index should remain unchanged.");
        }
    }
}