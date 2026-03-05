package ninja.crinkle.mod.client.gui.screens.binding;

import net.minecraftforge.common.capabilities.ICapabilityProvider;
import ninja.crinkle.mod.api.ServerUpdater;
import ninja.crinkle.mod.settings.Setting;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class PendingSettings {
    private final Map<String, Object> pending = new LinkedHashMap<>();
    private final Set<String> settingKeys;
    private final ICapabilityProvider provider;

    public PendingSettings(Set<String> settingKeys, ICapabilityProvider provider) {
        this.settingKeys = settingKeys;
        this.provider = provider;
        load();
    }

    public void load() {
        pending.clear();
        for (String key : settingKeys) {
            Setting<?> setting = SettingRegistry.get(key);
            pending.put(key, setting.get(provider));
        }
    }

    public boolean isDirty() {
        for (String key : settingKeys) {
            Setting<?> setting = SettingRegistry.get(key);
            Object current = setting.get(provider);
            Object pend = pending.get(key);
            if (current instanceof Double cd && pend instanceof Double pd) {
                if (Math.abs(cd - pd) > 0.001) return true;
            } else if (!java.util.Objects.equals(current, pend)) {
                return true;
            }
        }
        return false;
    }

    public void save() {
        for (String key : settingKeys) {
            Setting<?> setting = SettingRegistry.get(key);
            Object value = pending.get(key);
            setting.set(provider, value);
            setting.syncer(provider).ifPresent(ServerUpdater::syncServer);
        }
    }

    public void reset() {
        for (String key : settingKeys) {
            Setting<?> setting = SettingRegistry.get(key);
            pending.put(key, setting.getDefault(provider));
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        return (T) pending.get(key);
    }

    public void set(String key, Object value) {
        pending.put(key, value);
    }

    public ICapabilityProvider provider() {
        return provider;
    }
}
