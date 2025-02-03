package ninja.crinkle.mod.client.gui.widgets;

import net.minecraft.client.Minecraft;
import ninja.crinkle.mod.client.animations.Animation;
import ninja.crinkle.mod.client.gui.properties.Box;
import ninja.crinkle.mod.client.gui.properties.Point;
import ninja.crinkle.mod.client.gui.properties.Size;
import ninja.crinkle.mod.client.gui.renderers.ThemeGraphics;
import ninja.crinkle.mod.client.gui.states.references.ValueRef;
import ninja.crinkle.mod.util.ClientUtil;

public class AnimatedWidget extends AbstractWidget {
    private final ValueRef<Animation> animation;

    public AnimatedWidget(Builder builder) {
        super(builder);
        this.animation = manager().stateStorage().createValue(Animation.class, builder.animation());
    }

    public void animation(Animation animation) {
        this.animation.set(animation);
        size(Size.of(animation.width(), animation.height()));
    }

    public Animation animation() {
        return animation.get();
    }

    public static Builder builder(AbstractContainer parent) {
        return new Builder(parent);
    }


    @Override
    public void renderContent(ThemeGraphics graphics, Point pMouse, Box renderedBox, float pPartialTick) {
        Minecraft minecraft = ClientUtil.getMinecraft();
        if (minecraft == null || minecraft.level == null || minecraft.player == null) return;
        double gameTime = minecraft.level.getGameTime() + pPartialTick;
        Animation animation = animation();
        if (animation == null) return;
        animation.update(gameTime);
        animation.render(graphics.graphics(), renderedBox.topLeft().xInt(), renderedBox.topLeft().yInt(), zIndex());
    }

    public static class Builder extends AbstractWidget.AbstractBuilder<Builder> {
        private Animation animation;

        protected Builder(AbstractContainer parent) {
            super(parent);
        }

        public Animation animation() {
            return animation;
        }

        public Builder animation(Animation animation) {
            this.animation = animation;
            return self();
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
    }
}
