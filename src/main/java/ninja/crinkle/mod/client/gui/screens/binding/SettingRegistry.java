package ninja.crinkle.mod.client.gui.screens.binding;

import ninja.crinkle.mod.settings.Setting;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class SettingRegistry {
    private static final Map<String, Setting<?>> SETTINGS = new LinkedHashMap<>();

    public static void register(Setting<?> setting) {
        SETTINGS.put(setting.key(), setting);
    }

    public static void register(String key, Setting<?> setting) {
        SETTINGS.put(key, setting);
    }

    public static Setting<?> get(String key) {
        Setting<?> setting = SETTINGS.get(key);
        if (setting == null) {
            throw new IllegalArgumentException("Unknown setting: " + key);
        }
        return setting;
    }

    public static Map<String, Setting<?>> all() {
        return Collections.unmodifiableMap(SETTINGS);
    }
}
