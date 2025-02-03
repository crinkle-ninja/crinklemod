package ninja.crinkle.mod.client.gui.widgets;

import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import ninja.crinkle.mod.CrinkleMod;
import ninja.crinkle.mod.client.animations.Animation;
import ninja.crinkle.mod.client.animations.BubbleSpriteGroup;
import ninja.crinkle.mod.client.animations.CharacterSpriteGroup;
import ninja.crinkle.mod.client.gui.states.references.ValueRef;
import ninja.crinkle.mod.events.AccidentEvent;
import ninja.crinkle.mod.events.CrinkleEvent;
import ninja.crinkle.mod.events.DesperationEvent;
import ninja.crinkle.mod.metabolism.Metabolism;
import ninja.crinkle.mod.util.ClientUtil;

public class MetabolismWidget extends AnimatedWidget {
    private final ValueRef<Metabolism.DesperationLevel> numberOne = manager().stateStorage()
            .createValue(Metabolism.DesperationLevel.class, Metabolism.DesperationLevel.NONE);
    private final ValueRef<Metabolism.DesperationLevel> numberTwo = manager().stateStorage()
            .createValue(Metabolism.DesperationLevel.class, Metabolism.DesperationLevel.NONE);
    private final ValueRef<CrinkleEvent.Type> accidentType = manager().stateStorage()
            .createValue(CrinkleEvent.Type.class, CrinkleEvent.Type.NONE);

    public MetabolismWidget(AbstractContainer parent) {
        this(AnimatedWidget.builder(parent));
    }

    public MetabolismWidget(Builder builder) {
        super(builder);
        CrinkleMod.EVENT_BUS.register(this);
        if (ClientUtil.getPlayer() instanceof LocalPlayer player) {
            numberOne.set(Metabolism.of(player).getNumberOneDesperationLevel());
            numberTwo.set(Metabolism.of(player).getNumberTwoDesperationLevel());
        }
        setDesperationAnimation();
    }

    @SubscribeEvent
    public void onAccident(AccidentEvent event) {
        AccidentEvent.Type type = event.getType();
        CrinkleEvent.Type accidentType = this.accidentType.get();
        if (accidentType != type && accidentType != CrinkleEvent.Type.BOTH && accidentType != CrinkleEvent.Type.NONE) {
            type = CrinkleEvent.Type.BOTH;
        }
        this.accidentType.set(type);
        Animation animation = Animation.builder()
                .addSpriteGroups(CharacterSpriteGroup.ACCIDENT, BubbleSpriteGroup.forType(type))
                .speed(4.0f)
                .onFinished(() -> animation(Animation.builder()
                        .onFinished(() -> {
                            this.accidentType.set(CrinkleEvent.Type.NONE);
                            numberOne.set(Metabolism.of(event.getPlayer()).getNumberOneDesperationLevel());
                            numberTwo.set(Metabolism.of(event.getPlayer()).getNumberTwoDesperationLevel());
                            setDesperationAnimation();
                        })
                        .addSpriteGroups(CharacterSpriteGroup.RELIEF, BubbleSpriteGroup.NORMAL)
                        .speed(0.75f)
                        .build()))
                .build();
        animation(animation);
    }

    private Metabolism.DesperationLevel maxLevel() {
        return Metabolism.DesperationLevel.max(numberOne.get(), numberTwo.get());
    }

    private CrinkleEvent.Type maxType() {
        if (maxLevel() == Metabolism.DesperationLevel.NONE) {
            return CrinkleEvent.Type.NONE;
        }
        if (numberOne.get() != Metabolism.DesperationLevel.NONE
                && numberTwo.get() != Metabolism.DesperationLevel.NONE) {
            return CrinkleEvent.Type.BOTH;
        }
        return maxLevel() == numberOne.get() ? CrinkleEvent.Type.BLADDER : CrinkleEvent.Type.BOWEL;
    }

    public void setDesperationAnimation() {
        CharacterSpriteGroup character = CharacterSpriteGroup.forLevel(maxLevel());
        BubbleSpriteGroup bubble = BubbleSpriteGroup.forType(maxType());
        float speed = 1.0f;
        if (maxLevel().getLevel() > 1) {
            speed = maxLevel().getLevel() * 2f;
        }
        if (character != null && bubble != null) {
            Animation animation = Animation.builder()
                    .onFinished(this::setDesperationAnimation)
                    .addSpriteGroups(character, bubble)
                    .speed(speed)
                    .build();
            animation(animation);
        }
    }

    @SubscribeEvent
    public void onDesperation(DesperationEvent event) {
        switch(event.getType()) {
            case BLADDER -> numberOne.set(event.getLevel());
            case BOWEL -> numberTwo.set(event.getLevel());
        }
        // Don't interrupt an accident!
        if (accidentType.get() != CrinkleEvent.Type.NONE) return;
        setDesperationAnimation();
    }
}
