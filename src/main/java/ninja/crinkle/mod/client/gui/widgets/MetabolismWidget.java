package ninja.crinkle.mod.client.gui.widgets;

import com.mojang.logging.LogUtils;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import ninja.crinkle.mod.CrinkleMod;
import ninja.crinkle.mod.client.gui.animations.Animation;
import ninja.crinkle.mod.client.gui.properties.Point;
import ninja.crinkle.mod.client.gui.properties.Rect;
import ninja.crinkle.mod.client.gui.renderers.ThemeGraphics;
import ninja.crinkle.mod.client.gui.themes.Theme;
import ninja.crinkle.mod.client.gui.themes.ThemeRegistry;
import ninja.crinkle.mod.events.AccidentEvent;
import ninja.crinkle.mod.events.CrinkleEvent;
import ninja.crinkle.mod.events.DesperationEvent;
import ninja.crinkle.mod.metabolism.Metabolism;
import ninja.crinkle.mod.util.ClientUtil;
import org.slf4j.Logger;

public class MetabolismWidget extends AnimatedWidget {
    private static final Logger LOGGER = LogUtils.getLogger();
    private Metabolism.DesperationLevel numberOne = Metabolism.DesperationLevel.NONE;
    private Metabolism.DesperationLevel numberTwo = Metabolism.DesperationLevel.NONE;
    private CrinkleEvent.Type accidentType = CrinkleEvent.Type.NONE;

    public MetabolismWidget(AbstractContainer parent) {
        this(new AnimatedWidget.Builder(parent));
        name("metabolism_widget");
    }

    @Override
    public AbstractWidget visualCopy(AbstractContainer newParent) {
        MetabolismWidget copy = new MetabolismWidget(newParent);
        copy.copyVisualProperties(this);
        return copy;
    }

    public MetabolismWidget(Builder builder) {
        super(builder);
        CrinkleMod.EVENT_BUS.register(this);
        if (ClientUtil.getPlayer() instanceof LocalPlayer player) {
            numberOne = Metabolism.of(player).getNumberOneDesperationLevel();
            numberTwo = Metabolism.of(player).getNumberTwoDesperationLevel();
        }
    }

    @Override
    public void renderContent(ThemeGraphics graphics, Point pMouse, Rect renderedRect, float pPartialTick) {
        if (isFinished()) {
            setDesperationAnimation();
        }
        super.renderContent(graphics, pMouse, renderedRect, pPartialTick);
    }

    @SubscribeEvent
    public void onAccident(AccidentEvent event) {
        AccidentEvent.Type type = event.getType();
        CrinkleEvent.Type accidentType = this.accidentType;
        if (accidentType != type && accidentType != CrinkleEvent.Type.BOTH && accidentType != CrinkleEvent.Type.NONE) {
            type = CrinkleEvent.Type.BOTH;
        }
        this.accidentType = type;

        if (!trySetAnimations(4.0f, "accident", typeSpriteId(type))) return;

        onFinished(() -> {
            this.accidentType = CrinkleEvent.Type.NONE;
            trySetAnimations(0.75f, "relief", "normal");
            onFinished(() -> {
                numberOne = Metabolism.of(event.getPlayer()).getNumberOneDesperationLevel();
                numberTwo = Metabolism.of(event.getPlayer()).getNumberTwoDesperationLevel();
                setDesperationAnimation();
            });
        });
    }

    private boolean trySetAnimations(double speed, String characterSprite, String bubbleSprite) {
        Theme theme = ThemeRegistry.current();
        if (theme == null) return false;

        Animation character = theme.animation("character").orElse(null);
        Animation bubble = theme.animation("bubble").orElse(null);
        if (character == null) {
            LOGGER.error("character is null for theme {}", theme.id());
            return false;
        }
        if (bubble == null) {
            LOGGER.error("bubble is null for theme {}", theme.id());
            return false;
        }
        clearPlayer();
        fps(speed);
        animation(character, characterSprite);
        animation(bubble, bubbleSprite);
        return true;
    }

    private Metabolism.DesperationLevel maxLevel() {
        return Metabolism.DesperationLevel.max(numberOne, numberTwo);
    }

    private CrinkleEvent.Type maxType() {
        if (maxLevel() == Metabolism.DesperationLevel.NONE) return CrinkleEvent.Type.NONE;
        if (numberOne != Metabolism.DesperationLevel.NONE
                && numberTwo != Metabolism.DesperationLevel.NONE) {
            return CrinkleEvent.Type.BOTH;
        }
        return maxLevel() == numberOne ? CrinkleEvent.Type.BLADDER : CrinkleEvent.Type.BOWEL;
    }

    private String desperationSpriteId(Metabolism.DesperationLevel level) {
        return switch (level) {
            case NONE, LOW -> "normal";
            case MEDIUM_LOW, MEDIUM -> "desperate";
            case MEDIUM_HIGH, HIGH -> "very_desperate";
        };
    }

    private String typeSpriteId(CrinkleEvent.Type type) {
        return switch (type) {
            case NONE -> "normal";
            case BLADDER, LIQUIDS -> "wet";
            case BOWEL, SOLIDS -> "messy";
            case BOTH -> "both";
        };
    }

    public void setDesperationAnimation() {
        double speed = maxLevel().getLevel() > 1 ? maxLevel().getLevel() * 2.0 : 1.0;
        if (!trySetAnimations(speed, desperationSpriteId(maxLevel()), typeSpriteId(maxType()))) return;
        onFinished(this::setDesperationAnimation);
    }

    @SubscribeEvent
    public void onDesperation(DesperationEvent event) {
        switch(event.getType()) {
            case BLADDER -> numberOne = event.getLevel();
            case BOWEL -> numberTwo = event.getLevel();
        }
        if (accidentType != CrinkleEvent.Type.NONE) return;
        setDesperationAnimation();
    }
}
