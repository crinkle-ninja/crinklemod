package ninja.crinkle.mod.client.gui.themes;

import ninja.crinkle.mod.client.color.Color;
import ninja.crinkle.mod.client.gui.textures.Texture;
import ninja.crinkle.mod.util.ClientUtil;
import ninja.crinkle.mod.util.TestUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StyleVariantTest {

    @Test
    void coalesceWith() {
        try (MockedStatic<ClientUtil> ignored = TestUtil.mockClientUtil()) {
            Texture testTexture = TestUtil.spyTexture(TestUtil.spyTheme());
            Color testColor = Color.BLACK;
            Color backgroundColor = Color.CYAN;
            StyleVariant base = StyleVariant.builder()
                    .backgroundTexture(testTexture)
                    .backgroundColor(backgroundColor)
                    .foregroundColor(testColor)
                    .build();
            Color overridenColor = Color.WHITE;
            StyleVariant variant = StyleVariant.builder()
                    .foregroundColor(overridenColor)
                    .build();
            StyleVariant result = variant.coalesceWith(base);
            assertEquals(testTexture, result.backgroundTexture(), "Background texture check");
            assertEquals(backgroundColor, result.backgroundColor(), "Background color check");
            assertEquals(overridenColor, result.foregroundColor(), "Foreground color check");
        }
    }

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }
}