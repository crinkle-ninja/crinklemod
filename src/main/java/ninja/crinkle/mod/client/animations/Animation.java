package ninja.crinkle.mod.client.animations;

import com.mojang.datafixers.types.Func;
import net.minecraft.client.gui.GuiGraphics;
import ninja.crinkle.mod.config.ClientConfig;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;

public class Animation {
    private double startTime;
    private double elapsedTime;
    private double frameTime;
    private double speed;
    private int frameIndex;
    private int x;
    private int y;
    private final List<CompositeFrame> frames = new ArrayList<>();
    private Runnable onFinished = () -> { };

    public Animation(int x, int y, List<CompositeFrame> frames) {
        this.x = x;
        this.y = y;
        this.frames.addAll(frames);
    }

    public static Builder builder() {
        return new Builder();
    }

    public List<CompositeFrame> getFrames() {
        return frames;
    }

    public CompositeFrame getFrame(int index) {
        return frames.get(index);
    }

    public int height() {
        return frames.stream().mapToInt(CompositeFrame::height).max().orElse(0);
    }

    public Runnable onFinished() {
        return onFinished;
    }

    public void onFinished(Runnable onFinished) {
        this.onFinished = onFinished;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
        this.frameTime = 20.0 / speed;
    }

    public int width() {
        return frames.stream().mapToInt(CompositeFrame::width).max().orElse(0);
    }

    public double getSpeed() {
        return speed;
    }

    public void render(GuiGraphics guiGraphics, int xOffset, int yOffset, int frame, int blitOffset) {
        getFrame(frame).render(guiGraphics, getX() + xOffset, getY() + yOffset, blitOffset);
    }

    public void render(GuiGraphics guiGraphics, int xOffset, int yOffset, int blitOffset) {
        render(guiGraphics, xOffset, yOffset, getCurrentFrameIndex(), blitOffset);
    }

    public int getCurrentFrameIndex() {
        return frameIndex;
    }

    public void update(double gameTime) {
        if (isFinished() && onFinished != null) {
            onFinished.run();
        }

        if (!ClientConfig.overlay().metabolism.animated.get()) {
            frameIndex = 0;
            return;
        }

        if (startTime == 0) {
            startTime = gameTime;
        }
        elapsedTime = gameTime - startTime;
        frameIndex = (int) (elapsedTime / frameTime) % frames.size();
    }


    public boolean isFinished() {
        return elapsedTime >= frames.size() * frameTime;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public static class Builder {
        private Runnable onFinished;
        private double speed = 1.0;
        private int x = 0;
        private int y = 0;
        private final List<CompositeFrame> frames = new ArrayList<>();

        public Builder onFinished(Runnable onFinished) {
            this.onFinished = onFinished;
            return this;
        }

        public Builder speed(double speed) {
            this.speed = speed;
            return this;
        }

        public Builder x(int x) {
            this.x = x;
            return this;
        }

        public Builder y(int y) {
            this.y = y;
            return this;
        }

        public Builder position(int x, int y) {
            x(x);
            return y(y);
        }

        public Builder addSpriteGroups(SpriteGroup... spriteGroups) {
            int largestIndex = Arrays.stream(spriteGroups).mapToInt(g -> g.getSprites().size()).max().orElse(0);
            if (largestIndex == 0) {
                return this;
            }
            for (int i = 0; i < largestIndex; i++) {
                List<Sprite> sprites = new ArrayList<>();
                for (SpriteGroup spriteGroup : spriteGroups) {
                    if (spriteGroup.getSprites().isEmpty()) {
                        continue;
                    }
                    int index = i % spriteGroup.getSprites().size();
                    sprites.add(spriteGroup.getSprites().get(index));
                }
                frames.add(CompositeFrame.builder().addSprites(sprites).build());
            }
            return this;
        }

        public Animation build() {
            Animation ani = new Animation(x, y, frames);
            ani.setSpeed(speed);
            ani.onFinished(onFinished);
            return ani;
        }
    }
}
