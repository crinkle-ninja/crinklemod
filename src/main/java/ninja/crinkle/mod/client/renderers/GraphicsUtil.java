package ninja.crinkle.mod.client.renderers;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import ninja.crinkle.mod.client.color.Color;
import ninja.crinkle.mod.client.icons.Icons;
import org.joml.Matrix4f;

public class GraphicsUtil {
    private final GuiGraphics graphics;

    public GraphicsUtil(GuiGraphics graphics) {
        this.graphics = graphics;
    }

    public void render(Icons icon, int x, int y, Color color) {
        render(icon, x, y, icon.width(), icon.height(), 1.0f, 1.0f, color);
    }

    public void render(Icons icon, int x, int y, int width, int height, float uPercent, float vPercent, Color color) {
        TextureAtlasSprite sprite = icon.getSprite();
        float x2 = (float) (x + width) - (width * (1.0F - uPercent));
        float y2 = (float) (y + height) - (height * (1.0F - vPercent));
        float minU = sprite.getU0();
        float maxU = sprite.getU1() - (sprite.getU1() - sprite.getU0()) * (1.0F - uPercent);
        float minV = sprite.getV0();
        float maxV = sprite.getV1() - (sprite.getV1() - sprite.getV0()) * (1.0F - vPercent);
        renderIcon(sprite, x, y, x2, y2, minU, maxU, minV, maxV, color);
    }

    private void renderIcon(TextureAtlasSprite sprite, float x1, float y1, float x2, float y2, float minU, float maxU,
                            float minV, float maxV, Color color) {
        ResourceLocation atlasLocation = sprite.atlasLocation();
        RenderSystem.setShaderTexture(0, atlasLocation);
        RenderSystem.setShader(GameRenderer::getPositionColorTexShader);
        Matrix4f matrix4f = graphics.pose().last().pose();
        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);
        Color c = color;
        if (c == Color.RAINBOW)
            c = Color.rainbow(1000, 0);
        bufferbuilder.vertex(matrix4f, x1, y1, 0).color(c.ABGR()).uv(minU, minV).endVertex();
        bufferbuilder.vertex(matrix4f, x1, y2, 0).color(c.ABGR()).uv(minU, maxV).endVertex();
        bufferbuilder.vertex(matrix4f, x2, y2, 0).color(c.ABGR()).uv(maxU, maxV).endVertex();
        bufferbuilder.vertex(matrix4f, x2, y1, 0).color(c.ABGR()).uv(maxU, minV).endVertex();
        BufferUploader.drawWithShader(bufferbuilder.end());
    }

    public void render(Icons icon, int x, int y, int width, int height, Color color) {
        if (width < 0 || height < 0)
            throw new IllegalArgumentException("Width and height must be positive");
        render(icon, x, y, width, height, 1.0f, 1.0f, color);
    }
}
