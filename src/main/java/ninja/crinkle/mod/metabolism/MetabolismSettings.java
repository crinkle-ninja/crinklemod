package ninja.crinkle.mod.metabolism;

import net.minecraft.network.chat.Component;
import ninja.crinkle.mod.settings.Setting;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class MetabolismSettings {
    // Default Values
    public static final double DEFAULT_INTENSITY = 0.65;
    public static final double DEFAULT_INTERVAL_DECAY = 0.2;
    public static final int DEFAULT_INTERVAL = 300;
    public static final double DEFAULT_CONTROL = 1.0;
    public static final double DEFAULT_CONTROL_DECAY = -0.0008;

    public static final MetabolismSettings WET = new MetabolismSettings(Profile.of(Metabolism.Type.Wet)
            .control(0.6)
            .interval(400)
            .controlDecay(-0.0009)
            .intervalDecay(0.5)
            .intensity(0.5)
            .enabled(false));
    public static final MetabolismSettings MESS = new MetabolismSettings(Profile.of(Metabolism.Type.Mess)
            .control(0.75)
            .interval(600)
            .controlDecay(-0.0007)
            .intervalDecay(0.5)
            .intensity(0.5)
            .enabled(false));

    private final Component label;
    private final Metabolism.Type type;
    private final Setting<Boolean> enabled;
    private final Setting<Double> control;
    private final Setting<Integer> interval;
    private final Setting<Double> controlDecay;
    private final Setting<Double> intervalDecay;
    private final Setting<Double> intensity;
    private final Setting<String> pang;
    private final Setting<Double> currentControl;
    private final Setting<Double> currentInterval;
    private final Setting<Integer> pangDuration;
    private final Setting<Integer> tickCount;
    private final Map<String, Setting<?>> registry = new HashMap<>();

    public static class Profile {
        private final Metabolism.Type type;
        private boolean enabled;
        private double control;
        private int interval;
        private double controlDecay;
        private double intervalDecay;
        private double intensity;

        Profile(Metabolism.Type type) {
            this.type = type;
            this.enabled = false;
            this.control = DEFAULT_CONTROL;
            this.interval = DEFAULT_INTERVAL;
            this.controlDecay = DEFAULT_CONTROL_DECAY;
            this.intervalDecay = DEFAULT_INTERVAL_DECAY;
            this.intensity = DEFAULT_INTENSITY;
        }

        public static Profile of(Metabolism.Type type) {
            return new Profile(type);
        }

        public Profile enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public Profile control(double control) {
            this.control = control;
            return this;
        }

        public Profile interval(int interval) {
            this.interval = interval;
            return this;
        }

        public Profile controlDecay(double controlDecay) {
            this.controlDecay = controlDecay;
            return this;
        }

        public Profile intervalDecay(double intervalDecay) {
            this.intervalDecay = intervalDecay;
            return this;
        }

        public Profile intensity(double intensity) {
            this.intensity = intensity;
            return this;
        }

        public double control() {
            return control;
        }

        public double controlDecay() {
            return controlDecay;
        }

        public boolean enabled() {
            return enabled;
        }

        public double intensity() {
            return intensity;
        }

        public int interval() {
            return interval;
        }

        public double intervalDecay() {
            return intervalDecay;
        }

        public Metabolism.Type type() {
            return type;
        }
    }

    public MetabolismSettings(Profile profile) {
        this.type = profile.type();
        this.label = Component.translatable("setting.crinklemod.metabolism.%s.label".formatted(type()));
        this.enabled = register(Setting.booleanBuilder(type() + ".enabled")
                .label(Component.translatable("setting.crinklemod.metabolism.%s.enabled.label".formatted(type())))
                .tooltip(Component.translatable("setting.crinklemod.metabolism.%s.enabled.tooltip".formatted(type())))
                .defaultValue(profile.enabled())
                .build());
        this.control = register(Setting.doubleBuilder(type() + ".control")
                .range(0.0, 1.0)
                .label(Component.translatable("setting.crinklemod.metabolism.%s.control.label".formatted(type())))
                .tooltip(Component.translatable("setting.crinklemod.metabolism.%s.control.tooltip".formatted(type())))
                .numberFormat("#.###")
                .defaultValue(profile.control())
                .modifierValue(Setting.ModifierValue.Ctrl, 0.1)
                .modifierValue(Setting.ModifierValue.Shift, 0.05)
                .modifierValue(Setting.ModifierValue.None, 0.01)
                .build());
        this.interval = register(Setting.intBuilder(type() + ".interval")
                .range(10, 3600)
                .label(Component.translatable("setting.crinklemod.metabolism.%s.interval.label".formatted(type())))
                .tooltip(Component.translatable("setting.crinklemod.metabolism.%s.interval.tooltip".formatted(type())))
                .defaultValue(profile.interval())
                .modifierValue(Setting.ModifierValue.Alt, 100)
                .modifierValue(Setting.ModifierValue.Ctrl, 10)
                .modifierValue(Setting.ModifierValue.Shift, 5)
                .modifierValue(Setting.ModifierValue.None, 1)
                .build());
        this.controlDecay = register(Setting.doubleBuilder(type() + ".controlDecay")
                .range(-1.0, 0.0)
                .label(Component.translatable("setting.crinklemod.metabolism.%s.controlDecay.label".formatted(type())))
                .tooltip(Component.translatable("setting.crinklemod.metabolism.%s.controlDecay.tooltip".formatted(type())))
                .numberFormat("#.#####")
                .defaultValue(profile.controlDecay())
                .modifierValue(Setting.ModifierValue.Ctrl, -0.001)
                .modifierValue(Setting.ModifierValue.Shift, -0.0005)
                .modifierValue(Setting.ModifierValue.None, -0.0001)
                .build());
        this.intervalDecay = register(Setting.doubleBuilder(type() + ".intervalDecay")
                .range(0.0, 1.0)
                .label(Component.translatable("setting.crinklemod.metabolism.%s.intervalDecay.label".formatted(type())))
                .tooltip(Component.translatable("setting.crinklemod.metabolism.%s.intervalDecay.tooltip".formatted(type())))
                .numberFormat("#.###")
                .defaultValue(profile.intervalDecay())
                .modifierValue(Setting.ModifierValue.Ctrl, 0.01)
                .modifierValue(Setting.ModifierValue.Shift, 0.005)
                .modifierValue(Setting.ModifierValue.None, 0.001)
                .build());
        this.intensity = register(Setting.doubleBuilder(type() + ".intensity")
                .range(0.0, 1.0)
                .label(Component.translatable("setting.crinklemod.metabolism.%s.intensity.label".formatted(type())))
                .tooltip(Component.translatable("setting.crinklemod.metabolism.%s.intensity.tooltip".formatted(type())))
                .numberFormat("#.###")
                .defaultValue(profile.intensity())
                .modifierValue(Setting.ModifierValue.Ctrl, 0.1)
                .modifierValue(Setting.ModifierValue.Shift, 0.05)
                .modifierValue(Setting.ModifierValue.None, 0.01)
                .build());
        this.currentInterval = register(Setting.doubleBuilder(type() + ".currentInterval")
                .range(1.0, 3600.0)
                .label(Component.translatable("setting.crinklemod.metabolism.%s.currentInterval.label".formatted(type())))
                .tooltip(Component.translatable("setting.crinklemod.metabolism.%s.currentInterval.tooltip".formatted(type())))
                .defaultValue(Double.valueOf(interval().getDefault()))
                .build());
        this.pang = register(Setting.stringBuilder(type() + ".pang")
                .label(Component.translatable("setting.crinklemod.metabolism.%s.pang.label".formatted(type())))
                .tooltip(Component.translatable("setting.crinklemod.metabolism.%s.pang.tooltip".formatted(type())))
                .defaultValue("None")
                .build());
        this.pangDuration = register(Setting.intBuilder(type() + ".pangDuration")
                .range(0, 3600)
                .label(Component.translatable("setting.crinklemod.metabolism.%s.pangDuration.label".formatted(type())))
                .tooltip(Component.translatable("setting.crinklemod.metabolism.%s.pangDuration.tooltip".formatted(type())))
                .defaultValue(0)
                .build());
        this.currentControl = register(Setting.doubleBuilder(type() + ".currentControl")
                .range(0.0, 1.0)
                .label(Component.translatable("setting.crinklemod.metabolism.%s.currentControl.label".formatted(type())))
                .tooltip(Component.translatable("setting.crinklemod.metabolism.%s.currentControl.tooltip".formatted(type())))
                .numberFormat("#.###")
                .defaultValue(control().getDefault())
                .build());
        this.tickCount = register(Setting.intBuilder(type() + ".tickCount")
                .label(Component.translatable("setting.crinklemod.metabolism.%s.tickCount.label".formatted(type())))
                .tooltip(Component.translatable("setting.crinklemod.metabolism.%s.tickCount.tooltip".formatted(type())))
                .defaultValue(0)
                .build());
    }

    public static MetabolismSettings of(Metabolism.Type type) {
        return switch (type) {
            case Wet -> WET;
            case Mess -> MESS;
        };
    }

    private <T extends Comparable<? super T>> Setting<T> register(Setting<T> setting) {
        registry.put(setting.key(), setting);
        return setting;
    }

    public @Nullable Setting<?> byKey(String key) {
        return registry.get(key);
    }

    public Setting<String> pang() {
        return pang;
    }

    public Setting<Double> currentInterval() {
        return currentInterval;
    }

    public Setting<Double> currentControl() {
        return currentControl;
    }

    public Setting<Boolean> enabled() {
        return enabled;
    }

    public Setting<Double> intervalDecay() {
        return intervalDecay;
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

    public Setting<Double> control() {
        return control;
    }

    public Setting<Double> controlDecay() {
        return controlDecay;
    }

    public Setting<Integer> interval() {
        return interval;
    }

    public Metabolism.Type type() {
        return type;
    }
}
