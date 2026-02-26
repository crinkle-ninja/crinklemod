package ninja.crinkle.mod.client.gui.screens;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import ninja.crinkle.mod.client.gui.events.DroppedEvent;
import ninja.crinkle.mod.client.gui.events.listeners.MouseListener;
import ninja.crinkle.mod.client.gui.overlays.MetabolismOverlay;
import ninja.crinkle.mod.client.gui.properties.Position;
import ninja.crinkle.mod.client.gui.widgets.MetabolismWidget;
import ninja.crinkle.mod.config.ClientConfig;
import ninja.crinkle.mod.util.ClientUtil;
import org.jetbrains.annotations.NotNull;

public class OverlayConfigScreen extends AbstractScreen implements MouseListener {
    private final MetabolismWidget metabolism = new MetabolismWidget(root());

    public OverlayConfigScreen() {
        super(Component.literal("Overlay Config"), ClientUtil.screenSize());
    }

    @Override
    public String name() {
        return "OverlayConfigScreen";
    }

    public void init() {
        Position configPos = Position.relative(
                ClientConfig.overlay().metabolism.x.get(),
                ClientConfig.overlay().metabolism.y.get()
        );
        metabolism.updateLayout(layout -> layout.position(configPos));
        metabolism.draggable(true);
        root().add(metabolism);
        metabolism.addListener(this);
        metabolism.visible(true);
        metabolism.active(true);
        super.init();
    }

    @Override
    public void onDropped(DroppedEvent event) {
        Position newPosition = event.widget().layout().position();
        if (newPosition.equals(metabolism.layout().position())) return;
        metabolism.updateLayout(layout -> layout.position(newPosition));
        MetabolismOverlay.HUD.widget.updateLayout(layout -> layout.position(newPosition));
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
