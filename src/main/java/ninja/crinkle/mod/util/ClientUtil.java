package ninja.crinkle.mod.util;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import ninja.crinkle.mod.client.ClientHooks;
import ninja.crinkle.mod.client.gui.properties.Point;
import ninja.crinkle.mod.client.gui.properties.Rect;

import java.util.Optional;

public class ClientUtil {
    public static String getClipboard() {
        return getMinecraft().keyboardHandler.getClipboard();
    }

    public static void setClipboard(String text) {
        getMinecraft().keyboardHandler.setClipboard(text);
    }

    public static Point getMousePosition() {
        return new Point(getMinecraft().mouseHandler.xpos(), getMinecraft().mouseHandler.ypos());
    }

    public static Player getPlayer() {
        return Optional.ofNullable(DistExecutor.safeCallWhenOn(Dist.CLIENT, () -> ClientHooks::getMinecraft))
                .map(minecraft -> minecraft.player).orElse(null);
    }

    public static int screenHeight() {
        return getMinecraft().getWindow().getGuiScaledHeight();
    }

    public static Rect screenRect() {
        int width = getMinecraft().getWindow().getGuiScaledWidth();
        int height = getMinecraft().getWindow().getGuiScaledHeight();
        return new Rect(0, 0, width, height);
    }

    public static Minecraft getMinecraft() {
        return DistExecutor.safeCallWhenOn(Dist.CLIENT, () -> ClientHooks::getMinecraft);
    }

    public static int screenWidth() {
        return getMinecraft().getWindow().getGuiScaledWidth();
    }
}
