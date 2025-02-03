package ninja.crinkle.mod.client.gui.screens;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import ninja.crinkle.mod.client.gui.events.DroppedEvent;
import ninja.crinkle.mod.client.gui.events.listeners.MouseListener;
import ninja.crinkle.mod.client.gui.overlays.MetabolismOverlay;
import ninja.crinkle.mod.client.gui.properties.Position;
import ninja.crinkle.mod.client.gui.widgets.MetabolismWidget;
import ninja.crinkle.mod.config.ClientConfig;
import org.jetbrains.annotations.NotNull;

public class OverlayConfigScreen extends AbstractScreen implements MouseListener {
    public OverlayConfigScreen() {
        super(Component.literal("Overlay Config"));
    }

    @Override
    public String name() {
        return "OverlayConfigScreen";
    }

    protected void init() {
        MetabolismWidget metabolismWidget = new MetabolismWidget(root());
        metabolismWidget.position(Position.relative(
                ClientConfig.overlay().metabolism.x.get(),
                ClientConfig.overlay().metabolism.y.get()
        ));
        metabolismWidget.draggable(true);
        root().add(metabolismWidget);
        metabolismWidget.addListener(this);
        super.init();
    }

    @Override
    public void onDropped(DroppedEvent event) {
        Position newPosition = event.widget().position();
        MetabolismOverlay.HUD.widget.position(newPosition);
        ClientConfig.overlay().metabolism.x.set(newPosition.point().xInt());
        ClientConfig.overlay().metabolism.y.set(newPosition.point().yInt());
        ClientConfig.getSpec().save();
    }

    @Override
    public void render(@NotNull GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        renderBackground(pGuiGraphics);
        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
    }
}
