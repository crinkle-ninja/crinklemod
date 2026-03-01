package ninja.crinkle.mod.client.gui.editors;

import ninja.crinkle.mod.client.gui.managers.IManagedGUI;
import ninja.crinkle.mod.client.gui.properties.Rect;
import ninja.crinkle.mod.client.gui.properties.ScreenRegion;
import ninja.crinkle.mod.client.gui.screens.AbstractScreen;
import ninja.crinkle.mod.client.gui.widgets.AbstractWidget;
import ninja.crinkle.mod.config.ClientConfig;

import ninja.crinkle.mod.client.gui.widgets.AbstractContainer;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class LayoutRegistry {

    public record Entry(String id,
                        IManagedGUI gui,
                        Function<AbstractContainer, AbstractWidget> editorWidgetFactory,
                        Supplier<Rect> currentRect,
                        Consumer<Rect> onApply) {}

    private static final Map<String, Entry> entries = new LinkedHashMap<>();

    public static void register(Entry entry) {
        entries.put(entry.id(), entry);
    }

    public static Collection<Entry> entries() {
        return Collections.unmodifiableCollection(entries.values());
    }

    public static Collection<Entry> entries(IManagedGUI gui) {
        return entries.values().stream().filter(entry -> entry.gui() == gui).toList();
    }

    public static Optional<Rect> resolvePosition(String id, int screenW, int screenH, int widgetW, int widgetH) {
        return ClientConfig.layout().getPosition(id)
                .map(pos -> pos.resolve(screenW, screenH, widgetW, widgetH));
    }

    public static void savePosition(String id, Rect rect, int screenW, int screenH) {
        ScreenRegion.AnchoredPosition pos = ScreenRegion.fromAbsolute(
                rect.x(), rect.y(), rect.width(), rect.height(), screenW, screenH);
        ClientConfig.layout().setPosition(id, pos);
        ClientConfig.layout().save();
    }
}
