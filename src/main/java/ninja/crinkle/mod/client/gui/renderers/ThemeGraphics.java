package ninja.crinkle.mod.client.gui.renderers;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.gui.Font;
import org.lwjgl.opengl.GL11;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import ninja.crinkle.mod.client.color.Color;
import ninja.crinkle.mod.client.gui.properties.Point;
import ninja.crinkle.mod.client.gui.properties.Rect;
import ninja.crinkle.mod.client.gui.textures.Atlas;
import ninja.crinkle.mod.client.gui.textures.TextureSize;
import ninja.crinkle.mod.util.ClientUtil;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

import java.util.ArrayDeque;
import java.util.Deque;

@SuppressWarnings("unused")
public class ThemeGraphics extends GuiGraphics {
    private final Atlas atlas;
    private final GuiGraphics graphics;
    private final Deque<Rect> scissorStack = new ArrayDeque<>();
    private Rect scissorRect;

    public ThemeGraphics(GuiGraphics guiGraphics, Atlas atlas) {
        super(ClientUtil.getMinecraft(), guiGraphics.bufferSource());
        this.graphics = guiGraphics;
        this.atlas = atlas;
    }

    public void renderTooltip(Component tooltip, Point mouse) {
        disableScissor();
        graphics().renderTooltip(ClientUtil.getMinecraft().font, tooltip, mouse.xInt(), mouse.yInt());
        if (scissorRect != null)
            enableScissor(scissorRect.x(), scissorRect.y(), scissorRect.right(), scissorRect.bottom());
    }

    public void enableScissor(int x1, int y1, int x2, int y2) {
        Rect candidate = new Rect(x1, y1, x2 - x1, y2 - y1);
        Rect clipped = scissorStack.isEmpty() ? candidate : intersection(scissorStack.peek(), candidate);
        scissorStack.push(clipped);
        applyScissor(clipped);
    }

    public void disableScissor() {
        scissorStack.pop();
        if (scissorStack.isEmpty()) {
            GL11.glDisable(GL11.GL_SCISSOR_TEST);
        } else {
            applyScissor(scissorStack.peek());
        }
    }

    private void applyScissor(Rect r) {
        int scale = (int) ClientUtil.getMinecraft().getWindow().getGuiScale();
        scissorRect = r;
        int x = r.x() * scale;
        int y = ClientUtil.getMinecraft().getWindow().getHeight() - r.bottom() * scale;
        int w = r.width() * scale;
        int h = r.height() * scale;
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(x, y, w, h);
    }

    private static Rect intersection(Rect a, Rect b) {
        int x = Math.max(a.x(), b.x());
        int y = Math.max(a.y(), b.y());
        int x2 = Math.min(a.right(), b.right());
        int y2 = Math.min(a.bottom(), b.bottom());
        return new Rect(x, y, Math.max(0, x2 - x), Math.max(0, y2 - y));
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
        internalBlit(sprite.atlasLocation(), x, y, x + width, y + height, sprite.getU0(), sprite.getU1(),
                sprite.getV0(),
                sprite.getV1(), color, zOffset);
    }

    private void internalBlit(ResourceLocation location, float x1, float y1, float x2, float y2, float minU, float maxU,
                              float minV, float maxV, Color color, int zIndex) {
        if (color.isTransparent()) return;
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

    public void drawBox(Rect pRect, Color pColor, int zIndex) {
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        fill(pRect, pColor, zIndex);
        RenderSystem.disableDepthTest();
        RenderSystem.disableBlend();
    }

    public void fill(Rect rect, Color color, int zIndex) {
        fill(rect.x(), rect.y(), rect.right(), rect.bottom(), zIndex, color.get().color());
    }

    /**
     * Draws a string with the specified color at the specified position.
     * Shadow is disabled due to incompatibility with the gui system.
     *
     * @param font  the font to use
     * @param text  the text to draw
     * @param x     the x position
     * @param y     the y position
     * @param color the color to use
     */
    public void drawCenteredString(@NotNull Font font, @NotNull Component text, int x, int y, int z, int color) {
        pose().pushPose();
        pose().translate(0, 0, z);
        drawString(font, text, x - font.width(text) / 2, y - font.lineHeight / 2, color, false);
        pose().popPose();
    }

    public void drawRect(Rect rect, Color color, int zIndex) {
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        fill(rect, color, zIndex);
        RenderSystem.disableDepthTest();
        RenderSystem.disableBlend();
    }

    public void fill(int x1, int y1, int x2, int y2, Color color, int zIndex) {
        fill(x1, y1, x2, y2, zIndex, color.get().color());
    }

    public GuiGraphics graphics() {
        return graphics;
    }

    public void text(String text, Point point, int zIndex, Color color, Color shadow) {
        pose().translate(0, 0, zIndex);
        if (shadow != null && !shadow.isTransparent())
            drawString(ClientUtil.getMinecraft().font, text, point.xInt() + 1, point.yInt() + 1, shadow.get().color()
                    , false);
        if (color != null && !color.isTransparent())
            drawString(ClientUtil.getMinecraft().font, text, point.xInt(), point.yInt(), color.get().color(), false);
        pose().translate(0, 0, -zIndex);
    }

    public void renderItem(@NotNull ItemStack stack, int x, int y, int zIndex) {
        pose().pushPose();
        pose().translate(0, 0, zIndex);
        renderItem(stack, x, y);
        pose().popPose();
    }

    public void renderItemWithShadow(ItemStack stack, int x, int y, int zIndex) {
        // Shadow pass: render at (+1,+1) tinted black
        RenderSystem.setShaderColor(0f, 0f, 0f, 0.4f);
        pose().pushPose();
        pose().translate(0, 0, zIndex);
        renderItem(stack, x + 1, y + 1);
        pose().popPose();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        // Normal pass: render at (x,y)
        pose().pushPose();
        pose().translate(0, 0, zIndex + 1);
        renderItem(stack, x, y);
        pose().popPose();
    }

    public int textHeight() {
        return ClientUtil.getMinecraft().font.lineHeight;
    }

    public int textWidth(String text) {
        return ClientUtil.getMinecraft().font.width(text);
    }
}
