package ninja.crinkle.mod.client.gui.managers;

import ninja.crinkle.mod.client.gui.editors.LayoutRegistry;
import ninja.crinkle.mod.client.gui.properties.Rect;
import ninja.crinkle.mod.client.gui.widgets.AbstractWidget;

import java.util.Optional;

public interface IManagedGUI {
    GuiManager manager();
    int width();
    int height();
    IManagedGUI gui();

    default boolean layoutEditorEnabled() {
        return true;
    }

    default void registerLayoutEntries() {
        for (AbstractWidget widget : manager().root().children()) {
            LayoutRegistry.register(new LayoutRegistry.Entry(
                    widget.name(),
                    gui(),
                    widget::visualCopy,
                    widget::rect,
                    widget::setRect
            ));
        }
    }

    default void resolveLayoutPositions() {
        int nextX = 0;
        for (LayoutRegistry.Entry entry : LayoutRegistry.entries(gui())) {
            Rect current = entry.currentRect().get();
            int w, h;
            if (current != null && !current.equals(Rect.ZERO)) {
                w = current.width();
                h = current.height();
            } else {
                // Widget hasn't been sized yet — use minimum or fallback
                w = 64;
                h = 64;
            }
            Optional<Rect> saved = LayoutRegistry.resolvePosition(entry.id(), width(), height(), w, h);
            if (saved.isPresent()) {
                entry.onApply().accept(saved.get());
            } else {
                // No saved position — assign a default and persist it
                Rect defaultRect = new Rect(nextX, 0, w, h);
                entry.onApply().accept(defaultRect);
                LayoutRegistry.savePosition(entry.id(), defaultRect, width(), height());
            }
            nextX += w + 4;
        }
    }
}
