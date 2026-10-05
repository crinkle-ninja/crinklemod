package ninja.crinkle.mod.metabolism;

import net.minecraft.network.chat.Component;
import ninja.crinkle.mod.settings.Setting;

public class MetabolismSettings {
    public static final MetabolismSettings WET = new MetabolismSettings(Metabolism.Type.Wet);
    public static final MetabolismSettings MESS = new MetabolismSettings(Metabolism.Type.Mess);
    private final Component label;
    private final Metabolism.Type type;
    private final Setting<Boolean> enabled;
    private final Setting<Double> training;
    private final Setting<Integer> ticks;
    private final Setting<Double> slopeDegradation;
    private final Setting<Double> frequencyCompression;
    private final Setting<Double> intensity;
    private final Setting<String> pang;
    private final Setting<Double> currentTraining;
    private final Setting<Double> currentFrequency;
    private final Setting<Integer> pangDuration;
    private final Setting<Integer> tickCount;

    public MetabolismSettings(Metabolism.Type type) {
        this.type = type;
        this.label = Component.translatable("setting.crinklemod.metabolism.%s.label".formatted(type()));
        this.enabled = Setting.booleanBuilder(type() + ".enabled")
                .label(Component.translatable("setting.crinklemod.metabolism.%s.enabled.label".formatted(type())))
                .tooltip(Component.translatable("setting.crinklemod.metabolism.%s.enabled.tooltip".formatted(type())))
                .defaultValue(false)
                .build();
        this.training = Setting.doubleBuilder(type() + ".training")
                .range(0.0, 1.0)
                .label(Component.translatable("setting.crinklemod.metabolism.%s.training.label".formatted(type())))
                .tooltip(Component.translatable("setting.crinklemod.metabolism.%s.training.tooltip".formatted(type())))
                .numberFormat("#.##")
                .defaultValue(0.60)
                .modifierValue(Setting.ModifierValue.Ctrl, 0.1)
                .modifierValue(Setting.ModifierValue.Shift, 0.05)
                .modifierValue(Setting.ModifierValue.None, 0.01)
                .build();
        this.ticks = Setting.intBuilder(type() + ".ticks")
                .range(10, 3600)
                .label(Component.translatable("setting.crinklemod.metabolism.%s.ticks.label".formatted(type())))
                .tooltip(Component.translatable("setting.crinklemod.metabolism.%s.ticks.tooltip".formatted(type())))
                .defaultValue(30)
                .modifierValue(Setting.ModifierValue.Alt, 100)
                .modifierValue(Setting.ModifierValue.Ctrl, 10)
                .modifierValue(Setting.ModifierValue.Shift, 5)
                .modifierValue(Setting.ModifierValue.None, 1)
                .build();
        this.slopeDegradation = Setting.doubleBuilder(type() + ".slopeDegradation")
                .range(-1.0, 0.0)
                .label(Component.translatable("setting.crinklemod.metabolism.%s.slopeDegradation.label".formatted(type())))
                .tooltip(Component.translatable("setting.crinklemod.metabolism.%s.slopeDegradation.tooltip".formatted(type())))
                .numberFormat("#.#####")
                .defaultValue(-0.0005)
                .modifierValue(Setting.ModifierValue.Ctrl, -0.001)
                .modifierValue(Setting.ModifierValue.Shift, -0.0005)
                .modifierValue(Setting.ModifierValue.None, -0.0001)
                .build();
        this.frequencyCompression = Setting.doubleBuilder(type() + ".frequencyCompression")
                .range(0.0, 1.0)
                .label(Component.translatable("setting.crinklemod.metabolism.%s.frequencyCompression.label".formatted(type())))
                .tooltip(Component.translatable("setting.crinklemod.metabolism.%s.frequencyCompression.tooltip".formatted(type())))
                .numberFormat("#.###")
                .defaultValue(0.025)
                .modifierValue(Setting.ModifierValue.Ctrl, 0.01)
                .modifierValue(Setting.ModifierValue.Shift, 0.005)
                .modifierValue(Setting.ModifierValue.None, 0.001)
                .build();
        this.intensity = Setting.doubleBuilder(type() + ".intensity")
                .range(0.0, 1.0)
                .label(Component.translatable("setting.crinklemod.metabolism.%s.intensity.label".formatted(type())))
                .tooltip(Component.translatable("setting.crinklemod.metabolism.%s.intensity.tooltip".formatted(type())))
                .numberFormat("#.##")
                .defaultValue(0.50)
                .modifierValue(Setting.ModifierValue.Ctrl, 0.1)
                .modifierValue(Setting.ModifierValue.Shift, 0.05)
                .modifierValue(Setting.ModifierValue.None, 0.01)
                .build();
        this.currentFrequency = Setting.doubleBuilder(type() + ".currentFrequency")
                .range(1.0, 3600.0)
                .label(Component.translatable("setting.crinklemod.metabolism.%s.currentFrequency.label".formatted(type())))
                .tooltip(Component.translatable("setting.crinklemod.metabolism.%s.currentFrequency.tooltip".formatted(type())))
                .defaultValue(Double.valueOf(ticks().getDefault()))
                .build();
        this.pang = Setting.stringBuilder(type() + ".pang")
                .label(Component.translatable("setting.crinklemod.metabolism.%s.pang.label".formatted(type())))
                .tooltip(Component.translatable("setting.crinklemod.metabolism.%s.pang.tooltip".formatted(type())))
                .defaultValue("None")
                .build();
        this.pangDuration = Setting.intBuilder(type() + ".pangDuration")
                .range(0, 3600)
                .label(Component.translatable("setting.crinklemod.metabolism.%s.pangDuration.label".formatted(type())))
                .tooltip(Component.translatable("setting.crinklemod.metabolism.%s.pangDuration.tooltip".formatted(type())))
                .defaultValue(0)
                .build();
        this.currentTraining = Setting.doubleBuilder(type() + ".currentTraining")
                .range(0.0, 1.0)
                .label(Component.translatable("setting.crinklemod.metabolism.%s.currentTraining.label".formatted(type())))
                .tooltip(Component.translatable("setting.crinklemod.metabolism.%s.currentTraining.tooltip".formatted(type())))
                .numberFormat("#.###")
                .defaultValue(training().getDefault())
                .build();
        this.tickCount = Setting.intBuilder(type() + ".tickCount")
                .label(Component.translatable("setting.crinklemod.metabolism.%s.tickCount.label".formatted(type())))
                .tooltip(Component.translatable("setting.crinklemod.metabolism.%s.tickCount.tooltip".formatted(type())))
                .defaultValue(0)
                .build();
    }

    public static MetabolismSettings of(Metabolism.Type type) {
        return switch (type) {
            case Wet -> WET;
            case Mess -> MESS;
        };
    }

    public Setting<String> pang() {
        return pang;
    }

    public Setting<Double> currentFrequency() {
        return currentFrequency;
    }

    public Setting<Double> currentTraining() {
        return currentTraining;
    }

    public Setting<Boolean> enabled() {
        return enabled;
    }

    public Setting<Double> frequencyCompression() {
        return frequencyCompression;
    }

    public Setting<Double> intensity() {
        return intensity;
    }

    public Component label() {
        return label;
    }

    public Setting<Integer> pangDuration() {
        return pangDuration;
    }

    public Setting<Integer> tickCount() {
        return tickCount;
    }

    public Setting<Double> training() {
        return training;
    }

    public Setting<Double> slopeDegradation() {
        return slopeDegradation;
    }

    public Setting<Integer> ticks() {
        return ticks;
    }

    public Metabolism.Type type() {
        return type;
    }
}
