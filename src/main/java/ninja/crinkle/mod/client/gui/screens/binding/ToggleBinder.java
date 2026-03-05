package ninja.crinkle.mod.client.gui.screens.binding;

public class ToggleBinder {

    private final PendingSettings pending;
    private final String settingKey;

    public ToggleBinder(PendingSettings pending, String settingKey) {
        this.pending = pending;
        this.settingKey = settingKey;
    }

    public void toggle() {
        boolean current = pending.get(settingKey);
        pending.set(settingKey, !current);
    }

    public boolean value() {
        return pending.get(settingKey);
    }

    public String displayValue() {
        return value() ? "ON" : "OFF";
    }
}
