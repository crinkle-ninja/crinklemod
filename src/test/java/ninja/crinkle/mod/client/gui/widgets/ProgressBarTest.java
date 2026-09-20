package ninja.crinkle.mod.client.gui.widgets;

import ninja.crinkle.mod.client.color.Color;
import ninja.crinkle.mod.client.gui.managers.GuiManager;
import ninja.crinkle.mod.client.gui.properties.Rect;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ProgressBar")
class ProgressBarTest {

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
        @DisplayName("should initialize with default values")
        void defaults() {
            ProgressBar bar = new ProgressBar.Builder(parent).build();
            assertEquals(0, bar.value());
            assertEquals(1, bar.maxValue());
            assertEquals(Color.GREEN, bar.fillColor());
            assertEquals(Color.LIGHT_GRAY, bar.backgroundColor());
        }

        @Test
        @DisplayName("should initialize with provided values")
        void providedValues() {
            ProgressBar bar = new ProgressBar.Builder(parent)
                    .value(3).maxValue(10)
                    .fillColor(Color.YELLOW).backgroundColor(Color.BLACK)
                    .build();

            assertEquals(3, bar.value());
            assertEquals(10, bar.maxValue());
            assertEquals(Color.YELLOW, bar.fillColor());
            assertEquals(Color.BLACK, bar.backgroundColor());
        }
    }

    @Nested
    @DisplayName("Progress Calculation")
    class ProgressTest {
        private GuiManager manager;
        private Container parent;

        @BeforeEach
        void setUp() {
            manager = GuiManager.create();
            parent = new Container.Builder(manager).build();
        }

        @Test
        @DisplayName("progress should return 0 when value is 0")
        void zero() {
            ProgressBar bar = new ProgressBar.Builder(parent).value(0).maxValue(10).build();
            assertEquals(0.0, bar.progress(), 0.001);
        }

        @Test
        @DisplayName("progress should return 1 when value equals max")
        void full() {
            ProgressBar bar = new ProgressBar.Builder(parent).value(10).maxValue(10).build();
            assertEquals(1.0, bar.progress(), 0.001);
        }

        @Test
        @DisplayName("progress should return 0.5 when half full")
        void half() {
            ProgressBar bar = new ProgressBar.Builder(parent).value(5).maxValue(10).build();
            assertEquals(0.5, bar.progress(), 0.001);
        }

        @Test
        @DisplayName("progress should clamp to 1 when value exceeds max")
        void overflow() {
            ProgressBar bar = new ProgressBar.Builder(parent).value(15).maxValue(10).build();
            assertEquals(1.0, bar.progress(), 0.001);
        }

        @Test
        @DisplayName("progress should return 0 when maxValue is 0")
        void zeroMax() {
            ProgressBar bar = new ProgressBar.Builder(parent).value(5).maxValue(0).build();
            assertEquals(0.0, bar.progress(), 0.001);
        }

        @Test
        @DisplayName("progress should return 0 when maxValue is negative")
        void negativeMax() {
            ProgressBar bar = new ProgressBar.Builder(parent).value(5).maxValue(-1).build();
            assertEquals(0.0, bar.progress(), 0.001);
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
        @DisplayName("minimumHeight should return default of 9")
        void defaultHeight() {
            ProgressBar bar = new ProgressBar.Builder(parent).build();
            assertEquals(9, bar.minimumHeight());
        }

        @Test
        @DisplayName("minimumWidth should return default of 60")
        void defaultWidth() {
            ProgressBar bar = new ProgressBar.Builder(parent).build();
            assertEquals(60, bar.minimumWidth());
        }

        @Test
        @DisplayName("explicit minimum size should take precedence")
        void explicitMinSize() {
            ProgressBar bar = new ProgressBar.Builder(parent).minSize(100, 20).build();
            assertEquals(100, bar.minimumWidth());
            assertEquals(20, bar.minimumHeight());
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
        @DisplayName("value setter should update value")
        void valueSetter() {
            ProgressBar bar = new ProgressBar.Builder(parent).build();
            bar.value(7);
            assertEquals(7, bar.value());
        }

        @Test
        @DisplayName("maxValue setter should update maxValue")
        void maxValueSetter() {
            ProgressBar bar = new ProgressBar.Builder(parent).build();
            bar.maxValue(20);
            assertEquals(20, bar.maxValue());
        }

        @Test
        @DisplayName("fillColor setter should update fill color")
        void fillColorSetter() {
            ProgressBar bar = new ProgressBar.Builder(parent).build();
            bar.fillColor(Color.RED);
            assertEquals(Color.RED, bar.fillColor());
        }

        @Test
        @DisplayName("backgroundColor setter should update background color")
        void backgroundColorSetter() {
            ProgressBar bar = new ProgressBar.Builder(parent).build();
            bar.backgroundColor(Color.WHITE);
            assertEquals(Color.WHITE, bar.backgroundColor());
        }

        @Test
        @DisplayName("pushAndReturn should add bar to parent")
        void pushAndReturn() {
            ProgressBar bar = new ProgressBar.Builder(parent).pushAndReturn();
            assertTrue(parent.children().contains(bar));
        }

        @Test
        @DisplayName("push should return parent container")
        void push() {
            AbstractContainer result = new ProgressBar.Builder(parent).push();
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
        @DisplayName("visualCopy should preserve values and colors")
        void visualCopy() {
            ProgressBar bar = new ProgressBar.Builder(parent)
                    .value(3).maxValue(10)
                    .fillColor(Color.YELLOW).backgroundColor(Color.BLACK)
                    .build();
            bar.setRect(new Rect(0, 0, 60, 9));

            Container newParent = new Container.Builder(manager).build();
            ProgressBar copy = (ProgressBar) bar.visualCopy(newParent);

            assertEquals(3, copy.value());
            assertEquals(10, copy.maxValue());
            assertEquals(Color.YELLOW, copy.fillColor());
            assertEquals(Color.BLACK, copy.backgroundColor());
        }
    }
}