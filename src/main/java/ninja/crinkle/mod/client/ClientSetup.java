package ninja.crinkle.mod.client;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import ninja.crinkle.mod.client.gui.overlays.MetabolismOverlay;
import ninja.crinkle.mod.client.ui.overlays.CrinkleOverlay;
import ninja.crinkle.mod.client.ui.screens.DunnyScreen;
import ninja.crinkle.mod.menus.CrinkleMenus;

import static ninja.crinkle.mod.CrinkleMod.MODID;

@Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {
    @SubscribeEvent
    public static void init(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(CrinkleMenus.DUNNY_CONTAINER.get(), DunnyScreen::new);
            DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> ClientHooks::registerEvents);
        });
    }

    @SubscribeEvent
    public static void registerGuiOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAboveAll("crinkle-hud", CrinkleOverlay.HUD);
        event.registerAboveAll("metabolism-hud", MetabolismOverlay.HUD);
    }
}
