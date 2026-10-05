package ninja.crinkle.mod.client.gui.widgets;

import com.mojang.logging.LogUtils;
import ninja.crinkle.mod.CrinkleMod;
import ninja.crinkle.mod.client.gui.properties.Point;
import ninja.crinkle.mod.client.gui.properties.Rect;
import ninja.crinkle.mod.client.gui.renderers.ThemeGraphics;
import ninja.crinkle.mod.metabolism.Metabolism;
import ninja.crinkle.mod.util.ClientUtil;
import org.slf4j.Logger;

import java.util.Comparator;
import java.util.stream.Stream;

public class MetabolismWidget extends AnimatedWidget {
    private static final Logger LOGGER = LogUtils.getLogger();

    public MetabolismWidget(AbstractContainer parent) {
        this(new AnimatedWidget.Builder(parent));
        name("metabolism_widget");
    }

    public MetabolismWidget(Builder builder) {
        super(builder);
        CrinkleMod.EVENT_BUS.register(this);
    }

    private String characterSpriteId(Metabolism metabolism) {
        return metabolism.pang().name().toLowerCase();
    }

    private void updateAnimations() {
        Metabolism wet = Metabolism.wetOf(ClientUtil.getPlayer());
        Metabolism mess = Metabolism.messOf(ClientUtil.getPlayer());
        Metabolism metabolism = Stream.of(wet, mess).max(Comparator.comparingInt(m -> m.pang().ordinal())).orElseThrow();

        // Bubble
        if (wet.isNormal() && mess.isNormal()) {
            trySetAnimation(animationSpeed(metabolism), false, "bubble", "none");
        } else {
            if (wet.isPang() && mess.isPang()) {
                trySetAnimation(animationSpeed(metabolism), false, "bubble", "both");
            } else {
                trySetAnimation(animationSpeed(metabolism), false, "bubble", metabolism.type().name().toLowerCase());
            }
        }

        // Character
        trySetAnimation(animationSpeed(metabolism), false, "character", characterSpriteId(metabolism));
    }

    @Override
    public void renderContent(ThemeGraphics graphics, Point pMouse, Rect renderedRect, float pPartialTick) {
        if (isFinished()) {
            updateAnimations();
        }
        super.renderContent(graphics, pMouse, renderedRect, pPartialTick);
    }

    private double animationSpeed(Metabolism metabolism) {
        return switch(metabolism.pang()) {
            case Relief -> 0.75;
            case None -> 1.0;
            case Minor -> 1.5;
            case Major -> 2.0;
            case Accident -> 3.0;
        };
    }

    protected void tryUpdateAnimations(double speed, String characterSprite, String bubbleSprite) {
        LOGGER.trace("tryUpdateAnimations: character={} bubble={}", characterSprite, bubbleSprite);
        if (trySetAnimation(speed, false, "character", characterSprite)) {
            trySetAnimation(speed, false, "bubble", bubbleSprite);
        }
    }

    @Override
    public AbstractWidget visualCopy(AbstractContainer newParent) {
        MetabolismWidget copy = new MetabolismWidget(newParent);
        copy.copyVisualProperties(this);
        return copy;
    }
}
