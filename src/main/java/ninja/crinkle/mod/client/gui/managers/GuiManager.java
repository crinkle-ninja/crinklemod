package ninja.crinkle.mod.client.gui.managers;

import ninja.crinkle.mod.client.gui.states.references.StateStorageRef;
import ninja.crinkle.mod.client.gui.states.storages.StateStorage;
import ninja.crinkle.mod.client.gui.widgets.AbstractContainer;
import ninja.crinkle.mod.client.gui.widgets.Container;

import java.util.Optional;

public interface GuiManager {
    static GuiManager create() {
        return create(640, 480);
    }
    static GuiManager create(int width, int height) {
        return new GuiManager() {
            final EventManager eventManager = EventManager.createScreen();
            final DragManager dragManager = new DragManager(eventManager);
            final FocusManager focusManager = new FocusManager();
            final StateStorageRef stateManagerRef = StateManager.screen();
            final StateStorage stateStorage = StateManager.get(stateManagerRef);
            final Container root = Container.builder(this).absolute(0,0).name("root").size(width, height).build();

            @Override
            public DragManager dragManager() {
                return dragManager;
            }

            @Override
            public Optional<EventManager> eventManager() {
                return Optional.of(eventManager);
            }

            @Override
            public FocusManager focusManager() {
                return focusManager;
            }

            @Override
            public Container root() {
                return root;
            }

            @Override
            public StateStorageRef stateStorageRef() {
                return stateManagerRef;
            }

            @Override
            public StateStorage stateStorage() {
                return stateStorage;
            }
        };
    }

    DragManager dragManager();

    Optional<EventManager> eventManager();

    FocusManager focusManager();

    AbstractContainer root();

    StateStorageRef stateStorageRef();

    StateStorage stateStorage();
}
