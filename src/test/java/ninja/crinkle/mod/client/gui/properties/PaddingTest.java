package ninja.crinkle.mod.client.gui.properties;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Padding Properties")
class PaddingTest {

    @Nested
    @DisplayName("Constructor")
    class ConstructorTest {
        @Test
        @DisplayName("should create padding with a single value for all sides")
        void constructor_withSingleValue() {
            Padding padding = new Padding(8);
            assertEquals(8, padding.top());
            assertEquals(8, padding.right());
            assertEquals(8, padding.bottom());
            assertEquals(8, padding.left());
        }

        @Test
        @DisplayName("should create padding with vertical and horizontal values")
        void constructor_withTwoValues() {
            Padding padding = new Padding(7, 4);
            assertEquals(7, padding.top());
            assertEquals(4, padding.right());
            assertEquals(7, padding.bottom());
            assertEquals(4, padding.left());
        }

        @Test
        @DisplayName("should create padding with individual values for each side")
        void constructor_withFourValues() {
            Padding padding = new Padding(3, 5, 7, 9);
            assertEquals(3, padding.top());
            assertEquals(5, padding.right());
            assertEquals(7, padding.bottom());
            assertEquals(9, padding.left());
        }

        @Test
        @DisplayName("should allow zero values")
        void constructor_withZeroValues() {
            Padding padding = new Padding(0, 0, 0, 0);
            assertEquals(0, padding.top());
            assertEquals(0, padding.right());
            assertEquals(0, padding.bottom());
            assertEquals(0, padding.left());
        }

        @Test
        @DisplayName("should throw an exception if any value is negative")
        void constructor_throwsForNegativeValues() {
            String expectedMessage = "Padding values cannot be negative";
            assertEquals(expectedMessage, assertThrows(IllegalArgumentException.class, () -> new Padding(-1, 5, 5, 5)).getMessage());
            assertEquals(expectedMessage, assertThrows(IllegalArgumentException.class, () -> new Padding(5, -1, 5, 5)).getMessage());
            assertEquals(expectedMessage, assertThrows(IllegalArgumentException.class, () -> new Padding(5, 5, -1, 5)).getMessage());
            assertEquals(expectedMessage, assertThrows(IllegalArgumentException.class, () -> new Padding(5, 5, 5, -1)).getMessage());
        }
    }

    @Nested
    @DisplayName("Static Factories and Constants")
    class FactoriesAndConstantsTest {
        @Test
        @DisplayName("all() should create padding with equal values")
        void all_createsPaddingWithEqualValues() {
            Padding padding = Padding.all(5);
            assertEquals(5, padding.top());
            assertEquals(5, padding.right());
            assertEquals(5, padding.bottom());
            assertEquals(5, padding.left());
        }

        @Test
        @DisplayName("all() should throw an exception for a negative value")
        void all_throwsForNegativeValue() {
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> Padding.all(-1));
            assertEquals("Padding values cannot be negative", exception.getMessage());
        }

        @Test
        @DisplayName("ZERO constant should have all zero values")
        void zero_hasZeroValues() {
            Padding padding = Padding.ZERO;
            assertEquals(0, padding.top());
            assertEquals(0, padding.right());
            assertEquals(0, padding.bottom());
            assertEquals(0, padding.left());
        }
    }
}