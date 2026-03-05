package ninja.crinkle.mod.client.gui.screens.definition;

import ninja.crinkle.mod.client.gui.screens.binding.StepperBinder;

import java.util.ArrayList;
import java.util.List;

public sealed interface WidgetNode {

    String name();
    String style();
    int[] minSize();
    String[] horizontalSizing();
    String[] verticalSizing();
    Float stretchRatio();
    Boolean draggable();
    Boolean visible();

    default List<WidgetNode> childNodes() {
        return List.of();
    }

    record LabelNode(
            String name, String style, String text, String color, String role,
            int[] minSize, String[] horizontalSizing, String[] verticalSizing,
            Float stretchRatio, Boolean draggable, Boolean visible
    ) implements WidgetNode {}

    record ButtonNode(
            String name, String style, String text, String role,
            int[] minSize, String[] horizontalSizing, String[] verticalSizing,
            Float stretchRatio, Boolean draggable, Boolean visible
    ) implements WidgetNode {}

    record TextBoxNode(
            String name, String style, String text,
            int[] minSize, String[] horizontalSizing, String[] verticalSizing,
            Float stretchRatio, Boolean draggable, Boolean visible
    ) implements WidgetNode {}

    record VBoxNode(
            String name, String style, Integer separation,
            int[] minSize, String[] horizontalSizing, String[] verticalSizing,
            Float stretchRatio, Boolean draggable, Boolean visible,
            List<WidgetNode> children
    ) implements WidgetNode {
        @Override public List<WidgetNode> childNodes() { return children; }
    }

    record HBoxNode(
            String name, String style, Integer separation,
            int[] minSize, String[] horizontalSizing, String[] verticalSizing,
            Float stretchRatio, Boolean draggable, Boolean visible,
            List<WidgetNode> children
    ) implements WidgetNode {
        @Override public List<WidgetNode> childNodes() { return children; }
    }

    record MarginNode(
            String name, String style, Integer margins,
            int[] minSize, String[] horizontalSizing, String[] verticalSizing,
            Float stretchRatio, Boolean draggable, Boolean visible,
            List<WidgetNode> children
    ) implements WidgetNode {
        @Override public List<WidgetNode> childNodes() { return children; }
    }

    record CenterNode(
            String name, String style,
            int[] minSize, String[] horizontalSizing, String[] verticalSizing,
            Float stretchRatio, Boolean draggable, Boolean visible,
            List<WidgetNode> children
    ) implements WidgetNode {
        @Override public List<WidgetNode> childNodes() { return children; }
    }

    record PanelNode(
            String name, String style,
            int[] minSize, String[] horizontalSizing, String[] verticalSizing,
            Float stretchRatio, Boolean draggable, Boolean visible,
            List<WidgetNode> children
    ) implements WidgetNode {
        @Override public List<WidgetNode> childNodes() { return children; }
    }

    record TabbedPanelNode(
            String name, String style,
            Integer tabWidth, Integer tabMargin, Integer contentMargin,
            int[] minSize, String[] horizontalSizing, String[] verticalSizing,
            Float stretchRatio, Boolean draggable, Boolean visible,
            List<TabNode> tabs
    ) implements WidgetNode {
        @Override
        public List<WidgetNode> childNodes() {
            List<WidgetNode> all = new ArrayList<>();
            for (TabNode tab : tabs) {
                all.addAll(tab.children());
            }
            return all;
        }
    }

    record SettingStepperNode(
            String name, String style, String setting, double step,
            StepperBinder.Format format,
            int[] minSize, String[] horizontalSizing, String[] verticalSizing,
            Float stretchRatio, Boolean draggable, Boolean visible
    ) implements WidgetNode {}

    record SettingToggleNode(
            String name, String style, String setting, List<String> dependsOn,
            int[] minSize, String[] horizontalSizing, String[] verticalSizing,
            Float stretchRatio, Boolean draggable, Boolean visible
    ) implements WidgetNode {}
}
