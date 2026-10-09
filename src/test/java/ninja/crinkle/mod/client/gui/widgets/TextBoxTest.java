package ninja.crinkle.mod.client.gui.widgets;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.screens.Screen;
import ninja.crinkle.mod.client.gui.events.CharTypedEvent;
import ninja.crinkle.mod.client.gui.events.ClickEvent;
import ninja.crinkle.mod.client.gui.events.KeyEvent;
import ninja.crinkle.mod.client.gui.managers.GuiManager;
import ninja.crinkle.mod.client.gui.properties.Point;
import ninja.crinkle.mod.client.gui.properties.Rect;
import ninja.crinkle.mod.util.ClientUtil;
import ninja.crinkle.mod.util.TestUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TextBoxTest {
    private final TextBox textBox = spy(new TextBox.Builder(new Container.Builder(GuiManager.create()).build())
            .text("")
            .build());

    @Test
    void handleCopy() {
        when(textBox.focused()).thenReturn(true);
        textBox.text("diaper");
        TextBox.Selection selection = new TextBox.Selection(0, 3);
        textBox.selection(selection);
        try (MockedStatic<ClientUtil> clientUtil = TestUtil.mockClientUtil()) {
            try (MockedStatic<Screen> staticScreen = mockStatic(Screen.class)) {
                staticScreen.when(() -> Screen.isCopy(anyInt())).thenReturn(true);
                KeyEvent event = spy(new KeyEvent(textBox, InputConstants.KEY_C, 46, KeyEvent.Modifier.Control.mask()
                        , false));
                textBox.onKey(event);
                clientUtil.verify(() -> ClientUtil.setClipboard("dia"));
            }
        }
    }

    @Test
    void handleCut() {
        when(textBox.focused()).thenReturn(true);
        textBox.text("diaper");
        TextBox.Selection selection = new TextBox.Selection(0, 3);
        textBox.selection(selection);
        try (MockedStatic<ClientUtil> clientUtil = TestUtil.mockClientUtil()) {
            try (MockedStatic<Screen> staticScreen = mockStatic(Screen.class)) {
                staticScreen.when(() -> Screen.isCut(anyInt())).thenReturn(true);
                KeyEvent event = spy(new KeyEvent(textBox, InputConstants.KEY_X, 45, KeyEvent.Modifier.Control.mask()
                        , false));
                textBox.onKey(event);
                clientUtil.verify(() -> ClientUtil.setClipboard("dia"));
                assertEquals("per", textBox.text());
            }
        }
    }

    @Test
    void handlePaste() {
        when(textBox.focused()).thenReturn(true);
        textBox.text("dia");
        try (MockedStatic<ClientUtil> clientUtil = TestUtil.mockClientUtil()) {
            try (MockedStatic<Screen> staticScreen = mockStatic(Screen.class)) {
                staticScreen.when(() -> Screen.isPaste(anyInt())).thenReturn(true);
                textBox.cursorPos(3);
                when(ClientUtil.getClipboard()).thenReturn("per");
                KeyEvent event = spy(new KeyEvent(textBox, InputConstants.KEY_V, 47, KeyEvent.Modifier.Control.mask()
                        , false));
                textBox.onKey(event);
                clientUtil.verify(ClientUtil::getClipboard);
                assertEquals("diaper", textBox.text());
            }
        }
    }

    @Test
    void onCharTyped_emptyText() {
        when(textBox.focused()).thenReturn(true);
        try (MockedStatic<ClientUtil> ignored = TestUtil.mockClientUtil()) {
            CharTypedEvent event = new CharTypedEvent(textBox, 'a', 0);
            textBox.onCharTyped(event);
            assertEquals("a", textBox.text());
        }
    }

    @Test
    void onCharTyped_insertModeEnd() {
        when(textBox.focused()).thenReturn(true);
        textBox.text("diaper");
        try (MockedStatic<ClientUtil> ignored = TestUtil.mockClientUtil()) {
            textBox.cursorPos(textBox.text().length());
            CharTypedEvent event = new CharTypedEvent(textBox, 's', 0);
            textBox.onCharTyped(event);
            assertEquals("diapers", textBox.text());
            assertEquals(textBox.text().length(), textBox.cursorPos());
        }
    }

    @Test
    void onCharTyped_insertModeMiddle() {
        when(textBox.focused()).thenReturn(true);
        //noinspection SpellCheckingInspection
        textBox.text("diaer");
        try (MockedStatic<ClientUtil> ignored = TestUtil.mockClientUtil()) {
            textBox.cursorPos(3);
            CharTypedEvent event = new CharTypedEvent(textBox, 'p', 0);
            textBox.onCharTyped(event);
            assertEquals("diaper", textBox.text());
        }
    }

    @Test
    void onCharTyped_overwriteModeEnd() {
        when(textBox.focused()).thenReturn(true);
        when(textBox.insertMode()).thenReturn(false);
        textBox.text("diaper");
        try (MockedStatic<ClientUtil> ignored = TestUtil.mockClientUtil()) {
            textBox.cursorPos(textBox.text().length());
            CharTypedEvent event = new CharTypedEvent(textBox, 's', 0);
            textBox.onCharTyped(event);
            assertEquals("diapers", textBox.text());
        }
    }

    @Test
    void onCharTyped_overwriteModeMiddle() {
        when(textBox.focused()).thenReturn(true);
        when(textBox.insertMode()).thenReturn(false);
        //noinspection SpellCheckingInspection
        textBox.text("dlaper");
        try (MockedStatic<ClientUtil> ignored = TestUtil.mockClientUtil()) {
            textBox.cursorPos(1);
            CharTypedEvent event = new CharTypedEvent(textBox, 'i', 0);
            textBox.onCharTyped(event);
            assertEquals("diaper", textBox.text());
        }
    }

    @Test
    void onClick_Middle() {
        when(textBox.active()).thenReturn(true);
        try (MockedStatic<ClientUtil> ignored = TestUtil.mockClientUtil()) {
            textBox.text("diaper");
            int xPos = ClientUtil.getMinecraft().font.width("dia");
            ClickEvent event = new ClickEvent(textBox, xPos, 0, 0, true, List.of(textBox));
            textBox.onClick(event);
            assertEquals(3, textBox.cursorPos());
        }
    }

    @Test
    void onClick_leftEnd() {
        when(textBox.active()).thenReturn(true);
        try (MockedStatic<ClientUtil> ignored = TestUtil.mockClientUtil()) {
            Point mouse = Point.of(0, 0);
            ClickEvent event = new ClickEvent(textBox, mouse.xInt(), mouse.yInt(), 0, true, List.of(textBox));
            textBox.onClick(event);
            assertEquals(0, textBox.cursorPos());
        }
    }

    @Test
    void onClick_rightEnd() {
        when(textBox.active()).thenReturn(true);
        try (MockedStatic<ClientUtil> ignored = TestUtil.mockClientUtil()) {
            textBox.text("diaper");
            int xPos = ClientUtil.getMinecraft().font.width("diaper");
            ClickEvent event = new ClickEvent(textBox, xPos, 0, 0, true, List.of(textBox));
            textBox.onClick(event);
            assertEquals(textBox.text().length(), textBox.cursorPos());
        }
    }

    @Test
    void onClick_selectRange() {
        when(textBox.active()).thenReturn(true);
        when(textBox.shiftDown()).thenReturn(true);
        try (MockedStatic<ClientUtil> ignored = TestUtil.mockClientUtil()) {
            textBox.text("diaper");
            int xPos = ClientUtil.getMinecraft().font.width("dia");
            textBox.cursorPos(0);
            ClickEvent event = new ClickEvent(textBox, xPos, 0, 0, true, List.of(textBox));
            textBox.onClick(event);
            assertEquals(3, textBox.cursorPos());
            assertEquals(3, textBox.selection().end());
        }
    }

    @Test
    void onKey_backspace() {
        when(textBox.focused()).thenReturn(true);
        textBox.text("diaper");
        try (MockedStatic<ClientUtil> ignored = TestUtil.mockClientUtil()) {
            textBox.cursorPos(6);
            KeyEvent event = new KeyEvent(textBox, InputConstants.KEY_BACKSPACE, 259, 0, false);
            textBox.onKey(event);
            assertEquals("diape", textBox.text());
            assertEquals(5, textBox.cursorPos());
        }
    }

    @Test
    void onKey_backspaceSelection() {
        when(textBox.focused()).thenReturn(true);
        when(textBox.shiftDown()).thenReturn(true);
        textBox.text("diaper");
        try (MockedStatic<ClientUtil> ignored = TestUtil.mockClientUtil()) {
            textBox.cursorPos(6);
            TextBox.Selection selection = new TextBox.Selection(3, 6);
            textBox.selection(selection);
            KeyEvent event = new KeyEvent(textBox, InputConstants.KEY_BACKSPACE, 259, 0, false);
            textBox.onKey(event);
            assertEquals("dia", textBox.text());
            assertEquals(3, textBox.cursorPos());
        }
    }

    @Test
    void onKey_delete() {
        when(textBox.focused()).thenReturn(true);
        textBox.text("diaper");
        try (MockedStatic<ClientUtil> ignored = TestUtil.mockClientUtil()) {
            textBox.cursorPos(0);
            KeyEvent event = new KeyEvent(textBox, InputConstants.KEY_DELETE, 261, 0, false);
            textBox.onKey(event);
            assertEquals("iaper", textBox.text());
            assertEquals(0, textBox.cursorPos());
        }
    }

    @Test
    void onKey_deleteSelection() {
        when(textBox.focused()).thenReturn(true);
        when(textBox.shiftDown()).thenReturn(true);
        textBox.text("diaper");
        try (MockedStatic<ClientUtil> ignored = TestUtil.mockClientUtil()) {
            textBox.cursorPos(0);
            TextBox.Selection selection = new TextBox.Selection(0, 3);
            textBox.selection(selection);
            KeyEvent event = new KeyEvent(textBox, InputConstants.KEY_DELETE, 261, 0, false);
            textBox.onKey(event);
            assertEquals("per", textBox.text());
            assertEquals(0, textBox.cursorPos());
        }
    }

    @Test
    void onKey_end() {
        when(textBox.focused()).thenReturn(true);
        textBox.text("diaper");
        try (MockedStatic<ClientUtil> ignored = TestUtil.mockClientUtil()) {
            textBox.cursorPos(3);
            KeyEvent event = new KeyEvent(textBox, InputConstants.KEY_END, 269, 0, false);
            textBox.onKey(event);
            assertEquals(6, textBox.cursorPos());
        }
    }

    @Test
    void onKey_home() {
        when(textBox.focused()).thenReturn(true);
        textBox.text("diaper");
        try (MockedStatic<ClientUtil> ignored = TestUtil.mockClientUtil()) {
            textBox.cursorPos(3);
            KeyEvent event = new KeyEvent(textBox, InputConstants.KEY_HOME, 268, 0, false);
            textBox.onKey(event);
            assertEquals(0, textBox.cursorPos());
        }
    }

    @Test
    void onKey_leftMiddle() {
        when(textBox.focused()).thenReturn(true);
        textBox.text("diaper");
        try (MockedStatic<ClientUtil> ignored = TestUtil.mockClientUtil()) {
            textBox.cursorPos(3);
            KeyEvent event = new KeyEvent(textBox, InputConstants.KEY_LEFT, 263, 0, false);
            textBox.onKey(event);
            assertEquals(2, textBox.cursorPos());
        }
    }

    @Test
    void onKey_leftStart() {
        when(textBox.focused()).thenReturn(true);
        textBox.text("diaper");
        try (MockedStatic<ClientUtil> ignored = TestUtil.mockClientUtil()) {
            textBox.cursorPos(0);
            KeyEvent event = new KeyEvent(textBox, InputConstants.KEY_LEFT, 263, 0, false);
            textBox.onKey(event);
            assertEquals(0, textBox.cursorPos());
        }
    }

    @Test
    void onKey_rightEnd() {
        when(textBox.focused()).thenReturn(true);
        textBox.text("diaper");
        try (MockedStatic<ClientUtil> ignored = TestUtil.mockClientUtil()) {
            textBox.cursorPos(textBox.text().length());
            KeyEvent event = new KeyEvent(textBox, InputConstants.KEY_RIGHT, 262, 0, false);
            textBox.onKey(event);
            assertEquals(textBox.text().length(), textBox.cursorPos());
        }
    }

    @Test
    void onKey_rightMiddle() {
        when(textBox.focused()).thenReturn(true);
        textBox.text("diaper");
        try (MockedStatic<ClientUtil> ignored = TestUtil.mockClientUtil()) {
            textBox.cursorPos(3);
            KeyEvent event = new KeyEvent(textBox, InputConstants.KEY_RIGHT, 262, 0, false);
            textBox.onKey(event);
            assertEquals(4, textBox.cursorPos());
        }
    }

    @Test
    void onKey_shiftDown() {
        when(textBox.focused()).thenReturn(true);
        try (MockedStatic<ClientUtil> ignored = TestUtil.mockClientUtil()) {
            KeyEvent event = new KeyEvent(textBox, InputConstants.KEY_LSHIFT, 340, KeyEvent.Modifier.Shift.mask(),
                    false);
            textBox.onKey(event);
            assertTrue(textBox.shiftDown());
        }
    }

    @Test
    void onKey_shiftUp() {
        when(textBox.focused()).thenReturn(true);
        textBox.shiftDown(true);
        try (MockedStatic<ClientUtil> ignored = TestUtil.mockClientUtil()) {
            KeyEvent event = new KeyEvent(textBox, InputConstants.KEY_LSHIFT, 340, 0, true);
            textBox.onKey(event);
            assertFalse(textBox.shiftDown());
        }
    }

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
        reset(textBox);
    }

    @Test
    void testVisibleText_empty() {
        when(textBox.focused()).thenReturn(true);
        textBox.text("");
        try (MockedStatic<ClientUtil> ignored = TestUtil.mockClientUtil()) {
            assertEquals("", textBox.visibleText());
        }
    }

    @Test
    void testVisibleText_endOfText() {
        when(textBox.focused()).thenReturn(true);
        String text = "diaper";
        String expected = "per";
        int width = expected.length() * TestUtil.DEFAULT_FONT_WIDTH;
        try (MockedStatic<ClientUtil> ignored = TestUtil.mockClientUtil()) {
            textBox.setRect(new Rect(0, 0, width, 10));
            textBox.text(text);
            textBox.cursorPos(text.length());
            assertEquals(3, textBox.visibleStart());
            assertEquals(text, textBox.text());
            assertEquals(expected, textBox.visibleText());
        }
    }

    @Test
    void testVisibleText_middleOfText() {
        when(textBox.focused()).thenReturn(true);
        String text = "diaper";
        String expected = "ape";
        int width = expected.length() * TestUtil.DEFAULT_FONT_WIDTH;
        try (MockedStatic<ClientUtil> ignored = TestUtil.mockClientUtil()) {
            textBox.setRect(new Rect(0, 0, width, 10));
            textBox.text(text);
            textBox.cursorPos(5);
            assertEquals(2, textBox.visibleStart());
            assertEquals(text, textBox.text());
            assertEquals(expected, textBox.visibleText());
        }
    }

    @Test
    void testVisibleText_short() {
        when(textBox.focused()).thenReturn(true);
        textBox.setRect(new Rect(0, 0, 100, 10));
        textBox.text("short");
        try (MockedStatic<ClientUtil> ignored = TestUtil.mockClientUtil()) {
            assertEquals("short", textBox.visibleText());
        }
    }

    @Test
    void testVisibleText_startOfText() {
        when(textBox.focused()).thenReturn(true);
        String text = "diaper";
        String expected = "dia";
        int width = expected.length() * TestUtil.DEFAULT_FONT_WIDTH;
        try (MockedStatic<ClientUtil> ignored = TestUtil.mockClientUtil()) {
            textBox.setRect(new Rect(0, 0, width, 10));
            textBox.text(text);
            textBox.cursorPos(0);
            assertEquals(0, textBox.visibleStart());
            assertEquals(text, textBox.text());
            assertEquals(expected, textBox.visibleText());
        }
    }
}