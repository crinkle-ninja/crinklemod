package ninja.crinkle.mod.client.gui.renderers;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import ninja.crinkle.mod.client.color.Color;
import ninja.crinkle.mod.client.gui.properties.Box;
import ninja.crinkle.mod.client.gui.properties.Point;
import ninja.crinkle.mod.client.gui.properties.Size;
import ninja.crinkle.mod.client.gui.textures.Atlas;
import ninja.crinkle.mod.util.ClientUtil;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

public class ThemeGraphics extends GuiGraphics {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final GuiGraphics graphics;
    private final Atlas atlas;

    public ThemeGraphics(GuiGraphics guiGraphics, Atlas atlas) {
        super(ClientUtil.getMinecraft(), guiGraphics.bufferSource());
        this.graphics = guiGraphics;
        this.atlas = atlas;
    }

    public Atlas atlas() {
        return atlas;
    }

    public void blit(ResourceLocation texture, Point position, Size size, int zOffset) {
        TextureAtlasSprite sprite = atlas.getSprite(texture);
        blit(position.xInt(), position.yInt(), zOffset, size.width(), size.height(), sprite);
    }

    public void drawBox(Box pBox, Color pColor, int zIndex) {
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        fill(pBox, pColor, zIndex);
        RenderSystem.disableDepthTest();
        RenderSystem.disableBlend();
    }

    /**
     * Draws a string with the specified color at the specified position.
     * Shadow is disabled due to incompatibility with the gui system.
     *
     * @param font   the font to use
     * @param text   the text to draw
     * @param x      the x position
     * @param y      the y position
     * @param color  the color to use
     */
    public void drawCenteredString(@NotNull Font font, @NotNull Component text, int x, int y, int z, int color) {
        pose().pushPose();
        pose().translate(0, 0, z);
        drawString(font, text, x - font.width(text) / 2, y - font.lineHeight / 2, color, false);
        pose().popPose();
    }


    public void fill(Box pBox, Color pColor, int zIndex) {
        assert pBox.position().absolute() : "Box must have an absolute position";
        Point topLeft = pBox.topLeft();
        Point bottomRight = pBox.bottomRight();
        fill(topLeft.xInt(), topLeft.yInt(), bottomRight.xInt(), bottomRight.yInt(), zIndex, pColor.color());
    }

    public GuiGraphics graphics() {
        return graphics;
    }

    public void text(String text, Point point, int zIndex, Color color, boolean shadow) {
        pose().translate(0, 0, zIndex);
        drawString(ClientUtil.getMinecraft().font, text, point.xInt(), point.yInt(), color.color(), shadow);
        pose().translate(0, 0, -zIndex);
    }

    public int textHeight() {
        return ClientUtil.getMinecraft().font.lineHeight;
    }

    public int textWidth(String text) {
        return ClientUtil.getMinecraft().font.width(text);
    }
}
