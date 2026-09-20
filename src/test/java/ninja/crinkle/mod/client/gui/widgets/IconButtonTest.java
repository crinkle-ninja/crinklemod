package ninja.crinkle.mod.client.gui.widgets;

import net.minecraft.resources.ResourceLocation;
import ninja.crinkle.mod.client.gui.managers.GuiManager;
import ninja.crinkle.mod.client.gui.properties.Rect;
import ninja.crinkle.mod.client.gui.textures.TextureSize;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("IconButton")
class IconButtonTest {

    @Nested
    @DisplayName("Constructor")
    class ConstructorTest {
        private GuiManager manager;
        private Container parent;

        @BeforeEach
        void setUp() {
            manager = GuiManager.create();
            parent = new Container.Builder(manager).build();
        }

        @Test
        @DisplayName("should create icon button with icon from texture")
        void withTexture() {
            ResourceLocation tex = new ResourceLocation("crinklemod", "test");
            IconButton button = new IconButton.Builder(parent)
                    .texture(tex)
                    .build();

            assertNotNull(button.icon());
            assertEquals(tex, button.icon().texture());
        }

        @Test
        @DisplayName("should create icon button with text label")
        void withText() {
            IconButton button = new IconButton.Builder(parent)
                    .text("Click me")
                    .build();

            assertNotNull(button.label());
            assertEquals("Click me", button.label().text());
        }

        @Test
        @DisplayName("should create icon button with both icon and text")
        void withIconAndText() {
            ResourceLocation tex = new ResourceLocation("crinklemod", "test");
            IconButton button = new IconButton.Builder(parent)
                    .texture(tex)
                    .text("Click me")
                    .build();

            assertNotNull(button.icon());
            assertNotNull(button.label());
        }

        @Test
        @DisplayName("should create icon button with no icon or text")
        void empty() {
            IconButton button = new IconButton.Builder(parent).build();

            assertNull(button.icon());
            assertNull(button.label());
        }

        @Test
        @DisplayName("should have button behavior defaults")
        void defaults() {
            IconButton button = new IconButton.Builder(parent).build();

            assertTrue(button.active());
            assertTrue(button.focusable());
            assertTrue(button.pressable());
        }
    }

    @Nested
    @DisplayName("Minimum Size")
    class MinimumSizeTest {
        private GuiManager manager;
        private Container parent;

        @BeforeEach
        void setUp() {
            manager = GuiManager.create();
            parent = new Container.Builder(manager).build();
        }

        @Test
        @DisplayName("minimumHeight should account for icon size")
        void minimumHeight_withIcon() {
            IconButton button = new IconButton.Builder(parent)
                    .texture(new ResourceLocation("crinklemod", "test"))
                    .textureSize(TextureSize.of(32, 32))
                    .build();

            assertTrue(button.minimumHeight() >= 32);
        }

        @Test
        @DisplayName("minimumWidth should account for icon size")
        void minimumWidth_withIcon() {
            IconButton button = new IconButton.Builder(parent)
                    .texture(new ResourceLocation("crinklemod", "test"))
                    .textureSize(TextureSize.of(32, 32))
                    .build();

            assertTrue(button.minimumWidth() >= 32);
        }

        @Test
        @DisplayName("explicit minimum size should take precedence")
        void explicitMinSize() {
            IconButton button = new IconButton.Builder(parent)
                    .minSize(100, 50)
                    .build();

            assertEquals(100, button.minimumWidth());
            assertEquals(50, button.minimumHeight());
        }
    }

    @Nested
    @DisplayName("Arrange")
    class ArrangeTest {
        private GuiManager manager;
        private Container parent;

        @BeforeEach
        void setUp() {
            manager = GuiManager.create();
            parent = new Container.Builder(manager).build();
        }

        @Test
        @DisplayName("arrange should not throw when rect is zero")
        void arrangeWithZeroRect() {
            IconButton button = new IconButton.Builder(parent)
                    .texture(new ResourceLocation("crinklemod", "test"))
                    .build();

            assertDoesNotThrow(button::arrange);
        }

        @Test
        @DisplayName("arrange should position children when rect is set")
        void arrangeWithRect() {
            IconButton button = new IconButton.Builder(parent)
                    .texture(new ResourceLocation("crinklemod", "test"))
                    .build();
            button.setRect(new Rect(10, 10, 100, 50));

            assertDoesNotThrow(button::arrange);
            assertFalse(button.children().isEmpty());
        }
    }

    @Nested
    @DisplayName("Properties")
    class PropertiesTest {
        private GuiManager manager;
        private Container parent;

        @BeforeEach
        void setUp() {
            manager = GuiManager.create();
            parent = new Container.Builder(manager).build();
        }

        @Test
        @DisplayName("pushAndReturn should add button to parent")
        void pushAndReturn() {
            IconButton button = new IconButton.Builder(parent).pushAndReturn();
            assertTrue(parent.children().contains(button));
        }

        @Test
        @DisplayName("push should return parent container")
        void push() {
            AbstractContainer result = new IconButton.Builder(parent).push();
            assertEquals(parent, result);
        }

        @Test
        @DisplayName("toString should contain class name")
        void toStringFormat() {
            IconButton button = new IconButton.Builder(parent).build();
            assertTrue(button.toString().startsWith("IconButton{"));
        }
    }
}