package ninja.crinkle.mod.client.gui.widgets;

import ninja.crinkle.mod.CrinkleMod;
import ninja.crinkle.mod.client.gui.properties.Point;
import ninja.crinkle.mod.client.gui.properties.Rect;
import ninja.crinkle.mod.client.gui.renderers.ThemeGraphics;
import ninja.crinkle.mod.metabolism.Metabolism;
import ninja.crinkle.mod.metabolism.Pang;
import ninja.crinkle.mod.util.ClientUtil;

import java.util.HashMap;
import java.util.Map;

public class MetabolismWidget extends AnimatedWidget {
    private final Map<Metabolism.Type, Boolean> inPang = new HashMap<>();

    public MetabolismWidget(AbstractContainer parent) {
        this(new AnimatedWidget.Builder(parent));
        name("metabolism_widget");
    }

    public MetabolismWidget(Builder builder) {
        super(builder);
        CrinkleMod.EVENT_BUS.register(this);
    }

    public boolean inPang(Metabolism.Type type) {
        return inPang.getOrDefault(type, false);
    }

    public void inPang(Metabolism.Type type, boolean inPang) {
        this.inPang.put(type, inPang);
    }

    private String characterSpriteId(Metabolism metabolism) {
        if (inPang(metabolism.type()) && metabolism.pang() == Pang.None) {
            return "relief";
        }
        return metabolism.pang().name().toLowerCase();
    }

    private void updateAnimations(Metabolism metabolism) {
        // Handle start of pangs, wait to handle stop so we can show relief
        // Accidents can come before the pangs stop
        if ((!inPang(metabolism.type()) && metabolism.pang() != Pang.None) || metabolism.pang() == Pang.Accident) {
            clearPlayer();
            inPang(metabolism.type(), metabolism.pang() != Pang.None);
        }
        tryUpdateAnimations(animationSpeed(metabolism),
                characterSpriteId(metabolism),
                bubbleSpriteId(metabolism));
        inPang(metabolism.type(), metabolism.pang() != Pang.None);
    }

    @Override
    protected void tick() {
        super.tick();
        if (isFinished()) {
            updateAnimations(Metabolism.wetOf(ClientUtil.getPlayer()));
            updateAnimations(Metabolism.messOf(ClientUtil.getPlayer()));
        }
    }

    private double animationSpeed(Metabolism metabolism) {
        if (inPang(metabolism.type()) && metabolism.pang() == Pang.None) {
            // relief
            return 0.5;
        }
        return switch(metabolism.pang()) {
            case None -> 1.0;
            case Minor -> 2.0;
            case Major -> 3.0;
            case Accident -> 4.0;
        };
    }

    protected void tryUpdateAnimations(double speed, String characterSprite, String bubbleSprite) {
        if (trySetAnimation(speed, false, "character", characterSprite)) {
            trySetAnimation(speed, false, "bubble", bubbleSprite);
        }
    }

    private String bubbleSpriteId(Metabolism metabolism) {
        if (metabolism.pang() == Pang.None) {
            return "none";
        }
        return metabolism.type().name().toLowerCase();
    }

    @Override
    public AbstractWidget visualCopy(AbstractContainer newParent) {
        MetabolismWidget copy = new MetabolismWidget(newParent);
        copy.copyVisualProperties(this);
        return copy;
    }
}
