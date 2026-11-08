package ninja.crinkle.mod.events.handlers;

import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import ninja.crinkle.mod.CrinkleMod;
import ninja.crinkle.mod.capabilities.*;
import ninja.crinkle.mod.metabolism.Metabolism;
import ninja.crinkle.mod.util.ClientUtil;

@Mod.EventBusSubscriber(modid = CrinkleMod.MODID)
public class CrinkleModBusEvents {
    @SubscribeEvent
    public static void onPlayerCloned(PlayerEvent.Clone event) {
        if (event.isWasDeath()) {
            event.getOriginal().reviveCaps();
            event.getOriginal().getCapability(WetProvider.CAPABILITY).ifPresent( a ->
                    Metabolism.wetOf(event.getEntity()).copyFrom(a));

            event.getOriginal().getCapability(MessProvider.CAPABILITY).ifPresent( a ->
                    Metabolism.messOf(event.getEntity()).copyFrom(a));
            event.getOriginal().invalidateCaps();
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (ClientUtil.isDedicatedServer() || ClientUtil.getMinecraft().isLocalServer()) {
            Metabolism.wetOf(event.getEntity()).syncClient();
            Metabolism.messOf(event.getEntity()).syncClient();
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (ClientUtil.isClient()) {
            Metabolism.wetOf(event.getEntity()).syncServer();
            Metabolism.messOf(event.getEntity()).syncServer();
        }
    }
}
