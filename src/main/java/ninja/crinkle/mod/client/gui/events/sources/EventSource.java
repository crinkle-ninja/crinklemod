package ninja.crinkle.mod.client.gui.events.sources;

import com.mojang.logging.LogUtils;
import ninja.crinkle.mod.client.gui.managers.EventManager;
import ninja.crinkle.mod.client.gui.properties.Scope;
import org.slf4j.Logger;

import java.util.Optional;

public interface EventSource {
    Logger LOGGER = LogUtils.getLogger();
    EventManager localManager = EventManager.createLocal();
    default Optional<EventManager> eventManager() {
        return Optional.of(localManager);
    }
    default Optional<EventManager> eventManager(Scope scope) {
        return switch (scope) {
            case Global -> Optional.of(EventManager.global());
            case Local, Screen -> eventManager()
                    .flatMap(m -> m.scope().equals(scope) ? Optional.of(m) : Optional.empty());
        };
    }
    String name();
}
