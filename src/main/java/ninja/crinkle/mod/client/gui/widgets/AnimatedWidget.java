package ninja.crinkle.mod.client.gui.widgets;

import com.mojang.logging.LogUtils;
import ninja.crinkle.mod.client.gui.animations.Animation;
import ninja.crinkle.mod.client.gui.animations.Player;
import ninja.crinkle.mod.client.gui.events.DragEvent;
import ninja.crinkle.mod.client.gui.events.DragStoppedEvent;
import ninja.crinkle.mod.client.gui.events.MoveEvent;
import ninja.crinkle.mod.client.gui.properties.Point;
import ninja.crinkle.mod.client.gui.properties.Rect;
import ninja.crinkle.mod.client.gui.renderers.ThemeGraphics;
import ninja.crinkle.mod.client.gui.themes.Theme;
import ninja.crinkle.mod.client.gui.themes.ThemeRegistry;
import org.slf4j.Logger;

public class AnimatedWidget extends AbstractWidget {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Player player;

    public AnimatedWidget(Builder builder) {
        super(builder);
        this.player = new Player()
                .fps(builder.fps())
                .zIndex(builder.zIndex())
                .size(builder.textureSize());
    }

    @Override
    public void setRect(Rect rect) {
        super.setRect(rect);
        this.player.position(Point.of(rect.x(), rect.y()));
    }

    public void animation(Animation animation, String spriteId) {
        this.player.play(animation, spriteId);
        // Update min size and rect dimensions based on animation size
        int aw = this.player.animationSize().width();
        int ah = this.player.animationSize().height();
        minimumSize(aw, ah);
        if (!rect().equals(Rect.ZERO)) {
            setRect(new Rect(rect().x(), rect().y(), aw, ah));
        }
    }

    @Override
    public void onDrag(DragEvent event) {
        if (!visible() || !active()) return;
        super.onDrag(event);
    }

    @Override
    public void renderContent(ThemeGraphics graphics, Point pMouse, Rect renderedRect, float pPartialTick) {
        if (!visible() || !active()) return;
        this.player.render(graphics, pMouse, pPartialTick);
    }

    public boolean isFinished() {
        return this.player.isFinished();
    }

    public void fps(double framesPerSecond) {
        this.player.fps(framesPerSecond);
    }

    public void clearPlayer() {
        this.player.clear();
    }

    public void onFinished(Runnable runnable) {
        this.player.onFinished(runnable);
    }

    @Override
    public void onDragStopped(DragStoppedEvent event) {
        super.onDragStopped(event);
        this.player.position(Point.of(rect().x(), rect().y()));
    }

    @Override
    public void onMove(MoveEvent event) {
        super.onMove(event);
        this.player.position(Point.of(rect().x(), rect().y()));
    }

    protected boolean trySetAnimations(double speed, String animationId, String ...sprites) {
        Theme theme = ThemeRegistry.current();
        if (theme == null) return false;
        boolean shouldClearPlayer = true;
        for (String spriteId : sprites) {
            Animation ani = theme.animation(animationId).orElse(null);
            if (ani == null) {
                LOGGER.error("animation is null for animationId {} of theme {}", animationId, theme.id());
                return false;
            }
            if (shouldClearPlayer) {
                clearPlayer();
                shouldClearPlayer = false;
            }
            fps(speed);
            animation(ani, spriteId);
        }
        return true;
    }

    public static class Builder extends AbstractWidget.AbstractBuilder<Builder> {
        private int textureSize = 64;
        private int fps = 10;

        protected Builder(AbstractContainer parent) {
            super(parent);
        }

        @Override
        public AbstractWidget build() {
            return new AnimatedWidget(this);
        }

        @Override
        protected Builder self() {
            return this;
        }

        @Override
        public AbstractContainer push() {
            parent().add(this);
            return parent();
        }

        public Builder textureSize(int textureSize) {
            this.textureSize = textureSize;
            return self();
        }

        public int textureSize() {
            return this.textureSize;
        }

        public Builder fps(int fps) {
            this.fps = fps;
            return self();
        }

        public int fps() {
            return this.fps;
        }
    }
}
