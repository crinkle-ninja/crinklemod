package ninja.crinkle.mod.client.gui.overlays;

import java.util.Optional;
import ninja.crinkle.mod.client.gui.editors.LayoutRegistry;
import ninja.crinkle.mod.client.gui.managers.GuiManager;
import ninja.crinkle.mod.client.gui.properties.Rect;
import ninja.crinkle.mod.client.gui.widgets.MetabolismWidget;

public class MetabolismOverlay extends AbstractOverlay {
    public static final String WIDGET_ID = "metabolism_overlay";
    public static final MetabolismOverlay HUD = new MetabolismOverlay();
    public final MetabolismWidget widget = new MetabolismWidget(manager().root());

    public MetabolismOverlay() {
        manager().root().add(widget);

        LayoutRegistry.register(new LayoutRegistry.Entry(
                WIDGET_ID,
                this,
                (parent) -> {
                    MetabolismWidget w = new MetabolismWidget(parent);
                    w.repositionable(true);
                    return w;
                },
                widget::rect,
                widget::setRect
        ));
    }

    @Override
    protected void onScreenResize(int screenWidth, int screenHeight) {
        resolveAndApply(screenWidth, screenHeight);
    }

    private void resolveAndApply(int screenWidth, int screenHeight) {
        int w = Math.max(widget.minimumWidth(), 64);
        int h = Math.max(widget.minimumHeight(), 64);
        Optional<Rect> saved = LayoutRegistry.resolvePosition(
                WIDGET_ID, screenWidth, screenHeight, w, h);
        Rect resolved = saved.orElse(new Rect(0, 0, w, h));
        widget.setRect(resolved);
        widget.active(true);
        widget.visible(true);
        // Save default position so anchoring works on resize
        if (saved.isEmpty()) {
            LayoutRegistry.savePosition(WIDGET_ID, resolved, screenWidth, screenHeight);
        }
    }
}
