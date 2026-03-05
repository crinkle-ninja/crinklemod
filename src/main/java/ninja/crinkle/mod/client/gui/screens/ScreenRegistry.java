package ninja.crinkle.mod.client.gui.screens;

import ninja.crinkle.mod.client.gui.screens.definition.ScreenDefinition;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public enum ScreenRegistry {
    INSTANCE;

    private final Map<String, ScreenDefinition> screens = new HashMap<>();

    public void register(ScreenDefinition definition) {
        screens.put(definition.id(), definition);
    }

    public Optional<ScreenDefinition> get(String id) {
        return Optional.ofNullable(screens.get(id));
    }

    public Map<String, ScreenDefinition> screens() {
        return screens;
    }
}
