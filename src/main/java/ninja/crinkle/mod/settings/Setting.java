package ninja.crinkle.mod.settings;

import net.minecraft.network.chat.Component;
import org.apache.commons.lang3.NotImplementedException;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Predicate;

public abstract class Setting<T extends Comparable<? super T>> {
    protected final T defaultValue;
    protected final String key;
    protected final Component label;
    protected final RangeSupplier<T> rangeSupplier;
    protected final Component tooltip;
    protected final Class<T> type;
    protected final String numberFormat;
    protected final Map<ModifierValue, T> modifierValues = new HashMap<>();
    protected final Map<Predicate<T>, BiFunction<Setting<T>, T, Component>> validators;

    protected Setting(String key, Component label, Component tooltip, Class<T> type,
                      Map<Predicate<T>, BiFunction<Setting<T>, T, Component>> validators,
                      T defaultValue, RangeSupplier<T> rangeSupplier, String numberFormat,
                      Map<ModifierValue, T> modifierValues) {
        this.key = key;
        this.type = type;
        this.label = label;
        this.tooltip = tooltip;
        this.validators = validators;
        this.defaultValue = defaultValue;
        this.rangeSupplier = rangeSupplier;
        this.numberFormat = numberFormat;
        this.modifierValues.putAll(modifierValues);
    }

    @Contract("_ -> new")
    @SuppressWarnings("unused")
    public static Setting.@NotNull BooleanValueBuilder booleanBuilder(String key) {
        return new BooleanValueBuilder(key);
    }

    @Contract("_ -> new")
    public static Setting.@NotNull DoubleValueBuilder doubleBuilder(String key) {
        return new DoubleValueBuilder(key);
    }

    @Contract("_ -> new")
    public static Setting.@NotNull IntValueBuilder intBuilder(String key) {
        return new IntValueBuilder(key);
    }

    @Contract("_ -> new")
    @SuppressWarnings("unused")
    public static Setting.@NotNull StringValueBuilder stringBuilder(String key) {
        return new StringValueBuilder(key);
    }

    public String displayName() {
        return label() != null ? label().getString() : key();
    }

    public List<Component> errors(Object value) {
        if (!isValid(value)) {
            if (isInt()) {
                return List.of(Component.translatable("validation.crinklemod.metabolism.failure.invalid.integer",
                        value, displayName()));
            }
            if (isDouble()) {
                return List.of(Component.translatable("validation.crinklemod.metabolism.failure.invalid.double",
                        value, displayName()));
            }
            if (isBoolean()) {
                return List.of(Component.translatable("validation.crinklemod.metabolism.failure.invalid.boolean",
                        value, displayName()));
            }
            return List.of(Component.translatable("validation.crinklemod.metabolism.failure.invalid.general",
                    value, displayName()));
        }
        List<Component> errors = new ArrayList<>();
        validators.forEach((k, v) -> {
            if (!k.test(valueOf(value.toString()))) {
                errors.add(v.apply(this, valueOf(value.toString())));
            }
        });
        return errors;
    }

    public boolean isValid(Object value) {
        if (value == null) return false;
        if (isBoolean() && (value.toString().equals("true") || value.toString().equals("false"))) return true;
        if (isInt() || isDouble()) {
            try {
                T v = valueOf(value.toString());
                for(var entry : validators.entrySet()) {
                    if (!entry.getKey().test(v))
                        return false;
                }
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        }
        // String
        return true;
    }

    public String formattedString(Object value) {
        if (numberFormat().isBlank() || value == null) {
            return String.valueOf(value);
        }
        if (isDouble() || isInt() || !String.valueOf(value).isBlank()) {
            return new DecimalFormat(numberFormat()).format(value);
        }
        return value.toString();
    }

    public abstract T modifierValue(List<ModifierValue> modifierValues);

    private String numberFormat() {
        return numberFormat;
    }

    public boolean isInt() {
        return false;
    }

    public boolean isDouble() {
        return false;
    }

    public boolean isBoolean() {
        return false;
    }

    public abstract T add(T value, T modifier);

    public abstract T subtract(T value, T modifier);

    public abstract T valueOf(Object value);

    public T getDefault() {
        return defaultValue;
    }

    public boolean isString() {
        return false;
    }

    public String key() {
        return key;
    }

    public Component label() {
        return label;
    }

    public RangeSupplier<T> range() {
        return rangeSupplier;
    }

    public Component tooltip() {
        return tooltip;
    }

    public static class BooleanValue extends Setting<Boolean> {
        protected BooleanValue(String key, Component label, Component tooltip,
                               Map<Predicate<Boolean>, BiFunction<Setting<Boolean>, Boolean, Component>> validators,
                               Boolean defaultValue, RangeSupplier<Boolean> rangeSupplier) {
            super(key, label, tooltip, Boolean.class, validators, defaultValue,
                    rangeSupplier, "", Map.of());
        }

        public static boolean of(Object value) {
            return Boolean.parseBoolean(String.valueOf(value));
        }

        @Override
        public Boolean modifierValue(List<ModifierValue> modifierValues) {
            throw new NotImplementedException("modifierValue is not implemented on BooleanValue");
        }

        @Override
        public boolean isBoolean() {
            return true;
        }

        @Override
        public Boolean valueOf(Object value) {
            if (value instanceof Boolean)
                return (Boolean) value;
            if (value instanceof String v)
                return Boolean.valueOf(v);
            throw new IllegalArgumentException("invalid boolean type given, must be boolean or string");
        }

        @Override
        public Boolean add(Boolean value, Boolean modifier) {
            throw new IllegalArgumentException("add called on boolean value");
        }

        @Override
        public Boolean subtract(Boolean value, Boolean modifier) {
            throw new IllegalArgumentException("subtract called on boolean value");
        }
    }

    public static class BooleanValueBuilder extends Builder<Boolean> {
        private BooleanValueBuilder(String key) {
            super(key);
        }

        public BooleanValue build() {
            if (rangeSupplier != null)
                throw new IllegalStateException("Boolean values cannot have a rangeSupplier");
            return new BooleanValue(key, label, tooltip, validators, defaultValue,
                    null);
        }
    }

    public static abstract class Builder<T extends Comparable<? super T>> {
        protected final String key;
        protected final Map<Predicate<T>, BiFunction<Setting<T>, T, Component>> validators = new HashMap<>();
        protected T defaultValue;
        protected RangeSupplier<T> rangeSupplier;
        protected Component label;
        protected Component tooltip;
        protected String numberFormat = "";
        protected Map<ModifierValue, T> modifierValues = new HashMap<>();

        private Builder(String key) {
            this.key = key;
        }

        public abstract Setting<T> build();

        public Builder<T> defaultValue(T value) {
            this.defaultValue = value;
            return this;
        }

        public Builder<T> label(Component label) {
            this.label = label;
            return this;
        }

        public Builder<T> numberFormat(String format) {
            this.numberFormat = format;
            return this;
        }

        public Builder<T> range(T min, T max) {
            return this.range(new RangeSupplier<>(min, max));
        }

        public Builder<T> range(RangeSupplier<T> rangeSupplier) {
            this.rangeSupplier = rangeSupplier;
            return this;
        }

        public Builder<T> tooltip(Component tooltip) {
            this.tooltip = tooltip;
            return this;
        }

        @SuppressWarnings("UnusedReturnValue")
        public Builder<T> validator(Predicate<T> validator, BiFunction<Setting<T>, T, Component> messageSupplier) {
            validators.put(validator, messageSupplier);
            return this;
        }

        public Builder<T> modifierValue(ModifierValue modifier, T value) {
            modifierValues.put(modifier, value);
            return this;
        }
    }

    public static class DoubleValue extends Setting<Double> {
        protected DoubleValue(String key, Component label, Component tooltip,
                              Map<Predicate<Double>, BiFunction<Setting<Double>, Double, Component>> validators,
                              Double defaultValue, RangeSupplier<Double> rangeSupplier, String numberFormat,
                              Map<ModifierValue, Double> modifierValues) {
            super(key, label, tooltip, Double.class, validators, defaultValue,
                    rangeSupplier, numberFormat, modifierValues
            );
        }

        public static double of(Object value) {
            return Double.parseDouble(String.valueOf(value));
        }

        @Override
        public Double modifierValue(List<ModifierValue> modifierValues) {
            List<ModifierValue> validModifiers = modifierValues.stream()
                    .filter(v -> this.modifierValues.get(v) != null).toList();
            if (validModifiers.isEmpty() || validModifiers.contains(ModifierValue.None)) {
                return this.modifierValues.getOrDefault(ModifierValue.None, 0.01);
            }
            return validModifiers.stream()
                    .mapToDouble(this.modifierValues::get)
                    .sum();
        }

        @Override
        public boolean isDouble() {
            return true;
        }

        @Override
        public Double add(Double value, Double modifier) {
            return value + modifier;
        }

        @Override
        public Double subtract(Double value, Double modifier) {
            return value - modifier;
        }

        @Override
        public Double valueOf(Object value) {
            if (value instanceof Double d)
                return d;
            if (value instanceof Integer i)
                return (double) i;
            if (value instanceof String v)
                return Double.valueOf(v);
            throw new IllegalArgumentException("invalid double type given, must be double, integer, or string");
        }
    }

    public static class DoubleValueBuilder extends Builder<Double> {

        private String numberFormat;

        private DoubleValueBuilder(String key) {
            super(key);
            modifierValues.put(ModifierValue.None, 1.0);
        }

        public Builder<Double> numberFormat(String format) {
            numberFormat = format;
            return this;
        }

        public DoubleValue build() {
            if (rangeSupplier != null) {
                validator((v) -> rangeSupplier.apply().contains(v), (s,v) -> {
                    RangeSupplier.Result<Double> range = rangeSupplier.apply();
                    return Component.translatable("validation.crinklemod.metabolism.failure.out_of_range.double", v,
                            s.displayName(), range.min(), range.max());
                });
            }
            return new DoubleValue(key, label, tooltip, validators, defaultValue,
                    rangeSupplier, numberFormat, modifierValues);
        }
    }

    public static class IntValue extends Setting<Integer> {
        protected IntValue(String key, Component label, Component tooltip,
                           Map<Predicate<Integer>, BiFunction<Setting<Integer>, Integer, Component>> validators,
                           Integer defaultValue, RangeSupplier<Integer> rangeSupplier, String numberFormat,
                           Map<ModifierValue, Integer> modifierValues) {
            super(key, label, tooltip, Integer.class, validators, defaultValue,
                    rangeSupplier, numberFormat, modifierValues
            );
        }

        public static int of(Object value) {
            return Integer.parseInt(String.valueOf(value));
        }

        @Override
        public boolean isInt() {
            return true;
        }

        @Override
        public Integer add(Integer value, Integer modifier) {
            return value + modifier;
        }

        @Override
        public Integer subtract(Integer value, Integer modifier) {
            return value - modifier;
        }

        @Override
        public Integer modifierValue(List<ModifierValue> modifierValues) {
            List<ModifierValue> validModifiers = modifierValues.stream()
                    .filter(v -> this.modifierValues.get(v) != null).toList();
            if (validModifiers.isEmpty() || validModifiers.contains(ModifierValue.None)) {
                return this.modifierValues.getOrDefault(ModifierValue.None, 1);
            }
            return validModifiers.stream()
                    .mapToInt(this.modifierValues::get)
                    .sum();
        }

        @Override
        public Integer valueOf(Object value) {
            if (value instanceof Integer i)
                return i;
            if (value instanceof String v) {
                Number n;
                try {
                    n = new DecimalFormat("#,###").parse(v);
                } catch (ParseException e) {
                    throw new IllegalArgumentException(e);
                }
                return n.intValue();
            }
            throw new IllegalArgumentException("invalid integer type given, must be integer, or string");
        }
    }

    public static class IntValueBuilder extends Builder<Integer> {

        private IntValueBuilder(String key) {
            super(key);
            modifierValues.put(ModifierValue.None, 1);
        }

        @Override
        public IntValue build() {
            if (rangeSupplier != null)
                validator((v) -> rangeSupplier.apply().contains(v), (s,v) -> {
                    RangeSupplier.Result<Integer> range = rangeSupplier.apply();
                    return Component.translatable("validation.crinklemod.metabolism.failure.out_of_range.integer", v,
                            s.displayName(), range.min(), range.max());
                });
            return new IntValue(key, label, tooltip, validators, defaultValue,
                    rangeSupplier, numberFormat, modifierValues
            );
        }
    }

    public static class RangeSupplier<T extends Comparable<? super T>> {
        private final T max;
        private final T min;

        public RangeSupplier(T min, T max) {
            this.min = min;
            this.max = max;
        }

        public Result<T> apply() {
            return new Result<>(min, max);
        }

        public record Result<T extends Comparable<? super T>>(T min, T max) {

            public Result<Double> asDouble() {
                return new Result<>(DoubleValue.of(min), DoubleValue.of(max));
            }

            public Result<Integer> asInt() {
                return new Result<>(IntValue.of(min), IntValue.of(max));
            }

            public boolean contains(T value) {
                return value.compareTo(min) >= 0 && value.compareTo(max) <= 0;
            }
        }
    }

    public static class StringValue extends Setting<String> {
        protected StringValue(String key, Component label, Component tooltip,
                              Map<Predicate<String>, BiFunction<Setting<String>, String, Component>> validators,
                              String defaultValue, RangeSupplier<String> rangeSupplier, String numberFormat) {
            super(key, label, tooltip, String.class, validators, defaultValue,
                    rangeSupplier, numberFormat, Map.of()
            );
        }

        public static String of(Object value) {
            return String.valueOf(value);
        }

        @Override
        public boolean isString() {
            return true;
        }

        @Override
        public String valueOf(Object value) {
            return value.toString();
        }

        @Override
        public String modifierValue(List<ModifierValue> modifierValues) {
            throw new NotImplementedException("modifierValue does not exist for StringValue");
        }

        @Override
        public String add(String value, String modifier) {
            throw new IllegalArgumentException("add called on string value");
        }

        @Override
        public String subtract(String value, String modifier) {
            throw new IllegalArgumentException("subtract called on string value");
        }
    }

    public static class StringValueBuilder extends Builder<String> {
        private StringValueBuilder(String key) {
            super(key);
        }

        public StringValue build() {
            if (rangeSupplier != null)
                throw new IllegalStateException("String values cannot have a rangeSupplier");
            return new StringValue(key, label, tooltip, validators, defaultValue,
                    null, numberFormat);
        }
    }

    public enum ModifierValue {
        None, Alt, Ctrl, Shift
    }
}
