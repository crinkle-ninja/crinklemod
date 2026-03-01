package ninja.crinkle.mod.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.common.MinecraftForge;
import ninja.crinkle.mod.events.handlers.CrinkleForgeBusClientEvents;

public class ClientHooks {
    public static Minecraft getMinecraft() {
        return Minecraft.getInstance();
    }

    public static Player getPlayer() {
        return getMinecraft().player;
    }

    public static HitResult getHitResult() {
        return getMinecraft().hitResult;
    }

    public static void registerEvents() {
        MinecraftForge.EVENT_BUS.register(new CrinkleForgeBusClientEvents());
    }
}
