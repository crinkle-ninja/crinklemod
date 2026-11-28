package ninja.crinkle.mod.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import ninja.crinkle.mod.client.textures.Textures;
import ninja.crinkle.mod.events.handlers.CrinkleForgeBusClientEvents;

public class ClientHooks {

    public static Player getPlayer() {
        return getMinecraft().player;
    }

    public static Minecraft getMinecraft() {
        return Minecraft.getInstance();
    }

    public static boolean isClient() {
        return true;
    }

    public static void registerEvents() {
        MinecraftForge.EVENT_BUS.register(new CrinkleForgeBusClientEvents());
    }

    public static void rebuildGenerators() {
        Textures.rebuildGenerators();
    }
}
