package ninja.crinkle.mod.client.gui.screens.binding;

import ninja.crinkle.mod.settings.Setting;

public class StepperBinder {

    public enum Format {
        INTEGER, PERCENT, DECIMAL
    }

    private final PendingSettings pending;
    private final String settingKey;
    private final double step;
    private final Format format;

    public StepperBinder(PendingSettings pending, String settingKey, double step, Format format) {
        this.pending = pending;
        this.settingKey = settingKey;
        this.step = step;
        this.format = format;
    }

    public void increment() {
        Setting<?> setting = SettingRegistry.get(settingKey);
        if (setting.isInt()) {
            int val = pending.get(settingKey);
            int max = clampMax(setting);
            pending.set(settingKey, Math.min(max, val + (int) step));
        } else if (setting.isDouble()) {
            double val = pending.get(settingKey);
            double max = clampMaxDouble(setting);
            pending.set(settingKey, Math.min(max, val + step));
        }
    }

    public void decrement() {
        Setting<?> setting = SettingRegistry.get(settingKey);
        if (setting.isInt()) {
            int val = pending.get(settingKey);
            int min = clampMin(setting);
            pending.set(settingKey, Math.max(min, val - (int) step));
        } else if (setting.isDouble()) {
            double val = pending.get(settingKey);
            double min = clampMinDouble(setting);
            pending.set(settingKey, Math.max(min, val - step));
        }
    }

    public String displayValue() {
        Setting<?> setting = SettingRegistry.get(settingKey);
        Object val = pending.get(settingKey);
        return switch (format) {
            case INTEGER -> String.valueOf(((Number) val).intValue());
            case PERCENT -> String.format("%d%%", Math.round(((Number) val).doubleValue() * 100));
            case DECIMAL -> String.format("%.2f", ((Number) val).doubleValue());
        };
    }

    private int clampMin(Setting<?> setting) {
        if (setting.range() != null) {
            return setting.range().apply(pending.provider()).asInt().min();
        }
        return Integer.MIN_VALUE;
    }

    private int clampMax(Setting<?> setting) {
        if (setting.range() != null) {
            return setting.range().apply(pending.provider()).asInt().max();
        }
        return Integer.MAX_VALUE;
    }

    private double clampMinDouble(Setting<?> setting) {
        if (setting.range() != null) {
            return setting.range().apply(pending.provider()).asDouble().min();
        }
        return -Double.MAX_VALUE;
    }

    private double clampMaxDouble(Setting<?> setting) {
        if (setting.range() != null) {
            return setting.range().apply(pending.provider()).asDouble().max();
        }
        return Double.MAX_VALUE;
    }
}
