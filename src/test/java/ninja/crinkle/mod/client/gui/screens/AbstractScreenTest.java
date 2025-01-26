package ninja.crinkle.mod.client.gui.screens;

import net.minecraft.network.chat.Component;
import ninja.crinkle.mod.client.gui.events.*;
import ninja.crinkle.mod.client.gui.events.listeners.EventListener;
import ninja.crinkle.mod.client.gui.events.listeners.KeyListener;
import ninja.crinkle.mod.client.gui.events.listeners.MouseListener;
import ninja.crinkle.mod.client.gui.layouts.Layout;
import ninja.crinkle.mod.client.gui.managers.StateManager;
import ninja.crinkle.mod.client.gui.properties.Point;
import ninja.crinkle.mod.client.gui.themes.Style;
import ninja.crinkle.mod.client.gui.widgets.AbstractWidget;
import ninja.crinkle.mod.client.gui.widgets.Button;
import ninja.crinkle.mod.client.gui.widgets.Container;
import ninja.crinkle.mod.util.ClientUtil;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.stubbing.Answer;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AbstractScreenTest {
    @Mock AbstractScreen screen;

    @BeforeEach
    void setUp() {
        screen = spy(new AbstractScreen(Component.literal("testScreen")) {
            @Override
            public String name() {
                return "testScreen";
            }
        });
        doReturn(StateManager.screen()).when(screen).stateStorageRef();
        doReturn(spy(StateManager.get(screen.stateStorageRef()))).when(screen).stateStorage();
        Container root = spy(Container.builder(screen)
                .name("root")
                .size(600)
                .build());
        doReturn(root).when(screen).root();
        doReturn(true).when(screen).ready();
    }

    private Map<String, AbstractWidget> setupWidgets() {
        Container parent = screen.root().addContainer()
                .name("parent")
                .layoutManager(Layout.vertical().alignment(Layout.Alignment.TOP))
                .draggable(true)
                .size(100)
                .build();
        Container spyParent = spy(parent);
        screen.root().add(spyParent);

        Container child = parent.addContainer()
                .name("child")
                .size(50)
                .build();
        Container spyChild = spy(child);
        parent.add(spyChild);

        Button button = child.addButton()
                .name("button")
                .text("Button")
                .size(50)
                .focusable(true)
                .pressable(true)
                .build();
        Button spyButton = spy(button);
        child.add(spyButton);
        assert (button.priority() < child.priority()) && (child.priority() < parent.priority())
                : "Expected priorities to be in order. " + button.priority() + " < " + child.priority() + " < " + parent.priority();
        assert (button.zIndex() > child.zIndex()) && (child.zIndex() > parent.zIndex())
                : "Expected z-indices to be in order. " + button.zIndex() + " > " + child.zIndex() + " > " + parent.zIndex();
        return Map.of("parent", spyParent, "child", spyChild, "button", spyButton);
    }

    @Contract(pure = true)
    private <T> @NotNull Answer<T> consumeEvent() {
        return invocation -> {
            AbstractEvent event = invocation.getArgument(0);
            event.consumed(true);
            return null;
        };
    }

    private <T extends EventListener> T mockListener(Class<T> listener) {
        T mock = mock(listener);
        doCallRealMethod().when(mock).onEvent(any(AbstractEvent.class));
        return mock;
    }

    @Test
    void keyPressed() {
        doCallRealMethod().when(screen).keyPressed(anyInt(), anyInt(), anyInt());
        KeyListener listener = mockListener(KeyListener.class);
        doAnswer(consumeEvent()).when(listener).onKey(any());
        screen.addListener(listener);
        screen.keyPressed(0, 0, 0);
        verify(listener, times(1)).onKey(assertArg(event -> {
            assert event.pressed() : "Expected key to be pressed";
        }));
    }

    @Test
    void mouseClicked_noDragging() {
        var widgets = setupWidgets();
        doCallRealMethod().when(screen).mouseClicked(anyInt(), anyInt(), anyInt());
        Point button = widgets.get("button").layout().boxes().rendered().box().start().add(2, 2);
        screen.mouseClicked(button.x(), button.y(), 0);
        verify(widgets.get("button"), times(1)).onMousePressed(any());
        verify(widgets.get("button"), times(0)).onDragStarted(any());
        verify(widgets.get("parent"), times(0)).onDragStarted(any());
        verify(widgets.get("child"), times(0)).onDragStarted(any());
        assertFalse(screen.dragManager().dragging(), "Expected not dragging");
        assertTrue(widgets.get("button").pressed(), "Expected button to be pressed");
    }

    @Test
    void mouseClicked_withFocus() throws InterruptedException {
        var widgets = setupWidgets();
        doCallRealMethod().when(screen).mouseClicked(anyInt(), anyInt(), anyInt());
        Point button = widgets.get("button").layout().boxes().rendered().box().start().add(2, 2);
        screen.mouseClicked(button.x(), button.y(), 0);
        verify(widgets.get("button"), times(1)).onMousePressed(any());
        verify(widgets.get("button"), times(0)).onDragStarted(any());
        verify(widgets.get("parent"), times(0)).onDragStarted(any());
        verify(widgets.get("child"), times(0)).onDragStarted(any());
        assertFalse(screen.dragManager().dragging(), "Expected not dragging");
        assertNotNull(screen.focusManager().currentFocus());
        assertEquals(screen.focusManager().currentFocus(), widgets.get("button"), "Expected button to be focused");
        assertTrue(widgets.get("button").pressed(), "Expected button to be pressed");
        assertTrue(widgets.get("button").focused(), "Expected button to be focused");

        // Click away to unfocus
        Point parent = widgets.get("parent").layout().boxes().rendered().box().bottomRight().add(2, 2);
        screen.mouseClicked(parent.x(), parent.y(), 0);
        verify(widgets.get("button"), never()).onClick(any(ClickEvent.class));
        verify(widgets.get("button"), atLeastOnce()).focused(false);
        assertFalse(widgets.get("button").focused(), "Expected button to not be focused");
    }

    @Test
    void mouseClicked_notFocusable() {
        var widgets = setupWidgets();
        Button button = (Button) widgets.get("button");
        button.focusable(false);
        doCallRealMethod().when(screen).mouseClicked(anyInt(), anyInt(), anyInt());
        Point buttonStart = button.layout().boxes().rendered().contentBox().start().add(2, 2);
        screen.mouseClicked(buttonStart.xInt(), buttonStart.yInt(), 0);
        verify(button, times(1)).onMousePressed(any());
        verify(button, times(0)).onDragStarted(any());
        verify(widgets.get("parent"), times(0)).onDragStarted(any());
        verify(widgets.get("child"), times(0)).onDragStarted(any());
        assertFalse(screen.dragManager().dragging(), "Expected not dragging");
        assertTrue(button.pressed(), "Expected button to be pressed");
        assertFalse(button.focused(), "Expected button to not be focused");
    }

    @Test
    void mouseClicked_withDragging() {
        Map<String, AbstractWidget> widgets = setupWidgets();
        Container parent = (Container) widgets.get("parent");
        Container child = (Container) widgets.get("child");
        Button button = (Button) widgets.get("button");
        doCallRealMethod().when(screen).mouseClicked(anyInt(), anyInt(), anyInt());
        MouseListener listener = mockListener(MouseListener.class);
        doAnswer(consumeEvent()).when(listener).onMousePressed(any());
        doReturn("mockedListener").when(listener).name();
        screen.addListener(listener);
        assert screen.eventManager().isPresent() && screen.eventManager().get()
                .listeners(l -> widgets.containsKey(l.name())).size() == 3 : "Expected listeners for all widgets";
        Point clickPoint = parent.layout().boxes().rendered().box().bottomRight().subtract(2, 2);
        screen.mouseClicked(clickPoint.xInt(), clickPoint.yInt(), 0);
        assertEquals(parent.name(), screen.dragManager().current().name(),
                "Expected " + parent.name() + " to be current, instead got "
                        + screen.dragManager().current().name());
        assertFalse(screen.dragManager().dragging(), "Expected not dragging");
    }

    @Test
    void mouseReleased_noDragging() {
        var widgets = setupWidgets();
        var parent = (Container) widgets.get("parent");
        var child = (Container) widgets.get("child");
        var button = (Button) widgets.get("button");
        doCallRealMethod().when(screen).mouseReleased(anyInt(), anyInt(), anyInt());
        doCallRealMethod().when(screen).mouseClicked(anyInt(), anyInt(), anyInt());
        Point buttonStart = button.layout().boxes().rendered().contentBox().start().add(2, 2);
        Style.Variant beforeClickVariant = button.status();
        screen.mouseClicked(buttonStart.xInt(), buttonStart.yInt(), 0);
        verify(button, times(0)).onClick(any(ClickEvent.class));
        verify(button, times(1)).onMousePressed(any());
        assertTrue(button.pressed(), "Expected button to be pressed");
        assertEquals(Style.Variant.pressed, button.status(), "Expected status to be pressed");
        screen.mouseReleased(buttonStart.xInt(), buttonStart.yInt(), 0);
        verify(button, times(1)).onMouseReleased(assertArg(event -> {
            assert event.isLeftButton() : "Expected left button";
        }));
        verify(button, times(0)).onDragStopped(any());
        verify(button, times(1)).onClick(any(ClickEvent.class));
        assertFalse(button.pressed(), "Expected button to not be pressed");
        assertEquals(Style.Variant.focused, button.status(), "Expected status to be " + beforeClickVariant);
    }

    @Test
    void mouseReleased_withDragging() {
        var widgets = setupWidgets();
        var parent = (Container) widgets.get("parent");
        var child = (Container) widgets.get("child");
        var button = (Button) widgets.get("button");
        doCallRealMethod().when(screen).mouseReleased(anyInt(), anyInt(), anyInt());
        doCallRealMethod().when(screen).mouseClicked(anyInt(), anyInt(), anyInt());
        doCallRealMethod().when(screen).mouseDragged(anyDouble(), anyDouble(), anyInt(), anyDouble(), anyDouble());
        Point clickPoint = parent.layout().boxes().rendered().box().bottomRight().subtract(2, 2);
        screen.mouseClicked(clickPoint.xInt(), clickPoint.yInt(), 0);
        assertEquals(parent.name(), screen.dragManager().current().name(),
                "Expected " + parent.name() + " to be current, instead got "
                        + screen.dragManager().current().name());
        assertFalse(screen.dragManager().dragging(), "Expected not dragging");
        screen.mouseDragged(clickPoint.xInt() + 10, clickPoint.yInt() + 10, 0, 10, 10);
        assertTrue(screen.dragManager().dragging(), "Expected dragging");
        Point buttonEnd = button.layout().boxes().rendered().box().start();
        doAnswer(consumeEvent()).when(parent).onMouseReleased(any());
        screen.mouseReleased(buttonEnd.xInt(), buttonEnd.yInt(), 0);
        assertFalse(screen.dragManager().dragging(), "Expected not dragging");
        verify(parent, times(1)).onDragStopped(any());
        assertNull(screen.dragManager().current(), "Expected current to be null");
    }

    @Test
    void mouseDragged() {
        var widgets = setupWidgets();
        var parent = (Container) widgets.get("parent");
        var child = (Container) widgets.get("child");
        var button = (Button) widgets.get("button");
        doCallRealMethod().when(screen).mouseDragged(anyDouble(), anyDouble(), anyInt(), anyDouble(), anyDouble());
        doCallRealMethod().when(screen).mouseClicked(anyInt(), anyInt(), anyInt());
        Point parentStart = parent.layout().boxes().rendered().box().start();
        Point childStart = child.layout().boxes().rendered().box().start();
        Point buttonStart = button.layout().boxes().rendered().box().start();
        Point clickPoint = parent.layout().boxes().rendered().box().bottomRight().subtract(2, 2);
        screen.mouseClicked(clickPoint.xInt() + 2, clickPoint.yInt() + 2, 0);
        assertEquals(parent.name(), screen.dragManager().current().name(),
                "Expected " + parent.name() + " to be current, instead got "
                        + screen.dragManager().current().name());
        assertFalse(screen.dragManager().dragging(), "Expected not dragging");
        int dragOffset = 10;
        Point dragTo = clickPoint.add(dragOffset, dragOffset);
        screen.mouseDragged(dragTo.x(), dragTo.y(), 0, dragOffset, dragOffset);
        assertTrue(screen.dragManager().dragging(), "Expected dragging");
        verify(parent, times(1)).onDragStarted(any());
        verify(parent, times(1)).onDrag(assertArg(event -> {
            assertTrue(event.isLeftButton(), "Expected left button");
            assertEquals(event.x(), dragTo.x(), "Expected x to be " + dragTo.x() + ", instead got " + event.x());
            assertEquals(event.y(), dragTo.y(), "Expected y to be " + dragTo.y() + ", instead got " + event.y());
        }));
        Point parentEnd = parent.layout().boxes().rendered().box().start();
        Point childEnd = child.layout().boxes().rendered().box().start();
        Point buttonEnd = button.layout().boxes().rendered().box().start();
        assertEquals(parentStart.add(dragOffset, dragOffset), parentEnd,
                "Expected parent to move by " + dragOffset + " in both x and y");
        assertEquals(childStart.add(dragOffset, dragOffset), childEnd,
                "Expected child to move by " + dragOffset + " in both x and y");
        assertEquals(buttonStart.add(dragOffset, dragOffset), buttonEnd,
                "Expected button to move by " + dragOffset + " in both x and y");
    }

    @Test
    void mouseScrolled() {
        doCallRealMethod().when(screen).mouseScrolled(anyDouble(), anyDouble(), anyDouble());
        MouseListener listener = mockListener(MouseListener.class);
        doAnswer(consumeEvent()).when(listener).onScroll(any());
        screen.addListener(listener);
        screen.mouseScrolled(0, 0, 0);
        verify(listener, times(1)).onScroll(any());
    }

    @Test
    void keyReleased() {
        doCallRealMethod().when(screen).keyReleased(anyInt(), anyInt(), anyInt());
        KeyListener listener = mockListener(KeyListener.class);
        doAnswer(consumeEvent()).when(listener).onKey(any());
        screen.addListener(listener);
        screen.keyReleased(0, 0, 0);
        verify(listener, times(1)).onKey(assertArg(event -> {
            assert event.released() : "Expected key to be released";
        }));
    }

    @Test
    void charTyped() {
        doCallRealMethod().when(screen).charTyped(anyChar(), anyInt());
        KeyListener listener = mockListener(KeyListener.class);
        doAnswer(consumeEvent()).when(listener).onCharTyped(any());
        screen.addListener(listener);
        screen.charTyped('a', 0);
        verify(listener, times(1)).onCharTyped(assertArg(event ->
                assertEquals('a', event.codePoint(), "Expected character to be 'a'")));
    }

    @Test
    void mouseMoved() {
        var widgets = setupWidgets();
        doCallRealMethod().when(screen).mouseMoved(anyDouble(), anyDouble());
        Point start = widgets.get("button").layout().boxes().rendered().contentBox().start().add(2, 2);
        try(MockedStatic<ClientUtil> clientUtil = mockStatic(ClientUtil.class)) {
            clientUtil.when(ClientUtil::getMousePosition).thenReturn(start);
            screen.mouseMoved(start.x(), start.y());
        }
        verify(widgets.get("button"), times(1)).onMove(any());
        assertTrue(widgets.get("button").hovered(), "Expected button to be hovered");
    }
}