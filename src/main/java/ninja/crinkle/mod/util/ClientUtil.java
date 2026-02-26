package ninja.crinkle.mod.util;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import ninja.crinkle.mod.client.ClientHooks;
import ninja.crinkle.mod.client.gui.properties.*;

import java.util.Optional;

public class ClientUtil {
    public static Player getPlayer() {
        return Optional.ofNullable(DistExecutor.safeCallWhenOn(Dist.CLIENT, () -> ClientHooks::getMinecraft))
                .map(minecraft -> minecraft.player).orElse(null);
    }

    public static Minecraft getMinecraft() {
        return DistExecutor.safeCallWhenOn(Dist.CLIENT, () -> ClientHooks::getMinecraft);
    }

    public static Size screenSize() {
        int width = getMinecraft().getWindow().getGuiScaledWidth();
        int height = getMinecraft().getWindow().getGuiScaledHeight();
        return Size.ofPixels(width, height);
    }

    public static Point getMousePosition() {
        return new ImmutablePoint(getMinecraft().mouseHandler.xpos(), getMinecraft().mouseHandler.ypos());
    }

    public static void setClipboard(String text) {
        getMinecraft().keyboardHandler.setClipboard(text);
    }

    public static String getClipboard() {
        return getMinecraft().keyboardHandler.getClipboard();
    }
}
