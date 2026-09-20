package ninja.crinkle.mod.client.gui.themes;

import com.mojang.logging.LogUtils;
import ninja.crinkle.mod.config.ClientConfig;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public enum ThemeRegistry {
    INSTANCE;

    public static final String DEFAULT_THEME_ID = "default";
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Map<String, Theme> themes = new HashMap<>();

    public static Theme current() {
        String id;
        try {
            id = ClientConfig.themeId();
        } catch (IllegalStateException e) {
            return defaultTheme();
        }
        Theme theme = INSTANCE.theme(id);
        if (theme == null) {
            LOGGER.warn("Theme {} not found, using default", id);
            theme = INSTANCE.theme(DEFAULT_THEME_ID);
        }
        if (theme == null) {
            return defaultTheme();
        }
        return theme;
    }

    public Theme theme(String id) {
        return themes.get(id);
    }

    public static Theme defaultTheme() {
        return Optional.ofNullable(INSTANCE.theme(DEFAULT_THEME_ID)).orElse(Theme.EMPTY);
    }

    public void register(Theme theme) {
        themes.put(theme.id(), theme);
    }

    public Map<String, Theme> themes() {
        return themes;
    }
}
