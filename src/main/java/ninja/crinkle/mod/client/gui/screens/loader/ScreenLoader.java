package ninja.crinkle.mod.client.gui.screens.loader;

import com.google.gson.Gson;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import ninja.crinkle.mod.client.gui.screens.binding.SettingRegistry;
import org.apache.commons.compress.utils.Lists;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ScreenLoader {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String SCREENS_PATH = "screens";
    private static final Gson GSON = new Gson();

    private static final Set<String> WIDGET_TYPES = Set.of(
            "label", "button", "textbox",
            "vbox", "hbox", "center", "margin", "panel",
            "tabbed_panel", "setting_stepper", "setting_toggle"
    );

    private static final Set<String> VALID_FORMATS = Set.of("integer", "percent", "decimal");

    public static Map<ResourceLocation, Config> loadConfigs(ResourceManager resourceManager) {
        Map<ResourceLocation, Config> screens = new HashMap<>();
        resourceManager.listResources(SCREENS_PATH, (path) -> path.getPath().endsWith(".json")
                        && !path.getPath().split("/")[1].startsWith("schema"))
                .forEach((location, resource) -> {
                    try {
                        Config config = GSON.fromJson(resource.openAsReader(), Config.class);
                        if (screens.values().stream().anyMatch(c ->
                                c.screen().id().equals(config.screen().id()))) {
                            throw new IllegalStateException("Duplicate screen id: " + config.screen().id()
                                    + ", path: " + location);
                        }
                        screens.put(location, config);
                    } catch (Exception e) {
                        LOGGER.error("Failed to load screen from {}", location, e);
                    }
                });
        return screens;
    }

    public record Config(int version, ScreenData screen) {
        public record Error(String message) {
        }

        public List<Error> validate() {
            List<Error> errors = Lists.newArrayList();
            if (screen == null) {
                errors.add(new Error("Screen data is missing."));
                return errors;
            }
            if (version != 1) {
                errors.add(new Error("Invalid screen version: " + version));
            }
            if (screen.id() == null || screen.id().isEmpty()) {
                errors.add(new Error("Screen id is missing or empty."));
            }
            if (screen.title() == null || screen.title().isEmpty()) {
                errors.add(new Error("Screen title is missing or empty."));
            }
            if (screen.root() == null) {
                errors.add(new Error("Screen root widget is missing."));
            } else {
                errors.addAll(validateWidget(screen.root(), "root"));
            }
            return errors;
        }

        private List<Error> validateWidget(WidgetData widget, String path) {
            List<Error> errors = Lists.newArrayList();
            if (widget.type() == null || widget.type().isEmpty()) {
                errors.add(new Error(path + ": Widget type is missing."));
                return errors;
            }
            if (!WIDGET_TYPES.contains(widget.type())) {
                errors.add(new Error(path + ": Unknown widget type '" + widget.type() + "'."));
            }
            if ("setting_stepper".equals(widget.type())) {
                if (widget.setting() == null || widget.setting().isEmpty()) {
                    errors.add(new Error(path + ": setting_stepper requires 'setting' field."));
                }
                if (widget.format() != null && !VALID_FORMATS.contains(widget.format())) {
                    errors.add(new Error(path + ": Invalid format '" + widget.format() + "'."));
                }
            }
            if ("setting_toggle".equals(widget.type())) {
                if (widget.setting() == null || widget.setting().isEmpty()) {
                    errors.add(new Error(path + ": setting_toggle requires 'setting' field."));
                }
            }
            if ("tabbed_panel".equals(widget.type())) {
                if (widget.tabs() == null || widget.tabs().isEmpty()) {
                    errors.add(new Error(path + ": tabbed_panel requires at least one tab."));
                } else {
                    for (int i = 0; i < widget.tabs().size(); i++) {
                        TabData tab = widget.tabs().get(i);
                        String tabPath = path + ".tabs[" + i + "]";
                        if (tab.id() == null || tab.id().isEmpty()) {
                            errors.add(new Error(tabPath + ": Tab id is missing."));
                        }
                        if (tab.children() != null) {
                            for (int j = 0; j < tab.children().size(); j++) {
                                errors.addAll(validateWidget(tab.children().get(j),
                                        tabPath + ".children[" + j + "]"));
                            }
                        }
                    }
                }
            }
            if (widget.children() != null) {
                for (int i = 0; i < widget.children().size(); i++) {
                    errors.addAll(validateWidget(widget.children().get(i),
                            path + ".children[" + i + "]"));
                }
            }
            return errors;
        }
    }
}
