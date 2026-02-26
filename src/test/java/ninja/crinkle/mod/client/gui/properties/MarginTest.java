package ninja.crinkle.mod.client.gui.properties;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Margin Properties")
class MarginTest {

    @Nested
    @DisplayName("Constructor")
    class ConstructorTest {
        @Test
        @DisplayName("should create a margin with individual values for each side")
        void constructor_withIndividualValues() {
            Margin margin = new Margin(5, 10, 15, 20);

            assertEquals(5, margin.top());
            assertEquals(10, margin.right());
            assertEquals(15, margin.bottom());
            assertEquals(20, margin.left());
        }

        @Test
        @DisplayName("should create a margin with a single value for all sides")
        void constructor_withSingleValue() {
            Margin margin = new Margin(10);

            assertEquals(10, margin.top());
            assertEquals(10, margin.right());
            assertEquals(10, margin.bottom());
            assertEquals(10, margin.left());
        }

        @Test
        @DisplayName("should throw an exception if any value is negative")
        void constructor_throwsForNegativeValues() {
            String expectedMessage = "Margin values cannot be negative";
            assertEquals(expectedMessage, assertThrows(IllegalArgumentException.class, () -> new Margin(-1, 10, 15, 20)).getMessage());
            assertEquals(expectedMessage, assertThrows(IllegalArgumentException.class, () -> new Margin(5, -1, 15, 20)).getMessage());
            assertEquals(expectedMessage, assertThrows(IllegalArgumentException.class, () -> new Margin(5, 10, -1, 20)).getMessage());
            assertEquals(expectedMessage, assertThrows(IllegalArgumentException.class, () -> new Margin(5, 10, 15, -1)).getMessage());
        }
    }

    @Nested
    @DisplayName("Static Factories and Constants")
    class FactoriesAndConstantsTest {
        @Test
        @DisplayName("all() should create a margin with equal positive values")
        void all_withPositiveValue() {
            Margin margin = Margin.all(10);
            assertEquals(10, margin.top());
            assertEquals(10, margin.right());
            assertEquals(10, margin.bottom());
            assertEquals(10, margin.left());
        }

        @Test
        @DisplayName("all() should create a zero margin when given zero")
        void all_withZero() {
            Margin margin = Margin.all(0);
            assertEquals(0, margin.top());
            assertEquals(0, margin.right());
            assertEquals(0, margin.bottom());
            assertEquals(0, margin.left());
        }

        @Test
        @DisplayName("all() should throw an exception for a negative value")
        void all_throwsForNegativeValue() {
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> Margin.all(-5));
            assertEquals("Margin values cannot be negative", exception.getMessage());
        }

        @Test
        @DisplayName("ZERO constant should have all zero values")
        void zero_isZero() {
            Margin zeroMargin = Margin.ZERO;
            assertEquals(0, zeroMargin.top());
            assertEquals(0, zeroMargin.right());
            assertEquals(0, zeroMargin.bottom());
            assertEquals(0, zeroMargin.left());
        }
    }
}