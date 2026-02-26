package ninja.crinkle.mod.client.gui.properties;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Size Properties")
class SizeTest {

    @Nested
    @DisplayName("Static Factories and Constants")
    class FactoriesAndConstantsTest {
        @Test
        @DisplayName("ofPixels() should create a size with pixel units")
        void ofPixels_createsPixelSize() {
            Size size = Size.ofPixels(10, 20);
            assertEquals(10, size.width());
            assertEquals(20, size.height());
            assertEquals(Size.Unit.Pixels, size.unit());
        }

        @Test
        @DisplayName("ofPixels() should throw an exception for negative dimensions")
        void ofPixels_throwsForNegativeDimensions() {
            assertThrows(IllegalArgumentException.class, () -> Size.ofPixels(-10, 20));
            assertThrows(IllegalArgumentException.class, () -> Size.ofPixels(10, -20));
            assertThrows(IllegalArgumentException.class, () -> Size.ofPixels(-10, -20));
        }

        @Test
        @DisplayName("ofPercent() should create a size with percent units")
        void ofPercent_createsPercentSize() {
            Size sizeFromInt = Size.ofPercent(10, 20);
            assertEquals(10, sizeFromInt.width());
            assertEquals(20, sizeFromInt.height());
            assertEquals(Size.Unit.Percent, sizeFromInt.unit());

            Size sizeFromFloat = Size.ofPercent(10.5f, 20.5f);
            assertEquals(10.5f, sizeFromFloat.width());
            assertEquals(20.5f, sizeFromFloat.height());
            assertEquals(Size.Unit.Percent, sizeFromFloat.unit());
        }

        @Test
        @DisplayName("ZERO constant should be a zero-sized pixel-based size")
        void zero_isZeroPixelSize() {
            assertEquals(0, Size.ZERO.width());
            assertEquals(0, Size.ZERO.height());
            assertEquals(Size.Unit.Pixels, Size.ZERO.unit());
        }

        @Test
        @DisplayName("RELATIVE_ZERO constant should be a zero-sized percent-based size")
        void relativeZero_isZeroPercentSize() {
            assertEquals(0, Size.RELATIVE_ZERO.width());
            assertEquals(0, Size.RELATIVE_ZERO.height());
            assertEquals(Size.Unit.Percent, Size.RELATIVE_ZERO.unit());
        }
    }

    @Nested
    @DisplayName("Accessors")
    class AccessorsTest {
        @Test
        @DisplayName("widthInt() and heightInt() should return integer parts of dimensions")
        void intAccessors_returnIntegerParts() {
            Size size = Size.ofPercent(10.8f, 20.2f);
            assertEquals(10, size.widthInt());
            assertEquals(20, size.heightInt());
        }
    }

    @Nested
    @DisplayName("Arithmetic Operations")
    class ArithmeticTest {
        @Test
        @DisplayName("add() with a Size should combine dimensions")
        void add_size() {
            Size size1 = Size.ofPixels(10, 15);
            Size size2 = Size.ofPixels(5, 5);
            Size result = size1.add(size2);
            assertEquals(15, result.width());
            assertEquals(20, result.height());
        }

        @Test
        @DisplayName("add() with a Size of a different unit should throw an exception")
        void add_differentUnitSize_throwsException() {
            Size size1 = Size.ofPixels(10, 15);
            Size size2 = Size.ofPercent(5, 5);
            assertThrows(IllegalArgumentException.class, () -> size1.add(size2));
        }

        @Test
        @DisplayName("add() with individual values should update dimensions")
        void add_values() {
            Size original = Size.ofPixels(10, 15);
            assertEquals(new Size(15, 25, Size.Unit.Pixels), original.add(5, 10));
            assertEquals(new Size(5, 5, Size.Unit.Pixels), original.add(-5, -10));
            assertEquals(original, original.add(0, 0));
        }

        @Test
        @DisplayName("subtract() with a Size should reduce dimensions")
        void subtract_size() {
            Size size1 = Size.ofPixels(10, 15);
            Size size2 = Size.ofPixels(5, 10);
            Size result = size1.subtract(size2);
            assertEquals(5, result.width());
            assertEquals(5, result.height());
        }

        @Test
        @DisplayName("subtract() with individual values should reduce dimensions")
        void subtract_values() {
            Size size = Size.ofPixels(10, 15);
            Size result = size.subtract(5, 10);
            assertEquals(5, result.width());
            assertEquals(5, result.height());
        }
    }

    @Nested
    @DisplayName("Comparison")
    class ComparisonTest {
        @Test
        @DisplayName("greaterThan() should return true only if both dimensions are larger")
        void greaterThan_returnsTrueForLargerSize() {
            Size size1 = Size.ofPixels(20, 30);
            Size size2_smaller = Size.ofPixels(10, 15);
            Size size3_equal = Size.ofPixels(20, 30);
            Size size4_mixed = Size.ofPixels(10, 35);

            assertTrue(size1.greaterThan(size2_smaller));
            assertFalse(size2_smaller.greaterThan(size1));
            assertFalse(size1.greaterThan(size3_equal));
            assertFalse(size1.greaterThan(size4_mixed));
        }

        @Test
        @DisplayName("lessThan() should return true only if both dimensions are smaller")
        void lessThan_returnsTrueForSmallerSize() {
            Size size1 = Size.ofPixels(5, 10);
            Size size2_larger = Size.ofPixels(10, 15);
            Size size3_equal = Size.ofPixels(5, 10);
            Size size4_mixed = Size.ofPixels(2, 12);

            assertTrue(size1.lessThan(size2_larger));
            assertFalse(size2_larger.lessThan(size1));
            assertFalse(size1.lessThan(size3_equal));
            assertFalse(size1.lessThan(size4_mixed));
        }
    }

    @Nested
    @DisplayName("Object Contract")
    class ObjectContractTest {
        @Test
        @DisplayName("equals() should correctly compare sizes for equality")
        void equals_verifiesContract() {
            Size size1 = Size.ofPixels(10, 20);
            Size size2_equal = Size.ofPixels(10, 20);
            Size size3_differentValues = Size.ofPixels(5, 15);
            Size size4_differentUnit = Size.ofPercent(10, 20);

            assertEquals(size1, size1);
            assertEquals(size1, size2_equal);
            assertNotEquals(size1, size3_differentValues);
            assertNotEquals(null, size1);
            assertNotEquals(new Object(), size1);
            assertNotEquals(size1, size4_differentUnit, "Sizes with different units should not be equal.");
        }

        @Test
        @DisplayName("hashCode() should be consistent with equals()")
        void hashCode_isConsistentWithEquals() {
            Size size1 = Size.ofPixels(10, 20);
            Size size2_equal = Size.ofPixels(10, 20);
            Size size3_differentUnit = Size.ofPercent(10, 20);

            assertEquals(size1.hashCode(), size2_equal.hashCode());
            assertNotEquals(size1.hashCode(), size3_differentUnit.hashCode(),
                    "Hash codes for sizes with different units should be different.");
        }

        @Test
        @DisplayName("toString() should return a formatted string representation")
        void toString_returnsFormattedString() {
            Size size = Size.ofPixels(10, 20);
            assertEquals("Size{width=10.0, height=20.0, unit=Pixels}", size.toString());
        }
    }
}