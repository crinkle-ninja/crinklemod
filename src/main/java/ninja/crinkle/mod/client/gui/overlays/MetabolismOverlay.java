package ninja.crinkle.mod.client.gui.overlays;

import net.minecraft.client.Minecraft;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import ninja.crinkle.mod.client.color.Color;
import ninja.crinkle.mod.client.gui.properties.Border;
import ninja.crinkle.mod.client.gui.properties.Position;
import ninja.crinkle.mod.client.gui.widgets.MetabolismWidget;

public class MetabolismOverlay extends AbstractOverlay {
    public static final MetabolismOverlay HUD = new MetabolismOverlay();
    public final MetabolismWidget widget = new MetabolismWidget(manager().root());

    public MetabolismOverlay() {
        manager().root().add(widget);
        FMLJavaModLoadingContext.get().getModEventBus().register(this);
    }

    @SubscribeEvent
    public void onConfigLoad(ModConfigEvent configEvent) {
        if (configEvent instanceof ModConfigEvent.Reloading
                || configEvent instanceof ModConfigEvent.Loading) {
            if (configEvent.getConfig().getConfigData().contains("overlay.metabolism")) {
                Position newPosition = Position.absolute(
                        configEvent.getConfig().getConfigData().getInt("overlay.metabolism.x"),
                        configEvent.getConfig().getConfigData().getInt("overlay.metabolism.y")
                );
                widget.updateLayout(layout -> layout.position(newPosition));
                widget.active(true);
                widget.visible(true);
            }
        }
    }
}
