package ninja.crinkle.mod.client.gui.screens;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import ninja.crinkle.mod.client.color.Color;
import ninja.crinkle.mod.client.gui.editors.LayoutRegistry;
import ninja.crinkle.mod.client.gui.layouts.SizeFlags;
import ninja.crinkle.mod.client.gui.properties.Rect;
import ninja.crinkle.mod.client.gui.widgets.*;
import ninja.crinkle.mod.util.ClientUtil;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.util.LinkedHashMap;
import java.util.Map;

public class LayoutEditorScreen extends AbstractScreen {
    private final Map<String, AbstractWidget> editorWidgets = new LinkedHashMap<>();
    private long handCursor;
    private boolean cursorIsHand;

    public LayoutEditorScreen() {
        super(Component.literal("Layout Editor"), ClientUtil.screenWidth(), ClientUtil.screenHeight());
    }

    @Override
    public String name() {
        return "LayoutEditorScreen";
    }

    @Override
    public void init() {
        editorWidgets.clear();

        if (handCursor == 0) {
            handCursor = GLFW.glfwCreateStandardCursor(GLFW.GLFW_HAND_CURSOR);
        }

        for (LayoutRegistry.Entry entry : LayoutRegistry.entries()) {
            AbstractWidget widget = entry.editorWidgetFactory().get();
            widget.draggable(true);
            widget.active(true);
            widget.visible(true);
            // Prevent arrange() from expanding this widget to fill the root
            widget.hSizeFlags(SizeFlags.ShrinkBegin);
            widget.vSizeFlags(SizeFlags.ShrinkBegin);

            root().add(widget);
            editorWidgets.put(entry.id(), widget);
        }

        // Toolbar: MarginContainer(panel) > VBoxContainer > children
        MarginContainer toolbar = MarginContainer.builder(root())
                .name("layout_toolbar")
                .hSizeFlags(SizeFlags.ShrinkCenter)
                .vSizeFlags(SizeFlags.ShrinkCenter)
                .draggable(true)
                .widgetTheme("panel")
                .margins(8)
                .build();

        VBoxContainer column = VBoxContainer.builder(toolbar)
                .separation(4)
                .pushAndReturn();

        Label.builder(column)
                .text("Layout Editor")
                .color(Color.PURPLE)
                .name("layout_title")
                .minSize(92, 12)
                .push();

        Button.builder(column)
                .widgetTheme("button_primary")
                .text("Save")
                .name("layout_save")
                .minSize(92, 20)
                .onClick((event, btn) -> save())
                .push();

        Button.builder(column)
                .widgetTheme("button")
                .text("Cancel")
                .name("layout_cancel")
                .minSize(92, 20)
                .onClick((event, btn) -> cancel())
                .push();

        root().add(toolbar);

        super.init();

        // Position widgets AFTER super.init() so arrange() doesn't overwrite them.
        // Use the live widget's rect as the source of truth for dimensions.
        for (LayoutRegistry.Entry entry : LayoutRegistry.entries()) {
            AbstractWidget widget = editorWidgets.get(entry.id());
            if (widget == null) continue;

            Rect liveRect = entry.currentRect().get();
            int w = liveRect != null && !liveRect.equals(Rect.ZERO) ? liveRect.width() : 64;
            int h = liveRect != null && !liveRect.equals(Rect.ZERO) ? liveRect.height() : 64;

            Rect resolved = LayoutRegistry.resolvePosition(entry.id(), width, height, w, h)
                    .orElse(liveRect != null && !liveRect.equals(Rect.ZERO)
                            ? new Rect(liveRect.x(), liveRect.y(), w, h)
                            : new Rect(10, 10, w, h));

            widget.setRect(resolved);
        }

        // Position toolbar at top-center
        int tw = toolbar.getMinimumWidth();
        int th = toolbar.getMinimumHeight();
        toolbar.setRect(new Rect(width / 2 - tw / 2, 10, tw, th));
    }

    @Override
    public void mouseMoved(double pMouseX, double pMouseY) {
        super.mouseMoved(pMouseX, pMouseY);
        boolean overDraggable = editorWidgets.values().stream()
                .anyMatch(w -> w.rect().contains(pMouseX, pMouseY));
        if (overDraggable && !cursorIsHand) {
            GLFW.glfwSetCursor(windowHandle(), handCursor);
            cursorIsHand = true;
        } else if (!overDraggable && cursorIsHand) {
            GLFW.glfwSetCursor(windowHandle(), 0);
            cursorIsHand = false;
        }
    }

    private long windowHandle() {
        return Minecraft.getInstance().getWindow().getWindow();
    }

    private void save() {
        for (LayoutRegistry.Entry entry : LayoutRegistry.entries()) {
            AbstractWidget widget = editorWidgets.get(entry.id());
            if (widget != null) {
                LayoutRegistry.savePosition(entry.id(), widget.rect(), width, height);
                entry.onApply().accept(widget.rect());
            }
        }
        onClose();
    }

    private void cancel() {
        onClose();
    }

    @Override
    public void onClose() {
        // Restore default cursor
        GLFW.glfwSetCursor(windowHandle(), 0);
        cursorIsHand = false;
        if (handCursor != 0) {
            GLFW.glfwDestroyCursor(handCursor);
            handCursor = 0;
        }
        super.onClose();
    }

    @Override
    public void render(@NotNull GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        renderBackground(pGuiGraphics);
        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        // Draw outlines around draggable widgets
        for (AbstractWidget widget : editorWidgets.values()) {
            Rect r = widget.rect();
            if (r.equals(Rect.ZERO)) continue;
            int c = widget.rect().contains(pMouseX, pMouseY)
                    ? Color.WHITE.get().color()
                    : Color.LIGHT_GRAY.get().color();
            // Top
            pGuiGraphics.fill(r.x(), r.y(), r.right(), r.y() + 1, c);
            // Bottom
            pGuiGraphics.fill(r.x(), r.bottom() - 1, r.right(), r.bottom(), c);
            // Left
            pGuiGraphics.fill(r.x(), r.y(), r.x() + 1, r.bottom(), c);
            // Right
            pGuiGraphics.fill(r.right() - 1, r.y(), r.right(), r.bottom(), c);
        }
    }
}
