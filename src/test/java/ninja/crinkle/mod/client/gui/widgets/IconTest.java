package ninja.crinkle.mod.client.gui.widgets;

import net.minecraft.resources.ResourceLocation;
import ninja.crinkle.mod.client.gui.managers.GuiManager;
import ninja.crinkle.mod.client.gui.properties.Rect;
import ninja.crinkle.mod.client.gui.textures.TextureSize;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Icon")
class IconTest {

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
        @DisplayName("should initialize with default texture size of 16x16")
        void defaultTextureSize() {
            Icon icon = new Icon.Builder(parent).build();
            assertEquals(TextureSize.of(16, 16), icon.textureSize());
        }

        @Test
        @DisplayName("should initialize with null texture when not set")
        void nullTexture() {
            Icon icon = new Icon.Builder(parent).build();
            assertNull(icon.texture());
        }

        @Test
        @DisplayName("should initialize with provided texture and size")
        void providedValues() {
            ResourceLocation tex = new ResourceLocation("crinklemod", "test_icon");
            TextureSize size = TextureSize.of(32, 32);
            Icon icon = new Icon.Builder(parent).texture(tex).textureSize(size).build();

            assertEquals(tex, icon.texture());
            assertEquals(size, icon.textureSize());
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
        @DisplayName("minimumWidth should return texture width")
        void minimumWidth_fromTexture() {
            Icon icon = new Icon.Builder(parent).textureSize(TextureSize.of(24, 32)).build();
            assertEquals(24, icon.minimumWidth());
        }

        @Test
        @DisplayName("minimumHeight should return texture height")
        void minimumHeight_fromTexture() {
            Icon icon = new Icon.Builder(parent).textureSize(TextureSize.of(24, 32)).build();
            assertEquals(32, icon.minimumHeight());
        }

        @Test
        @DisplayName("explicit minimum size should take precedence over texture size")
        void explicitMinSize() {
            Icon icon = new Icon.Builder(parent)
                    .textureSize(TextureSize.of(16, 16))
                    .minSize(48, 48)
                    .build();
            assertEquals(48, icon.minimumWidth());
            assertEquals(48, icon.minimumHeight());
        }

        @Test
        @DisplayName("should return 0 when texture size is null")
        void nullTextureSize() {
            Icon icon = new Icon.Builder(parent).textureSize(null).build();
            assertEquals(0, icon.minimumWidth());
            assertEquals(0, icon.minimumHeight());
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
        @DisplayName("texture setter should update texture")
        void textureSetter() {
            Icon icon = new Icon.Builder(parent).build();
            ResourceLocation tex = new ResourceLocation("crinklemod", "updated");
            icon.texture(tex);
            assertEquals(tex, icon.texture());
        }

        @Test
        @DisplayName("textureSize setter should update texture size")
        void textureSizeSetter() {
            Icon icon = new Icon.Builder(parent).build();
            TextureSize size = TextureSize.of(64, 64);
            icon.textureSize(size);
            assertEquals(size, icon.textureSize());
        }

        @Test
        @DisplayName("pushAndReturn should add icon to parent")
        void pushAndReturn() {
            Icon icon = new Icon.Builder(parent).pushAndReturn();
            assertTrue(parent.children().contains(icon));
        }

        @Test
        @DisplayName("push should return parent container")
        void push() {
            AbstractContainer result = new Icon.Builder(parent).push();
            assertEquals(parent, result);
        }
    }

    @Nested
    @DisplayName("Visual Copy")
    class VisualCopyTest {
        private GuiManager manager;
        private Container parent;

        @BeforeEach
        void setUp() {
            manager = GuiManager.create();
            parent = new Container.Builder(manager).build();
        }

        @Test
        @DisplayName("visualCopy should create a copy with same texture and size")
        void visualCopy() {
            ResourceLocation tex = new ResourceLocation("crinklemod", "copy_test");
            TextureSize size = TextureSize.of(32, 32);
            Icon icon = new Icon.Builder(parent).texture(tex).textureSize(size).build();
            icon.setRect(new Rect(10, 20, 32, 32));

            Container newParent = new Container.Builder(manager).build();
            Icon copy = (Icon) icon.visualCopy(newParent);

            assertEquals(tex, copy.texture());
            assertEquals(size, copy.textureSize());
        }
    }
}