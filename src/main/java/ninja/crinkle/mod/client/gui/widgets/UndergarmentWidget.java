package ninja.crinkle.mod.client.gui.widgets;

import net.minecraft.client.player.LocalPlayer;
import ninja.crinkle.mod.CrinkleMod;
import ninja.crinkle.mod.client.gui.properties.Point;
import ninja.crinkle.mod.client.gui.properties.Rect;
import ninja.crinkle.mod.client.gui.renderers.ThemeGraphics;
import ninja.crinkle.mod.undergarment.Undergarment;
import ninja.crinkle.mod.util.ClientUtil;
import org.jetbrains.annotations.NotNull;

public class UndergarmentWidget extends AnimatedWidget {
    private static final double VISIBLY_MESSY_PERCENT = 0.1d;
    private static final double VISIBLY_WET_PERCENT = 0.1d;

    public UndergarmentWidget(AbstractContainer parent) {
        this(new AnimatedWidget.Builder(parent));
        name("undergarment_widget");
    }

    public UndergarmentWidget(Builder builder) {
        super(builder);
        CrinkleMod.EVENT_BUS.register(this);
    }

    private static @NotNull String getSpriteId(Undergarment undergarment) {
        double liquidPct = undergarment.getLiquidsPercent();
        double solidsPct = undergarment.getSolidsPercent();
        String spriteId = "clean";
        if (liquidPct >= VISIBLY_WET_PERCENT && solidsPct >= VISIBLY_MESSY_PERCENT) {
            spriteId = "both";
        } else if (solidsPct >= VISIBLY_MESSY_PERCENT) {
            spriteId = "messy";
        } else if (liquidPct >= VISIBLY_WET_PERCENT) {
            spriteId = "wet";
        }
        return spriteId;
    }

    @Override
    public void renderContent(ThemeGraphics graphics, Point pMouse, Rect renderedRect, float pPartialTick) {
        if (ClientUtil.getPlayer() instanceof LocalPlayer) {
            if (isFinished()) {
                setUndergarmentAnimation();
            }
        }
        super.renderContent(graphics, pMouse, renderedRect, pPartialTick);
    }

    private void setUndergarmentAnimation() {
        if (ClientUtil.getPlayer() instanceof LocalPlayer player) {
            if (isFinished()) {
                Undergarment undergarment = Undergarment.of(Undergarment.getWornUndergarment(player));
                double speed = undergarment.isLeaking() ? 2.0 : 1.0;
                String spriteId = getSpriteId(undergarment);

                // Hard-coded for now, but get animationId from the item NBT in the future
                if (!trySetAnimations(speed, "diaper", spriteId)) return;
                onFinished(this::setUndergarmentAnimation);
            }
        }
    }

    @Override
    public AbstractWidget visualCopy(AbstractContainer newParent) {
        UndergarmentWidget copy = new UndergarmentWidget(newParent);
        copy.copyVisualProperties(this);
        return copy;
    }

}
