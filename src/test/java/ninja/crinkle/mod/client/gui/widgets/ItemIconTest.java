package ninja.crinkle.mod.client.gui.widgets;

import net.minecraft.resources.ResourceLocation;
import ninja.crinkle.mod.client.gui.managers.GuiManager;
import ninja.crinkle.mod.client.gui.textures.TextureSize;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ItemIcon")
class ItemIconTest {

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
        @DisplayName("should initialize with null item stack when not set")
        void nullItemStack() {
            ItemIcon icon = new ItemIcon.Builder(parent).build();
            assertNull(icon.itemStack());
        }

        @Test
        @DisplayName("should be an instance of Icon")
        void isIcon() {
            ItemIcon icon = new ItemIcon.Builder(parent).build();
            assertInstanceOf(Icon.class, icon);
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
        @DisplayName("minimumWidth should return default item size of 16")
        void minimumWidth_default() {
            ItemIcon icon = new ItemIcon.Builder(parent).build();
            assertEquals(16, icon.minimumWidth());
        }

        @Test
        @DisplayName("minimumHeight should return default item size of 16")
        void minimumHeight_default() {
            ItemIcon icon = new ItemIcon.Builder(parent).build();
            assertEquals(16, icon.minimumHeight());
        }

        @Test
        @DisplayName("explicit minimum size should take precedence")
        void explicitMinSize() {
            ItemIcon icon = new ItemIcon.Builder(parent)
                    .minSize(48, 48)
                    .build();
            assertEquals(48, icon.minimumWidth());
            assertEquals(48, icon.minimumHeight());
        }

        @Test
        @DisplayName("minimumWidth uses Icon textureSize when larger than default")
        void minimumWidth_inheritsFromIconTextureSize() {
            // super.minimumWidth() calls Icon.minimumWidth() which returns textureSize width (24)
            // Since 24 > 0, ItemIcon returns it as the explicit value
            ItemIcon icon = new ItemIcon.Builder(parent)
                    .textureSize(TextureSize.of(24, 24))
                    .build();
            assertEquals(24, icon.minimumWidth());
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
        @DisplayName("itemStack setter should update item stack")
        void itemStackSetter() {
            ItemIcon icon = new ItemIcon.Builder(parent).build();
            icon.itemStack(null);
            assertNull(icon.itemStack());
        }

        @Test
        @DisplayName("pushAndReturn should add icon to parent")
        void pushAndReturn() {
            ItemIcon icon = new ItemIcon.Builder(parent).pushAndReturn();
            assertTrue(parent.children().contains(icon));
        }

        @Test
        @DisplayName("push should return parent container")
        void push() {
            AbstractContainer result = new ItemIcon.Builder(parent).push();
            assertEquals(parent, result);
        }

        @Test
        @DisplayName("texture and textureSize are inherited from Icon")
        void inheritedFromIcon() {
            ResourceLocation tex = new ResourceLocation("crinklemod", "test");
            TextureSize size = TextureSize.of(32, 32);
            ItemIcon icon = new ItemIcon.Builder(parent)
                    .texture(tex)
                    .textureSize(size)
                    .build();
            assertEquals(tex, icon.texture());
            assertEquals(size, icon.textureSize());
        }
    }
}