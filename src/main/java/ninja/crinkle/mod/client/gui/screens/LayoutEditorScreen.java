package ninja.crinkle.mod.client.gui.screens;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import ninja.crinkle.mod.client.color.Color;
import ninja.crinkle.mod.client.gui.editors.LayoutRegistry;
import ninja.crinkle.mod.client.gui.managers.IManagedGUI;
import ninja.crinkle.mod.client.gui.properties.Rect;
import ninja.crinkle.mod.client.gui.properties.Sizing;
import ninja.crinkle.mod.client.gui.widgets.*;
import ninja.crinkle.mod.util.ClientUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class LayoutEditorScreen extends AbstractScreen implements IManagedGUI {
    private static final int DIM_COLOR = 0xA0000000; // semi-transparent black
    private static final String TOOLBAR_ID = "layout_toolbar";
    private final Map<String, AbstractWidget> editorWidgets = new LinkedHashMap<>();
    private final @Nullable Screen parentScreen;
    private final IManagedGUI source;
    private boolean cursorIsHand;
    private long handCursor;
    private PanelContainer toolbar;

    public LayoutEditorScreen(IManagedGUI source) {
        super(Component.literal("Layout Editor"), ClientUtil.screenWidth(), ClientUtil.screenHeight());
        this.source = source;
        this.parentScreen = Minecraft.getInstance().screen;
    }

    private void cancel() {
        onClose();
    }

    @Override
    public String name() {
        return "LayoutEditorScreen";
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
        // Restore the parent screen instead of closing to null
        if (parentScreen != null) {
            eventManager().clear();
            Minecraft.getInstance().setScreen(parentScreen);
        } else {
            super.onClose();
        }
    }

    @Override
    public void init() {
        editorWidgets.clear();
        new ArrayList<>(root().children()).forEach(root()::remove);

        if (handCursor == 0) {
            handCursor = GLFW.glfwCreateStandardCursor(GLFW.GLFW_HAND_CURSOR);
        }

        for (LayoutRegistry.Entry entry : LayoutRegistry.entries(source)) {
            AbstractWidget widget = entry.editorWidgetFactory().apply(root());
            widget.draggable(true);
            widget.active(true);
            widget.visible(true);
            // Prevent arrange() from expanding this widget to fill the root
            widget.horizontalSizing(Sizing.ShrinkBegin);
            widget.verticalSizing(Sizing.ShrinkBegin);

            root().add(widget);
            editorWidgets.put(entry.id(), widget);
        }

        // Toolbar: PanelContainer > VBoxContainer > children
        toolbar = new PanelContainer.Builder(root())
                .margins(8)
                .name("layout_toolbar")
                .horizontalSizing(Sizing.ShrinkCenter)
                .verticalSizing(Sizing.ShrinkCenter)
                .draggable(true)
                .build();

        VBoxContainer column = new VBoxContainer.Builder(toolbar)
                .separation(4)
                .pushAndReturn();

        new Label.Builder(column)
                .text("Layout Editor")
                .color(Color.PURPLE)
                .name("layout_title")
                .minSize(92, 12)
                .push();

        new Button.Builder(column)
                .style("button_primary")
                .text("Save")
                .name("layout_save")
                .minSize(92, 20)
                .onClick((event, btn) -> save())
                .push();

        new Button.Builder(column)
                .style("button")
                .text("Cancel")
                .name("layout_cancel")
                .minSize(92, 20)
                .onClick((event, btn) -> cancel())
                .push();

        root().add(toolbar);

        super.init();

        // Position toolbar — resolve saved position or default to top-center
        int tw = toolbar.minimumWidth();
        int th = toolbar.minimumHeight();
        Rect defaultToolbar = new Rect(width / 2 - tw / 2, 10, tw, th);
        toolbar.setRect(LayoutRegistry.resolvePosition(TOOLBAR_ID, width, height, tw, th)
                .orElse(defaultToolbar));

        // Position widgets AFTER super.init() so arrange() doesn't overwrite them.
        // Use the live widget's rect as the source of truth for dimensions.
        for (LayoutRegistry.Entry entry : LayoutRegistry.entries(source)) {
            positionWidget(entry);
        }
    }

    @Override
    public int width() {
        return width;
    }

    @Override
    public int height() {
        return height;
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

    private void positionWidget(LayoutRegistry.Entry entry) {
        AbstractWidget widget = editorWidgets.get(entry.id());
        if (widget == null) return;

        Rect liveRect = entry.currentRect().get();
        int w = liveRect != null && !liveRect.equals(Rect.ZERO) ? liveRect.width() : 64;
        int h = liveRect != null && !liveRect.equals(Rect.ZERO) ? liveRect.height() : 64;

        Rect resolved = LayoutRegistry.resolvePosition(entry.id(), width, height, w, h)
                .orElse(liveRect != null && !liveRect.equals(Rect.ZERO)
                        ? new Rect(liveRect.x(), liveRect.y(), w, h)
                        : new Rect(10, 10, w, h));

        widget.setRect(resolved);
    }

    @Override
    public void registerLayoutEntries() {
        // No-op: the editor manages its own widget positioning and must not
        // overwrite the source GUI's entries in the LayoutRegistry.
    }

    @Override
    public void resolveLayoutPositions() {
        // No-op: the editor positions widgets manually in init().
    }

    @Override
    public void render(@NotNull GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        if (parentScreen != null) {
            // Render parent screen as read-only backdrop (mouse at -1,-1 to avoid hover effects)
            parentScreen.render(pGuiGraphics, -1, -1, pPartialTick);
            // Dim overlay so editor widgets stand out
            pGuiGraphics.fill(0, 0, width, height, DIM_COLOR);
        } else {
            renderBackground(pGuiGraphics);
        }
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

    private void save() {
        for (LayoutRegistry.Entry entry : LayoutRegistry.entries(source)) {
            AbstractWidget widget = editorWidgets.get(entry.id());
            if (widget != null) {
                LayoutRegistry.savePosition(entry.id(), widget.rect(), width, height);
                entry.onApply().accept(widget.rect());
            }
        }
        LayoutRegistry.savePosition(TOOLBAR_ID, toolbar.rect(), width, height);
        onClose();
    }
}
