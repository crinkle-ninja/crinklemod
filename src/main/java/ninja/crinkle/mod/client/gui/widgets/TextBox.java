package ninja.crinkle.mod.client.gui.widgets;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.logging.LogUtils;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import ninja.crinkle.mod.client.color.Color;
import ninja.crinkle.mod.client.gui.actors.Modifier;
import ninja.crinkle.mod.client.gui.actors.MouseButton;
import ninja.crinkle.mod.client.gui.actors.KeyActor;
import ninja.crinkle.mod.client.gui.actors.MouseActor;
import ninja.crinkle.mod.client.gui.properties.Point;
import ninja.crinkle.mod.client.gui.properties.Rect;
import ninja.crinkle.mod.client.gui.renderers.ThemeGraphics;
import ninja.crinkle.mod.client.gui.textures.Texture;
import ninja.crinkle.mod.client.gui.textures.TextureSize;
import ninja.crinkle.mod.util.ClientUtil;
import org.slf4j.Logger;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;


public class TextBox extends AbstractWidget implements KeyActor, MouseActor {
    private static final Logger LOGGER = LogUtils.getLogger();
    private int cursorPos = -1;
    private boolean insertMode = true;
    private Component placeholder;
    private boolean readOnly;
    private Selection selection;
    private boolean shadow;
    private boolean shiftDown;
    private String text;
    private String value;
    private int visibleStart = 0;
    private final Consumer<String> onTextChanged;

    protected TextBox(Builder builder) {
        super(builder);
        this.text = builder.text();
        this.placeholder = builder.placeholder();
        this.shadow = builder.shadow();
        this.readOnly = builder.readOnly();
        this.onTextChanged = builder.onTextChanged();
        if (style() == null) {
            style("textbox");
        }
    }

    public void cursorPos(int cursorPos) {
        if (cursorPos == -1) {
            this.cursorPos = -1;
            visibleStart(0);
            return;
        }

        cursorPos = Math.max(0, Math.min(cursorPos, text().length()));
        this.cursorPos = cursorPos;

        int visibleWidth = rect().width();
        Font font = appearance().font();

        if (cursorPos == text().length()) {
            int textPixelWidth = font.width(text());
            if (textPixelWidth > visibleWidth) {
                int newStart = 0;
                while (font.width(text().substring(newStart)) > visibleWidth) {
                    newStart++;
                }
                visibleStart(newStart);
            } else {
                visibleStart(0);
            }
            return;
        }

        int newStart = Math.min(visibleStart(), cursorPos);
        int cursorPixelPos = font.width(text().substring(newStart, cursorPos));

        if (cursorPixelPos < 0) {
            newStart = cursorPos;
        } else if (cursorPixelPos > visibleWidth) {
            while (font.width(text().substring(newStart, cursorPos)) > visibleWidth) {
                newStart++;
            }
        }

        visibleStart(newStart);
    }

    private int cursorPosFromPoint(Point point) {
        int cursorPos = 0;
        double charX = rect().x();
        for (char c : text().toCharArray()) {
            charX += appearance().font().width(String.valueOf(c));
            if (charX > point.x()) {
                break;
            }
            cursorPos++;
        }
        return cursorPos;
    }

    private boolean textChanged() {
        if (value() != null && value().equals(text())) return false;
        value(text());
        onTextChanged.accept(value());
        return true;
    }

    public String value() {
        return this.value;
    }

    private boolean handleReturn() {
        return textChanged();
    }

    @Override
    public void focused(boolean focused) {
        super.focused(focused);
        if (focused) {
            value(text());
        } else {
            textChanged();
        }
    }

    private boolean handleBackspace() {
        int index = cursorPos();
        if (selection() != null) {
            text(text().substring(0, selection().min()) + text().substring(selection().max()));
            cursorPos(selection().min());
            selection(null);
            return true;
        } else if (index > 0) {
            text(text().substring(0, index - 1) + text().substring(index));
            cursorPos(index - 1);
            return true;
        }
        return false;
    }

    private boolean handleCopy(int keyCode) {
        if (Screen.isCopy(keyCode) && selection() != null) {
            ClientUtil.setClipboard(selection().text(text()));
            return true;
        }
        return false;
    }

    private boolean handleCut(int keyCode) {
        if (Screen.isCut(keyCode) && selection() != null) {
            ClientUtil.setClipboard(selection().text(text()));
            text(text().substring(0, selection().min()) + text().substring(selection().max()));
            cursorPos(selection().min());
            selection(null);
            return true;
        }
        return false;
    }

    private boolean handleDelete() {
        int index = cursorPos();
        if (selection() != null) {
            text(text().substring(0, selection().min()) + text().substring(selection().max()));
            cursorPos(selection().min());
            selection(null);
            return true;
        } else if (index < text().length()) {
            text(text().substring(0, index) + text().substring(index + 1));
            return true;
        }
        return false;
    }

    private boolean handleEnd(int modifiers) {
        int newPos = text().length();
        if (selection() != null) {
            newPos = selection().max();
            selection(null);
        } else if (isShiftDown(modifiers)) {
            if (cursorPos() < text().length()) {
                selection(new Selection(cursorPos(), text().length()));
            }
        }
        cursorPos(newPos);
        return true;
    }

    private boolean handleEscape() {
        if (!focused()) return false;
        if (selection() != null) {
            selection(null);
        }
        return true;
    }

    private boolean handleHome() {
        int newPos = 0;
        if (selection() != null) {
            newPos = selection().min();
            selection(null);
        } else if (shiftDown()) {
            if (cursorPos() == 0) {
                selection(new Selection(0, 0));
            } else {
                selection(new Selection(cursorPos(), 0));
            }
        }
        cursorPos(newPos);
        return true;
    }

    private void handleInsert(int index, char c) {
        if (selection() != null && !selection().isEmpty()) {
            text(text().substring(0, selection().min()) + c + text().substring(selection().max()));
            cursorPos(selection().min() + 1);
            selection(null);
            return;
        } else if (index == text().length() || index == -1) {
            text(text() + c);
        } else {
            text(text().substring(0, index) + c + text().substring(index));
        }
        cursorPos(index + 1);
    }

    private boolean handleLeft() {
        if (selection() != null && !shiftDown()) {
            cursorPos(selection().min());
            selection(null);
            return true;
        } else if (shiftDown()) {
            if (selection() == null) {
                selection(new Selection(cursorPos(), cursorPos()));
            }
            cursorPos(Math.max(0, cursorPos() - 1));
            selection(new Selection(cursorPos(), selection().max()));
            return true;
        } else if (cursorPos() > 0) {
            cursorPos(Math.max(0, cursorPos() - 1));
            return true;
        }
        return false;
    }

    private void handleOverwrite(int index, char c) {
        if (selection() != null && !selection().isEmpty()) {
            text(text().substring(0, selection().min()) + c + text().substring(selection().max()));
            cursorPos(selection().min() + 1);
            selection(null);
            return;
        } else if (index == text().length()) {
            text(text() + c);
        } else {
            text(text().substring(0, index) + c + text().substring(index + 1));
        }
        cursorPos(index + 1);
    }

    private boolean handlePaste(int keyCode) {
        if (!Screen.isPaste(keyCode)) {
            return false;
        }
        String clipboard = Optional.of(ClientUtil.getClipboard()).orElse("");
        if (selection() != null) {
            text(text().substring(0, selection().min()) + clipboard + text().substring(selection().max()));
            cursorPos(selection().min() + clipboard.length());
            selection(null);
        } else {
            text(text().substring(0, cursorPos()) + clipboard + text().substring(cursorPos()));
            cursorPos(cursorPos() + clipboard.length());
        }
        return true;
    }

    private boolean handleRight() {
        if (selection() != null && !shiftDown()) {
            cursorPos(selection().max());
            selection(null);
            return true;
        } else if (shiftDown()) {
            if (selection() == null) {
                selection(new Selection(cursorPos(), cursorPos()));
            }
            cursorPos(Math.min(text().length(), cursorPos() + 1));
            selection(new Selection(selection().min(), cursorPos()));
            return true;
        } else if (cursorPos() < text().length()) {
            cursorPos(Math.min(text().length(), cursorPos() + 1));
            return true;
        }
        return false;
    }

    public void insertMode(boolean insertMode) {
        this.insertMode = insertMode;
    }

    @Override
    public boolean onCharTyped(char pCodePoint, int pModifiers) {
        if (!focused() || !active() || readOnly()) return false;
        if (insertMode()) {
            handleInsert(cursorPos(), pCodePoint);
        } else {
            handleOverwrite(cursorPos(), pCodePoint);
        }
        return true;
    }

    @Override
    public boolean onClick(Point point, MouseButton mouseButton) {
        if (!visible() || !active()) {
            return false;
        }
        if (mouseButton == MouseButton.LEFT) {
            int start = cursorPos();
            int end = cursorPosFromPoint(point);
            if (selection() != null && selection.contains(end)) {
                return true;
            }
            cursorPos(end);
            if (shiftDown()) {
                selection(new Selection(start, end));
            } else {
                selection(null);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean onDoubleClick(Point point, MouseButton button) {
        if (!active() || !rect().contains(point)) {
            return false;
        }
        if (selection() != null && !selection().isEmpty()
                && selection().contains(cursorPosFromPoint(point))) {
            selection(new Selection(0, text().length()));
        } else {
            int start = cursorPos();
            int end = cursorPos();
            while (start > 0 && !Character.isWhitespace(text().charAt(start - 1))) {
                start--;
            }
            while (end < text().length() && end >= 0 && !Character.isWhitespace(text().charAt(end))) {
                end++;
            }
            cursorPos(end);
            if (start <= 0 || end <= 0) {
                return true;
            }
            selection(new Selection(start, end));
            return true;
        }
        return false;
    }

    private boolean isShiftDown(int modifiers) {
        return Modifier.Shift.mask() == (modifiers & Modifier.Shift.mask());
    }

    @Override
    public boolean onKeyPressed(int keyCode, int scanCode, int modifiers) {
        if (!focused() || !active() || readOnly()) {
            return false;
        }

        shiftDown(isShiftDown(modifiers));

        return switch (keyCode) {
            case InputConstants.KEY_ESCAPE -> handleEscape();
            case InputConstants.KEY_LEFT -> handleLeft();
            case InputConstants.KEY_RIGHT -> handleRight();
            case InputConstants.KEY_INSERT -> {
                insertMode(!insertMode());
                yield true;
            }
            case InputConstants.KEY_RETURN -> handleReturn();
            case InputConstants.KEY_BACKSPACE -> handleBackspace();
            case InputConstants.KEY_DELETE -> handleDelete();
            case InputConstants.KEY_HOME -> handleHome();
            case InputConstants.KEY_END -> handleEnd(modifiers);
            case InputConstants.KEY_C -> handleCopy(keyCode);
            case InputConstants.KEY_X -> handleCut(keyCode);
            case InputConstants.KEY_V -> handlePaste(keyCode);
            case InputConstants.KEY_A -> {
                if (Screen.isSelectAll(keyCode)) {
                    selection(new Selection(0, text().length()));
                    cursorPos(text().length());
                    yield true;
                }
                yield false;
            }
            default -> false;
        };
    }

    @SuppressWarnings("unused")
    public void placeholder(String placeholder) {
        placeholder(Component.literal(placeholder));
    }

    public void placeholder(Component placeholder) {
        this.placeholder = placeholder;
    }

    @SuppressWarnings("unused")
    public void readOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    @Override
    public void renderContent(ThemeGraphics graphics, Point pMouse, Rect renderedRect, float pPartialTick) {
        TextureSize background = appearance().backgroundTexture().boundsOf(Texture.Slice.Location.topLeft);
        int offset = (renderedRect.height() / 2) - (graphics.textHeight() / 2);
        Point renderPos = Point.of(renderedRect.x() + background.width(), renderedRect.y() + offset);
        if (text().isEmpty() && !focused()) {
            graphics.text(placeholder().getString(), renderPos, zIndex(), appearance().foregroundColor().halftone(),
                    appearance().shadow());
        } else {
            graphics.text(visibleText(), renderPos, zIndex(), appearance().foregroundColor(), appearance().shadow());
            if (selection() != null) {
                int start = Math.max(0, selection().min() - visibleStart());
                int end = Math.min(Math.max(0, selection().max() - visibleStart()), visibleText().length());
                int xStart = renderPos.xInt() + graphics.textWidth(visibleText().substring(0, start));
                int xEnd = renderPos.xInt() + graphics.textWidth(visibleText().substring(0, end));
                graphics.fill(xStart, renderPos.yInt(), xEnd, renderPos.yInt() + graphics.textHeight(), zIndex(),
                        appearance().foregroundColor().color());
                graphics.text(selection().text(visibleText()), Point.of(xStart, renderPos.yInt()), zIndex(),
                        Optional.ofNullable(appearance().backgroundColor()).orElse(Color.RAINBOW),
                        appearance().shadow());
            }
            renderCursor(graphics, renderPos);
        }
    }

    public String text() {
        return text;
    }

    public Component placeholder() {
        return placeholder;
    }

    public void value(String value) {
        this.value = value;
    }

    protected String visibleText() {
        int width = rect().width();
        Font font = appearance().font();
        String visibleText = text().substring(visibleStart());

        for (int i = 1; i <= visibleText.length(); i++) {
            if (font.width(visibleText.substring(0, i)) > width) {
                return visibleText.substring(0, i - 1);
            }
        }
        return visibleText;
    }

    public Selection selection() {
        return selection;
    }

    public int visibleStart() {
        return visibleStart;
    }

    private void renderCursor(ThemeGraphics graphics, Point renderPos) {
        if (!focused() || !active() || readOnly() || cursorPos() == -1) {
            return;
        }
        if (ClientUtil.getMinecraft().gui.getGuiTicks() % 20 < 10) {
            return;
        }

        int cursorX = renderPos.xInt() + graphics.textWidth(visibleText());
        String character = "_";
        int cursorXEnd = insertMode() ? cursorX + 1 : cursorX + graphics.textWidth(character);
        int cursorY = renderPos.yInt();
        if (cursorPos() < text().length() && cursorPos() >= 0) {
            cursorX = renderPos.xInt() + graphics.textWidth(visibleText().substring(0, visibleCursor())) - 1;
            character = text().substring(cursorPos(), cursorPos() + 1);
            cursorXEnd = insertMode() ? cursorX + 1 : cursorX + graphics.textWidth(character);
        }

        if (insertMode()) {
            Color foregroundColor = Optional.ofNullable(appearance().foregroundColor()).orElse(Color.RAINBOW);
            if (selection() != null && selection().contains(cursorPos())) {
                foregroundColor = Optional.ofNullable(appearance().backgroundColor()).orElse(Color.RAINBOW);
            }
            if (cursorPos() == text().length()) {
                graphics.text(character, Point.of(cursorX, cursorY), zIndex(), foregroundColor,
                        appearance().shadow());
            } else {
                graphics.fill(cursorX, cursorY, cursorXEnd, cursorY + graphics.textHeight(), zIndex(),
                        foregroundColor.color());
            }
        } else {
            Color cursorColor = Optional.ofNullable(appearance().foregroundColor()).orElse(Color.RAINBOW);
            if (selection() != null && selection().contains(cursorPos())) {
                cursorColor = Optional.ofNullable(appearance().backgroundColor()).orElse(Color.RAINBOW);
            }
            if (cursorPos() < text().length()) {
                cursorX += 1;
            } else {
                cursorXEnd -= 1;
            }
            graphics.fill(cursorX, cursorY, cursorXEnd, cursorY + graphics.textHeight(), zIndex(),
                    cursorColor.inverted().color());
            graphics.text(character, Point.of(cursorX, cursorY), zIndex(),
                    Objects.requireNonNull(appearance().backgroundColor()), appearance().shadow());
        }
    }

    public boolean readOnly() {
        return readOnly;
    }

    public int cursorPos() {
        return cursorPos;
    }

    public boolean insertMode() {
        return insertMode;
    }

    protected int visibleCursor() {
        return cursorPos() - visibleStart();
    }

    public void selection(Selection selection) {
        this.selection = selection;
    }

    @SuppressWarnings("unused")
    public boolean shadow() {
        return shadow;
    }

    @SuppressWarnings("unused")
    public void shadow(boolean shadow) {
        this.shadow = shadow;
    }

    public void shiftDown(boolean shiftDown) {
        this.shiftDown = shiftDown;
    }

    public boolean shiftDown() {
        return shiftDown;
    }

    public void text(String value) {
        this.text = value;
    }

    public void visibleStart(int visibleStart) {
        this.visibleStart = visibleStart;
    }

    @SuppressWarnings("unused")
    public static class Builder extends AbstractWidget.AbstractBuilder<TextBox.Builder> {
        private Component placeholder = Component.empty();
        private boolean readOnly = false;
        private boolean shadow = false;
        private String text = "";
        private Consumer<String> onTextChanged;
        private Supplier<?> getter;

        public Builder getter(Supplier<?> getter) {
            this.getter = getter;
            return self();
        }

        public Supplier<?> getter() {
            return getter;
        }

        public Consumer<String> onTextChanged() {
            return onTextChanged;
        }

        public Builder onTextChanged(Consumer<String> onTextChanged) {
            this.onTextChanged = onTextChanged;
            return self();
        }


        public Builder(AbstractContainer parent) {
            super(parent);
            active(true);
            focusable(true);
        }

        @Override
        public TextBox build() {
            return new TextBox(this);
        }

        @Override
        public AbstractContainer push() {
            return parent().add(this);
        }

        public Builder placeholder(String placeholder) {
            return placeholder(Component.literal(placeholder));
        }

        public Builder placeholder(Component placeholder) {
            this.placeholder = placeholder;
            return self();
        }

        @Override
        protected Builder self() {
            return this;
        }

        public Component placeholder() {
            return placeholder;
        }

        @SuppressWarnings("unused")
        public TextBox pushAndReturn() {
            TextBox textBox = new TextBox(this);
            parent().add(textBox);
            return textBox;
        }

        public Builder readOnly(boolean readOnly) {
            this.readOnly = readOnly;
            return self();
        }

        public boolean readOnly() {
            return readOnly;
        }

        public Builder shadow(boolean shadow) {
            this.shadow = shadow;
            return self();
        }

        public boolean shadow() {
            return shadow;
        }

        public Builder text(String text) {
            this.text = text;
            return self();
        }

        public String text() {
            return text;
        }
    }

    public record Selection(int start, int end) {
        public Selection {
            if (start < 0 || end < 0) {
                throw new IllegalArgumentException("Selection start and end must be positive: start=" + start + ", " +
                        "end=" + end);
            }
        }

        public boolean contains(int index) {
            return index >= min() && index < max();
        }

        public int min() {
            return Math.min(start, end);
        }

        public int max() {
            return Math.max(start, end);
        }

        @SuppressWarnings("unused")
        public int length() {
            return Math.abs(end - start);
        }

        public String text(String text) {
            int start = Math.min(this.start, this.end);
            int end = Math.min(Math.max(this.start, this.end), text.length());
            if (isEmpty()) {
                return "";
            } else if (start < end) {
                return text.substring(start, end);
            } else {
                return text.substring(end, start);
            }
        }

        public boolean isEmpty() {
            return start == end;
        }
    }
}
