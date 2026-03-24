package ninja.crinkle.mod.client.gui.widgets;

import ninja.crinkle.mod.client.gui.events.FocusEnteredEvent;
import ninja.crinkle.mod.client.gui.events.FocusLeftEvent;
import ninja.crinkle.mod.client.gui.events.HoverEvent;
import ninja.crinkle.mod.client.gui.events.TabIndexEvent;
import ninja.crinkle.mod.client.gui.managers.DragManager;
import ninja.crinkle.mod.client.gui.managers.GuiManager;
import ninja.crinkle.mod.client.gui.properties.Point;
import ninja.crinkle.mod.util.ClientUtil;
import ninja.crinkle.mod.util.TestUtil;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("FieldCanBeLocal")
@DisplayName("AbstractWidget")
class AbstractWidgetTest {

    @Nested
    @DisplayName("Constructor")
    class ConstructorTest {
        private GuiManager manager;
        private Container parent;

        @Test
        @DisplayName("should initialize with default values via builder")
        void builderConstructor_defaultValues() {
            TestWidget widget = new TestWidget.Builder(parent).build();

            assertNotNull(widget.name(), "Name should not be null");
            assertTrue(widget.name().startsWith("Unnamed_"), "Should have default name pattern");
            assertNotNull(widget.behavior(), "Behavior should be initialized");
            assertNotNull(widget.display(), "Display should be initialized");
            assertNotNull(widget.rect(), "Rect should be initialized");
            assertEquals(0, widget.tabIndex(), "Default tab index should be 0");
        }

        @BeforeEach
        void setUp() {
            manager = GuiManager.create();
            parent = new Container.Builder(manager).build();
        }
    }

    @Nested
    @DisplayName("Event Dispatching")
    class EventDispatching {
        private final java.util.List<FocusEnteredEvent> focusEnteredEvents = new java.util.ArrayList<>();
        private final java.util.List<FocusLeftEvent> focusLeftEvents = new java.util.ArrayList<>();
        private MockedStatic<ClientUtil> clientUtilMock;
        private GuiManager manager;
        private Container parent;

        @Test
        @DisplayName("focused(false) should dispatch FocusLeftEvent")
        void focused_falseShouldDispatchFocusLeftEvent() {
            TestWidget widget = new TestWidget.Builder(parent).active(true).focusable(true).build();

            manager.eventManager().addListener(FocusLeftEvent.KEY, 0, focusLeftEvents::add);

            // First set to focused, then unfocus
            widget.focused(true);
            widget.focused(false);

            assertEquals(1, focusLeftEvents.size(), "Should dispatch exactly one FocusLeftEvent");
            FocusLeftEvent capturedEvent = focusLeftEvents.get(0);
            assertFalse(capturedEvent.focused(), "Event should indicate unfocused state");
            assertEquals(widget, capturedEvent.focusTarget(), "Event should reference the widget");
        }

        @Test
        @DisplayName("focused() should not dispatch event if state unchanged")
        void focused_shouldNotDispatchEventIfStateUnchanged() {
            TestWidget widget = new TestWidget.Builder(parent).active(true).focusable(true).build();

            manager.eventManager().addListener(FocusEnteredEvent.KEY, 0, focusEnteredEvents::add);

            widget.focused(true);
            focusEnteredEvents.clear(); // Clear the first event

            widget.focused(true); // Try to set again

            assertEquals(0, focusEnteredEvents.size(), "Should not dispatch event when state unchanged");
        }

        @Test
        @DisplayName("focused(true) should dispatch FocusEnteredEvent")
        void focused_trueShouldDispatchFocusEnteredEvent() {
            TestWidget widget = new TestWidget.Builder(parent).focusable(true).build();

            manager.eventManager().addListener(FocusEnteredEvent.KEY, 0, focusEnteredEvents::add);

            widget.focused(true);

            assertEquals(1, focusEnteredEvents.size(), "Should dispatch exactly one FocusEnteredEvent");
            FocusEnteredEvent capturedEvent = focusEnteredEvents.get(0);
            assertTrue(capturedEvent.focused(), "Event should indicate focused state");
            assertEquals(widget, capturedEvent.focusTarget(), "Event should reference the widget");
        }

        @Test
        @DisplayName("hovered(false) should dispatch HoverEvent with hovered=false")
        void hovered_falseShouldDispatchHoverEvent() {
            TestWidget widget = new TestWidget.Builder(parent).active(true).hoverable(true).build();
            java.util.List<HoverEvent> hoverEvents = new java.util.ArrayList<>();

            manager.eventManager().addListener(HoverEvent.KEY, 0, hoverEvents::add);

            // First set to hovered, then unhover
            widget.hovered(true);
            widget.hovered(false);

            assertEquals(2, hoverEvents.size(), "Should dispatch two HoverEvents");
            HoverEvent unhoverEvent = hoverEvents.get(1);
            assertFalse(unhoverEvent.hovered(), "Second event should indicate unhovered state");
        }

        @Test
        @DisplayName("hovered() should not dispatch event if widget not hoverable")
        void hovered_shouldNotDispatchEventIfNotHoverable() {
            TestWidget widget = new TestWidget.Builder(parent).hoverable(false).build();
            java.util.List<HoverEvent> hoverEvents = new java.util.ArrayList<>();

            manager.eventManager().addListener(HoverEvent.KEY, 0, hoverEvents::add);

            widget.hovered(true);

            assertEquals(0, hoverEvents.size(), "Should not dispatch event when not hoverable");
        }

        @Test
        @DisplayName("hovered() should not dispatch event if state unchanged")
        void hovered_shouldNotDispatchEventIfStateUnchanged() {
            TestWidget widget = new TestWidget.Builder(parent).active(true).hoverable(true).build();
            java.util.List<HoverEvent> hoverEvents = new java.util.ArrayList<>();

            manager.eventManager().addListener(HoverEvent.KEY, 0, hoverEvents::add);

            widget.hovered(true);
            hoverEvents.clear(); // Clear the first event

            widget.hovered(true); // Try to set again

            assertEquals(0, hoverEvents.size(), "Should not dispatch event when state unchanged");
        }

        @Test
        @DisplayName("hovered(true) should dispatch HoverEvent with hovered=true")
        void hovered_trueShouldDispatchHoverEvent() {
            TestWidget widget = new TestWidget.Builder(parent).hoverable(true).build();
            java.util.List<HoverEvent> hoverEvents = new java.util.ArrayList<>();

            manager.eventManager().addListener(HoverEvent.KEY, 0, hoverEvents::add);

            widget.hovered(true);

            assertEquals(1, hoverEvents.size(), "Should dispatch exactly one HoverEvent");
            HoverEvent capturedEvent = hoverEvents.get(0);
            assertTrue(capturedEvent.hovered(), "Event should indicate hovered state");
        }

        @BeforeEach
        void setUp() {
            manager = GuiManager.create();
            parent = new Container.Builder(manager).build();
            focusEnteredEvents.clear();
            focusLeftEvents.clear();

            // Mock ClientUtil for hover tests (includes mouseHandler mocking)
            clientUtilMock = TestUtil.mockClientUtil();
            // Explicitly stub getMousePosition() since hovered() calls it directly
            clientUtilMock.when(ClientUtil::getMousePosition).thenReturn(new Point(0, 0));
        }

        @Test
        @DisplayName("tabIndex() should dispatch TabIndexEvent when changed")
        void tabIndex_shouldDispatchTabIndexEvent() {
            TestWidget widget = new TestWidget.Builder(parent).tabIndex(0).build();
            java.util.List<TabIndexEvent> tabIndexEvents = new java.util.ArrayList<>();

            manager.eventManager().addListener(TabIndexEvent.KEY, 0, tabIndexEvents::add);

            widget.tabIndex(5);

            assertEquals(1, tabIndexEvents.size(), "Should dispatch exactly one TabIndexEvent");
            TabIndexEvent capturedEvent = tabIndexEvents.get(0);
            assertEquals(0, capturedEvent.oldIndex(), "Old index should be 0");
            assertEquals(5, capturedEvent.newIndex(), "New index should be 5");
            assertEquals(widget, capturedEvent.focusTarget(), "Event should reference the widget");
        }

        @Test
        @DisplayName("tabIndex() should not dispatch event if index unchanged")
        void tabIndex_shouldNotDispatchEventIfIndexUnchanged() {
            TestWidget widget = new TestWidget.Builder(parent).tabIndex(5).build();
            java.util.List<TabIndexEvent> tabIndexEvents = new java.util.ArrayList<>();

            manager.eventManager().addListener(TabIndexEvent.KEY, 0, tabIndexEvents::add);

            widget.tabIndex(5); // Set to same value

            assertEquals(0, tabIndexEvents.size(), "Should not dispatch event when index unchanged");
        }

        @AfterEach
        void tearDown() {
            if (clientUtilMock != null) {
                clientUtilMock.close();
            }
        }
    }

    @Nested
    @DisplayName("Exception Handling")
    class ExceptionHandlingTest {
        private GuiManager manager;
        private Container parent;

        @Test
        @DisplayName("builder should throw exception when parent's manager is null")
        void builder_shouldThrowExceptionWhenManagerIsNull() {
            assertNotNull(parent.manager(), "Test setup should have valid manager");
        }

        @Test
        @DisplayName("builder should throw exception when parent is null")
        void builder_shouldThrowExceptionWhenParentIsNull() {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> new TestWidget.Builder((AbstractContainer) null).build(),
                    "Should throw exception when parent is null"
            );
        }

        @Test
        @DisplayName("parentOrThrow() should throw exception when parent is null")
        void parentOrThrow_shouldThrowExceptionWhenParentIsNull() {
            TestWidget widget = new TestWidget.Builder(parent).build();

            assertNotNull(widget.parentOrThrow(), "Parent should not be null for properly constructed widget");
        }

        @BeforeEach
        void setUp() {
            manager = GuiManager.create();
            parent = new Container.Builder(manager).build();
        }

        @Test
        @DisplayName("zIndex() should accept value just below Z_MAX")
        void zIndex_shouldAcceptValueJustBelowMax() {
            TestWidget widget = new TestWidget.Builder(parent).build();

            assertDoesNotThrow(() -> widget.zIndex(DragManager.Z_MAX - 1),
                    "Should not throw exception when zIndex < Z_MAX"
            );

            assertEquals(DragManager.Z_MAX - 1, widget.zIndex());
        }

        @Test
        @DisplayName("zIndex() should throw exception when value > Z_MAX")
        void zIndex_shouldThrowExceptionWhenValueAboveMax() {
            TestWidget widget = new TestWidget.Builder(parent).build();

            assertThrows(
                    IllegalArgumentException.class,
                    () -> widget.zIndex(DragManager.Z_MAX + 1),
                    "Should throw exception when zIndex > Z_MAX"
            );
        }

        @Test
        @DisplayName("zIndex() should throw exception when value >= Z_MAX")
        void zIndex_shouldThrowExceptionWhenValueTooHigh() {
            TestWidget widget = new TestWidget.Builder(parent).build();

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> widget.zIndex(DragManager.Z_MAX),
                    "Should throw exception when zIndex >= Z_MAX"
            );

            assertEquals("Z-index cannot be set to maximum value", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Properties")
    class PropertiesTest {
        private GuiManager manager;
        private Container parent;

        @Test
        @DisplayName("behavior() should return widget behavior")
        void behavior_shouldReturnWidgetBehavior() {
            TestWidget widget = new TestWidget.Builder(parent).build();

            assertNotNull(widget.behavior(), "Behavior should not be null");
        }

        @Test
        @DisplayName("display() should return widget display")
        void display_shouldReturnWidgetDisplay() {
            TestWidget widget = new TestWidget.Builder(parent).build();

            assertNotNull(widget.display(), "Display should not be null");
        }

        @Test
        @DisplayName("manager() should return GUI manager")
        void manager_shouldReturnGuiManager() {
            TestWidget widget = new TestWidget.Builder(parent).build();

            assertNotNull(widget.manager(), "Manager should not be null");
            assertEquals(manager, widget.manager(), "Manager should match the one used in builder");
        }

        @Test
        @DisplayName("name() should return widget name")
        void name_shouldReturnWidgetName() {
            String widgetName = "test_widget";
            TestWidget widget = new TestWidget.Builder(parent).name(widgetName).build();

            assertNotNull(widget.name(), "Name should not be null");
            assertEquals(widgetName, widget.name(), "Name should be " + widgetName);
        }

        @Test
        @DisplayName("parent() should return parent container")
        void parent_shouldReturnParentContainer() {
            TestWidget widget = new TestWidget.Builder(parent).build();

            assertTrue(widget.parent().isPresent(), "Parent should be present");
            assertEquals(parent, widget.parent().get(), "Parent should match the one used in builder");
        }

        @Test
        @DisplayName("priority() should return priority value")
        void priority_shouldReturnPriorityValue() {
            int priority = 100;
            TestWidget widget = new TestWidget.Builder(parent).priority(priority).build();

            assertEquals(priority, widget.priority(), "Priority should be " + priority);
        }

        @Test
        @DisplayName("rect() should return widget rect")
        void rect_shouldReturnWidgetRect() {
            TestWidget widget = new TestWidget.Builder(parent).build();

            assertNotNull(widget.rect(), "Rect should not be null");
        }

        @BeforeEach
        void setUp() {
            manager = GuiManager.create();
            parent = new Container.Builder(manager).build();
        }

        @Test
        @DisplayName("style() should return null when no style is set")
        void style_shouldReturnNullWhenNoStyleSet() {
            TestWidget widget = new TestWidget.Builder(parent).build();

            assertNull(widget.style(), "Widget style should be null when no style is set");
        }

        @Test
        @DisplayName("tabIndex() should return tab index")
        void tabIndex_shouldReturnTabIndex() {
            int tabIndex = 42;
            TestWidget widget = new TestWidget.Builder(parent).tabIndex(tabIndex).build();

            assertEquals(tabIndex, widget.tabIndex(), "Tab index should be " + tabIndex);
        }

        @Test
        @DisplayName("zIndex() should return z-index value")
        void zIndex_shouldReturnZIndexValue() {
            int zIndex = 50;
            TestWidget widget = new TestWidget.Builder(parent).zIndex(zIndex).build();

            assertEquals(zIndex, widget.zIndex(), "Z-index should be " + zIndex);
        }
    }

    @Nested
    @DisplayName("State Mutations")
    class StateMutationsTest {
        private GuiManager manager;
        private Container parent;

        @Test
        @DisplayName("active() setter should update active state")
        void active_setterShouldUpdateState() {
            TestWidget widget = new TestWidget.Builder(parent).active(false).build();

            widget.active(true);
            assertTrue(widget.active(), "Should be active after setting to true");

            widget.active(false);
            assertFalse(widget.active(), "Should not be active after setting to false");
        }

        @Test
        @DisplayName("alpha() setter should update alpha value")
        void alpha_setterShouldUpdateValue() {
            TestWidget widget = new TestWidget.Builder(parent).build();

            widget.alpha(0.75f);
            assertEquals(0.75f, widget.alpha(), 0.001f, "Alpha should be 0.75");

            widget.alpha(0.25f);
            assertEquals(0.25f, widget.alpha(), 0.001f, "Alpha should be 0.25");
        }

        @Test
        @DisplayName("dragged() setter should update dragged state")
        void dragged_setterShouldUpdateState() {
            TestWidget widget = new TestWidget.Builder(parent).build();

            // Must be draggable to set dragged
            widget.draggable(true);
            widget.dragged(true);
            assertTrue(widget.dragged(), "Should be dragged after setting to true");

            widget.dragged(false);
            assertFalse(widget.dragged(), "Should not be dragged after setting to false");

            widget.draggable(false);
            assertThrows(IllegalArgumentException.class, () -> widget.dragged(true), "should throw exception if " +
                    "widget is not draggable");
        }

        @Test
        @DisplayName("name() setter should update name")
        void name_setterShouldUpdateName() {
            TestWidget widget = new TestWidget.Builder(parent).name("initial").build();

            widget.name("updated");
            assertEquals("updated", widget.name(), "Name should be updated");

            widget.name("final");
            assertEquals("final", widget.name(), "Name should be final");
        }

        @Test
        @DisplayName("pressed() setter should update pressed state")
        void pressed_setterShouldUpdateState() {
            TestWidget widget = new TestWidget.Builder(parent).active(true).build();

            widget.pressed(true);
            assertTrue(widget.pressed(), "Should be pressed after setting to true");

            widget.pressed(false);
            assertFalse(widget.pressed(), "Should not be pressed after setting to false");
        }

        @Test
        @DisplayName("priority() setter should update priority")
        void priority_setterShouldUpdatePriority() {
            TestWidget widget = new TestWidget.Builder(parent).priority(10).build();

            widget.priority(50);
            assertEquals(50, widget.priority(), "Priority should be 50");

            widget.priority(100);
            assertEquals(100, widget.priority(), "Priority should be 100");
        }

        @BeforeEach
        void setUp() {
            manager = GuiManager.create();
            parent = new Container.Builder(manager).build();
        }

        @Test
        @DisplayName("visible() setter should update visible state")
        void visible_setterShouldUpdateState() {
            TestWidget widget = new TestWidget.Builder(parent).visible(false).build();

            widget.visible(true);
            assertTrue(widget.visible(), "Should be visible after setting to true");

            widget.visible(false);
            assertFalse(widget.visible(), "Should not be visible after setting to false");
        }

        @Test
        @DisplayName("zIndex() setter should update z-index")
        void zIndex_setterShouldUpdateZIndex() {
            TestWidget widget = new TestWidget.Builder(parent).zIndex(10).build();

            widget.zIndex(100);
            assertEquals(100, widget.zIndex(), "Z-index should be 100");

            widget.zIndex(200);
            assertEquals(200, widget.zIndex(), "Z-index should be 200");
        }
    }

    @Nested
    @DisplayName("State")
    class StateTest {
        private GuiManager manager;
        private Container parent;

        @Test
        @DisplayName("active() should return active state")
        void active_shouldReturnActiveState() {
            TestWidget activeWidget = new TestWidget.Builder(parent).active(true).build();
            assertTrue(activeWidget.active(), "Should be active");

            TestWidget inactiveWidget = new TestWidget.Builder(parent).active(false).build();
            assertFalse(inactiveWidget.active(), "Should not be active");
        }

        @Test
        @DisplayName("alpha() should return alpha value")
        void alpha_shouldReturnAlphaValue() {
            TestWidget widget = new TestWidget.Builder(parent).alpha(0.5f).build();
            assertEquals(0.5f, widget.alpha(), 0.001f, "Alpha should be 0.5");
        }

        @Test
        @DisplayName("draggable() should return draggable state")
        void draggable_shouldReturnDraggableState() {
            TestWidget draggableWidget = new TestWidget.Builder(parent).draggable(true).build();
            assertTrue(draggableWidget.draggable(), "Should be draggable");

            TestWidget nonDraggableWidget = new TestWidget.Builder(parent).draggable(false).build();
            assertFalse(nonDraggableWidget.draggable(), "Should not be draggable");
        }

        @Test
        @DisplayName("focusable() should return focusable state")
        void focusable_shouldReturnFocusableState() {
            TestWidget focusableWidget = new TestWidget.Builder(parent).focusable(true).build();
            assertTrue(focusableWidget.focusable(), "Should be focusable");

            TestWidget nonFocusableWidget = new TestWidget.Builder(parent).focusable(false).build();
            assertFalse(nonFocusableWidget.focusable(), "Should not be focusable");
        }

        @Test
        @DisplayName("pressable() should return pressable state")
        void pressable_shouldReturnPressableState() {
            TestWidget pressableWidget = new TestWidget.Builder(parent).pressable(true).build();
            assertTrue(pressableWidget.pressable(), "Should be pressable");

            TestWidget nonPressableWidget = new TestWidget.Builder(parent).pressable(false).build();
            assertFalse(nonPressableWidget.pressable(), "Should not be pressable");
        }

        @BeforeEach
        void setUp() {
            manager = GuiManager.create();
            parent = new Container.Builder(manager).build();
        }

        @Test
        @DisplayName("visible() should return visible state")
        void visible_shouldReturnVisibleState() {
            TestWidget visibleWidget = new TestWidget.Builder(parent).visible(true).build();
            assertTrue(visibleWidget.visible(), "Should be visible");

            TestWidget invisibleWidget = new TestWidget.Builder(parent).visible(false).build();
            assertFalse(invisibleWidget.visible(), "Should not be visible");
        }
    }
}