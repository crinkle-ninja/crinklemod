package ninja.crinkle.mod.client.gui.overlays;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import ninja.crinkle.mod.client.gui.properties.Sizing;
import ninja.crinkle.mod.client.gui.widgets.MetabolismWidget;
import ninja.crinkle.mod.client.gui.widgets.UndergarmentWidget;
import ninja.crinkle.mod.undergarment.Undergarment;
import ninja.crinkle.mod.util.ClientUtil;

public class CrinkleOverlay extends AbstractOverlay {
    public static final CrinkleOverlay HUD = new CrinkleOverlay();
    public final MetabolismWidget metabolismWidget = new MetabolismWidget(manager().root());
    public final UndergarmentWidget undergarmentWidget = new UndergarmentWidget(manager().root());

    public CrinkleOverlay() {
        metabolismWidget.active(true);
        undergarmentWidget.active(true);
        manager().root().add(metabolismWidget);
        manager().root().add(undergarmentWidget);
        manager().root().verticalSizing(Sizing.Expand);
    }

    @Override
    public void render(ForgeGui gui, GuiGraphics guiGraphics, float partialTick, int screenWidth, int screenHeight) {
        undergarmentWidget.visible(Undergarment.getWornUndergarment(ClientUtil.getPlayer()) != ItemStack.EMPTY);
        super.render(gui, guiGraphics, partialTick, screenWidth, screenHeight);
    }
}
