package ninja.crinkle.mod.client.gui.textures;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ColorFiltersTest {
    @Test
    void testGrayscale() {
        int color = 0xFF112233; // Example ARGB color
        int expectedGray = (int) (0x11 * 0.3 + 0x22 * 0.59 + 0x33 * 0.11); // Grayscale luminance
        int expected = (0xFF << 24) | (expectedGray << 16) | (expectedGray << 8) | expectedGray;
        int result = ColorFilters.GRAYSCALE.apply(color);
        assertEquals(expected, result, "GRAYSCALE filter should convert RGB to grayscale and leave alpha unchanged.");
    }

    @Test
    void testInvert() {
        int color = 0xFF112233; // Example ARGB color
        int alpha = (color >> 24) & 0xFF;

        // Dynamically calculate the expected inverted color
        int invertedRed = 255 - ((color >> 16) & 0xFF);
        int invertedGreen = 255 - ((color >> 8) & 0xFF);
        int invertedBlue = 255 - (color & 0xFF);

        int expected = (alpha << 24) | (invertedRed << 16) | (invertedGreen << 8) | invertedBlue;
        int result = ColorFilters.INVERT.apply(color);

        assertEquals(expected, result, "INVERT filter should invert the RGB channels and leave alpha unchanged.");
    }

    @Test
    void testNormal() {
        int color = 0xFF112233; // Example ARGB color
        int result = ColorFilters.NORMAL.apply(color);
        assertEquals(color, result, "NORMAL filter should return the input unchanged.");
    }

    @Test
    void testSepia() {
        int color = 0xFF112233; // Example ARGB color
        int alpha = (color >> 24) & 0xFF;
        int red = (color >> 16) & 0xFF;
        int green = (color >> 8) & 0xFF;
        int blue = color & 0xFF;

        // Calculate expected sepia tone using the same formula as the filter
        int expectedRed = Math.min(255, (int) (red * 0.393 + green * 0.769 + blue * 0.189));
        int expectedGreen = Math.min(255, (int) (red * 0.349 + green * 0.686 + blue * 0.168));
        int expectedBlue = Math.min(255, (int) (red * 0.272 + green * 0.534 + blue * 0.131));

        int expected = (alpha << 24) | (expectedRed << 16) | (expectedGreen << 8) | expectedBlue;
        int result = ColorFilters.SEPIA.apply(color);

        assertEquals(expected, result, "SEPIA filter should apply a sepia tone to the RGB channels and leave alpha " +
                "unchanged.");
    }

}