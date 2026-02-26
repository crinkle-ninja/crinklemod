package ninja.crinkle.mod.client.gui.animations;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import ninja.crinkle.mod.client.color.Color;
import ninja.crinkle.mod.client.gui.properties.Point;
import ninja.crinkle.mod.client.gui.properties.Position;
import ninja.crinkle.mod.client.gui.textures.TextureSize;
import ninja.crinkle.mod.client.gui.renderers.ThemeGraphics;
import ninja.crinkle.mod.client.gui.renderers.ThemeRenderable;
import ninja.crinkle.mod.config.ClientConfig;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Player implements ThemeRenderable {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static int DEFAULT_SIZE = 64;
    private double startTime;
    private double elapsedTime;
    private double speed;
    private Position position;
    private int zIndex = 0;
    private int size = DEFAULT_SIZE;
    private final Map<Animation, String> animations = new ConcurrentHashMap<>();
    private double totalTime;
    private Runnable onFinished = () -> {
    };
    private TextureSize animationSize;

    public Player() {
        clear();
    }

    public void clear() {
        this.animations.clear();
        this.totalTime = 0;
        this.startTime = 0;
        this.elapsedTime = 0;
        this.animationSize = TextureSize.of(0,0);
    }

    public void play(Animation animation, String spriteId) {
        animations.put(animation, spriteId);
        int totalFrameTime = animation.sprite(spriteId, size).frames().size() * frameTime();
        if (totalFrameTime > totalTime) {
            totalTime = totalFrameTime;
        }
        TextureSize newAnimationSize = TextureSize.of(size(), size()).add(animation.offset().xInt(), animation.offset().yInt());
        if (newAnimationSize.width() > animationSize.width()) {
            animationSize = TextureSize.of(newAnimationSize.width(),  animationSize.height());
        }
        if (newAnimationSize.height() > animationSize.height()) {
            animationSize = TextureSize.of(animationSize.width(), newAnimationSize.height());
        }
    }

    public TextureSize animationSize() {
        return animationSize;
    }

    public Player zIndex(int zIndex) {
        this.zIndex = zIndex;
        return this;
    }

    public int zIndex() {
        return zIndex;
    }

    public Position position() {
        return position;
    }

    public Player position(Position position) {
        this.position = position;
        return this;
    }

    public void onFinished(Runnable onFinished) {
        this.onFinished = onFinished;
    }

    @Override
    public void render(@NotNull ThemeGraphics graphics, Point pMouse, float pPartialTick) {
        double gameTime = Minecraft.getInstance().level != null ? Minecraft.getInstance().level.getGameTime() : 0;
        gameTime += pPartialTick;
        for (Animation animation : animations.keySet()) {
            Frame frame = currentFrame(gameTime, animation);
            if (frame != null) {
                graphics.blit(frame.resourceLocation(size()), position().point().add(animation.offset()),
                        TextureSize.of(size(), size()), zIndex(), Color.WHITE);
            } else {
                LOGGER.error("No frame for animation: {} and sprite {}", animation, animations.get(animation));
            }
        }
    }

    public Player size(int size) {
        this.size = size;
        return this;
    }

    public int size() {
        return size;
    }

    public Player fps(double fps) {
        this.speed = fps;
        return this;
    }

    private int frameTime() {
        return (int) (20.0 / speed);
    }

    protected Frame currentFrame(double gameTime, Animation animation) {
        if (isFinished()) {
            if (onFinished != null) {
                onFinished.run();
            }
        }

        String spriteId = animations.get(animation);
        if (spriteId == null) {
            throw new IllegalStateException("No sprite id for animation: " + animation);
        }
        Sprite sprite = animation.sprite(spriteId, size);
        if (!ClientConfig.overlay().metabolism.animated.get()) {
            return sprite.frames().get(0);
        }

        if (startTime == 0) {
            startTime = gameTime;
        }
        elapsedTime = gameTime - startTime;
        int frameIndex = (int) (elapsedTime / frameTime()) % sprite.frames().size();
        return sprite.frames().get(frameIndex);
    }


    public boolean isFinished() {
        if (animations.isEmpty()) {
            return true;
        }
        int totalFrameTime = animations.keySet().stream()
                .mapToInt(animation -> animation.sprite(animations.get(animation), size).frames().size() * frameTime())
                .max()
                .orElse(0);
        return elapsedTime >= totalFrameTime;
    }

    @Override
    public String toString() {
        return String.format("Player{position=%s, zIndex=%d, size=%d, speed=%.2f, elapsedTime=%.2f, totalTime=%.2f, " +
                        "animationSize=%s, animations=%s}",
                position, zIndex, size, speed, elapsedTime, totalTime, animationSize,
                animations.entrySet().stream()
                        .map(entry -> entry.getKey().id() + ":" + entry.getValue())
                        .toList());
    }
}
