package ninja.crinkle.mod.client.gui.screens.definition;

import ninja.crinkle.mod.client.gui.screens.binding.StepperBinder;
import ninja.crinkle.mod.client.gui.screens.loader.ScreenData;
import ninja.crinkle.mod.client.gui.screens.loader.TabData;
import ninja.crinkle.mod.client.gui.screens.loader.WidgetData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public record ScreenDefinition(String id, String title, WidgetNode root, Set<String> settingKeys) {

    public static ScreenDefinition fromConfig(ScreenData data) {
        Set<String> keys = new LinkedHashSet<>();
        WidgetNode root = convertWidget(data.root(), keys);
        return new ScreenDefinition(data.id(), data.title(), root, Collections.unmodifiableSet(keys));
    }

    private static WidgetNode convertWidget(WidgetData w, Set<String> settingKeys) {
        return switch (w.type()) {
            case "label" -> new WidgetNode.LabelNode(
                    w.name(), w.style(), w.text(), w.color(), w.role(),
                    w.minSize(), w.horizontalSizing(), w.verticalSizing(),
                    toFloat(w.stretchRatio()), w.draggable(), w.visible());
            case "button" -> new WidgetNode.ButtonNode(
                    w.name(), w.style(), w.text(), w.role(),
                    w.minSize(), w.horizontalSizing(), w.verticalSizing(),
                    toFloat(w.stretchRatio()), w.draggable(), w.visible());
            case "textbox" -> new WidgetNode.TextBoxNode(
                    w.name(), w.style(), w.text(),
                    w.minSize(), w.horizontalSizing(), w.verticalSizing(),
                    toFloat(w.stretchRatio()), w.draggable(), w.visible());
            case "vbox" -> new WidgetNode.VBoxNode(
                    w.name(), w.style(), w.separation(),
                    w.minSize(), w.horizontalSizing(), w.verticalSizing(),
                    toFloat(w.stretchRatio()), w.draggable(), w.visible(),
                    convertChildren(w.children(), settingKeys));
            case "hbox" -> new WidgetNode.HBoxNode(
                    w.name(), w.style(), w.separation(),
                    w.minSize(), w.horizontalSizing(), w.verticalSizing(),
                    toFloat(w.stretchRatio()), w.draggable(), w.visible(),
                    convertChildren(w.children(), settingKeys));
            case "margin" -> new WidgetNode.MarginNode(
                    w.name(), w.style(), w.margins(),
                    w.minSize(), w.horizontalSizing(), w.verticalSizing(),
                    toFloat(w.stretchRatio()), w.draggable(), w.visible(),
                    convertChildren(w.children(), settingKeys));
            case "center" -> new WidgetNode.CenterNode(
                    w.name(), w.style(),
                    w.minSize(), w.horizontalSizing(), w.verticalSizing(),
                    toFloat(w.stretchRatio()), w.draggable(), w.visible(),
                    convertChildren(w.children(), settingKeys));
            case "panel" -> new WidgetNode.PanelNode(
                    w.name(), w.style(),
                    w.minSize(), w.horizontalSizing(), w.verticalSizing(),
                    toFloat(w.stretchRatio()), w.draggable(), w.visible(),
                    convertChildren(w.children(), settingKeys));
            case "tabbed_panel" -> new WidgetNode.TabbedPanelNode(
                    w.name(), w.style(),
                    w.tabWidth(), w.tabMargin(), w.contentMargin(),
                    w.minSize(), w.horizontalSizing(), w.verticalSizing(),
                    toFloat(w.stretchRatio()), w.draggable(), w.visible(),
                    convertTabs(w.tabs(), settingKeys));
            case "setting_stepper" -> {
                settingKeys.add(w.setting());
                yield new WidgetNode.SettingStepperNode(
                        w.name(), w.style(), w.setting(),
                        w.step() != null ? w.step() : 1,
                        parseFormat(w.format()),
                        w.minSize(), w.horizontalSizing(), w.verticalSizing(),
                        toFloat(w.stretchRatio()), w.draggable(), w.visible());
            }
            case "setting_toggle" -> {
                settingKeys.add(w.setting());
                yield new WidgetNode.SettingToggleNode(
                        w.name(), w.style(), w.setting(), w.dependsOn(),
                        w.minSize(), w.horizontalSizing(), w.verticalSizing(),
                        toFloat(w.stretchRatio()), w.draggable(), w.visible());
            }
            default -> throw new IllegalArgumentException("Unknown widget type: " + w.type());
        };
    }

    private static List<WidgetNode> convertChildren(List<WidgetData> children, Set<String> settingKeys) {
        if (children == null) return List.of();
        List<WidgetNode> nodes = new ArrayList<>();
        for (WidgetData child : children) {
            nodes.add(convertWidget(child, settingKeys));
        }
        return List.copyOf(nodes);
    }

    private static List<TabNode> convertTabs(List<TabData> tabs, Set<String> settingKeys) {
        if (tabs == null) return List.of();
        List<TabNode> nodes = new ArrayList<>();
        for (TabData tab : tabs) {
            nodes.add(new TabNode(tab.id(), tab.label(), convertChildren(tab.children(), settingKeys)));
        }
        return List.copyOf(nodes);
    }

    private static Float toFloat(Double d) {
        return d != null ? d.floatValue() : null;
    }

    private static StepperBinder.Format parseFormat(String format) {
        if (format == null) return StepperBinder.Format.INTEGER;
        return switch (format.toLowerCase()) {
            case "percent" -> StepperBinder.Format.PERCENT;
            case "decimal" -> StepperBinder.Format.DECIMAL;
            default -> StepperBinder.Format.INTEGER;
        };
    }
}
