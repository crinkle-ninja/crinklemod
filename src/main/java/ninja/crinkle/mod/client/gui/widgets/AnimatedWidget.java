package ninja.crinkle.mod.client.gui.widgets;

import ninja.crinkle.mod.client.gui.animations.Animation;
import ninja.crinkle.mod.client.gui.animations.Player;
import ninja.crinkle.mod.client.gui.events.DragEvent;
import ninja.crinkle.mod.client.gui.events.DragStoppedEvent;
import ninja.crinkle.mod.client.gui.events.MoveEvent;
import ninja.crinkle.mod.client.gui.properties.*;
import ninja.crinkle.mod.client.gui.renderers.ThemeGraphics;

public class AnimatedWidget extends AbstractWidget {
    private final Player player;

    public AnimatedWidget(Builder builder) {
        super(builder);
        this.player = new Player()
                .fps(builder.fps())
                .zIndex(builder.zIndex())
                .size(builder.textureSize());
    }

    public void animation(Animation animation, String spriteId) {
        this.player.play(animation, spriteId);
        Size size = Size.ofPercent(this.player.animationSize().width(),
                this.player.animationSize().height());
        updateLayout(layout -> layout.size(size));
    }

    @Override
    public void onDrag(DragEvent event) {
        if (!visible() || !active()) return;
        Point mouse = event.position();
        mouse.subtract(layout().size().width() / 2, layout().size().height() / 2);
        player.position(Position.relative(ImmutablePoint.from(mouse)));
        super.onDrag(event);
    }

    public static Builder builder(AbstractContainer parent) {
        return new Builder(parent);
    }

    @Override
    public void renderContent(ThemeGraphics graphics, Point pMouse, Box renderedBox, float pPartialTick) {
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
        this.player.position(layout().position());
    }

    @Override
    public void onMove(MoveEvent event) {
        super.onMove(event);
        this.player.position(layout().position());
    }

    public static class Builder extends AbstractWidget.AbstractBuilder<Builder> {
        private int textureSize = 64;
        private int fps = 10;

        public Builder(AbstractContainer parent) {
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
