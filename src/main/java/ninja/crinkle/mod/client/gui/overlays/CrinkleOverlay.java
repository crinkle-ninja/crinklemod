package ninja.crinkle.mod.client.gui.overlays;

import ninja.crinkle.mod.client.gui.widgets.MetabolismWidget;
import ninja.crinkle.mod.client.gui.widgets.UndergarmentWidget;

public class CrinkleOverlay extends AbstractOverlay {
    public static final CrinkleOverlay HUD = new CrinkleOverlay();
    public final MetabolismWidget metabolismWidget = new MetabolismWidget(manager().root());
    public final UndergarmentWidget undergarmentWidget = new UndergarmentWidget(manager().root());

    public CrinkleOverlay() {
        metabolismWidget.active(true);
        undergarmentWidget.active(true);
        manager().root().add(metabolismWidget);
        manager().root().add(undergarmentWidget);
    }
}
