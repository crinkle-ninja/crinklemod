package ninja.crinkle.mod.client.gui.renderers;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.logging.LogUtils;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import ninja.crinkle.mod.client.color.Color;
import ninja.crinkle.mod.client.gui.properties.Point;
import ninja.crinkle.mod.client.gui.properties.Rect;
import ninja.crinkle.mod.client.gui.textures.Atlas;
import ninja.crinkle.mod.client.gui.textures.TextureSize;
import ninja.crinkle.mod.util.ClientUtil;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
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

    public void blit(ResourceLocation texture, Point position, TextureSize size, int zOffset, Color color) {
        if (!texture.getPath().startsWith("dynamic")) {
            TextureAtlasSprite sprite = atlas.getSprite(texture);
            blit(sprite, position, size, zOffset, color);
            return;
        }
        int x = position.xInt();
        int y = position.yInt();
        int width = size.width();
        int height = size.height();
        internalBlit(texture, x, y, x + width, y + height, 0, 1, 0, 1, color, zOffset);
    }

    public void blit(TextureAtlasSprite sprite, Point position, TextureSize size, int zOffset, Color color) {
        int x = position.xInt();
        int y = position.yInt();
        int width = size.width();
        int height = size.height();
        internalBlit(sprite.atlasLocation(), x, y, x + width, y + height, sprite.getU0(), sprite.getU1(), sprite.getV0(),
                sprite.getV1(), color, zOffset);
    }

    public void drawBox(Rect pRect, Color pColor, int zIndex) {
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        fill(pRect, pColor, zIndex);
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


    public void fill(int x1, int y1, int x2, int y2, Color color, int zIndex) {
        fill(x1, y1, x2, y2, zIndex, color.get().color());
    }

    public void fill(Rect rect, Color color, int zIndex) {
        fill(rect.x(), rect.y(), rect.right(), rect.bottom(), zIndex, color.get().color());
    }

    public void drawRect(Rect rect, Color color, int zIndex) {
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        fill(rect, color, zIndex);
        RenderSystem.disableDepthTest();
        RenderSystem.disableBlend();
    }

    public GuiGraphics graphics() {
        return graphics;
    }

    public void text(String text, Point point, int zIndex, Color color, boolean shadow) {
        pose().translate(0, 0, zIndex);
        drawString(ClientUtil.getMinecraft().font, text, point.xInt(), point.yInt(), color.get().color(), shadow);
        pose().translate(0, 0, -zIndex);
    }

    public int textHeight() {
        return ClientUtil.getMinecraft().font.lineHeight;
    }

    public int textWidth(String text) {
        return ClientUtil.getMinecraft().font.width(text);
    }

    private void internalBlit(ResourceLocation location, float x1, float y1, float x2, float y2, float minU, float maxU,
                            float minV, float maxV, Color color, int zIndex) {
        RenderSystem.setShaderTexture(0, location);
        RenderSystem.setShader(GameRenderer::getPositionColorTexShader);
        RenderSystem.enableBlend();
        Matrix4f matrix4f = graphics.pose().last().pose();
        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);
        Color c = color.get();
        bufferbuilder.vertex(matrix4f, x1, y1, zIndex).color(c.color()).uv(minU, minV).endVertex();
        bufferbuilder.vertex(matrix4f, x1, y2, zIndex).color(c.color()).uv(minU, maxV).endVertex();
        bufferbuilder.vertex(matrix4f, x2, y2, zIndex).color(c.color()).uv(maxU, maxV).endVertex();
        bufferbuilder.vertex(matrix4f, x2, y1, zIndex).color(c.color()).uv(maxU, minV).endVertex();
        BufferUploader.drawWithShader(bufferbuilder.end());
        RenderSystem.disableBlend();
    }
}
