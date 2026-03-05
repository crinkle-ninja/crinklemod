package ninja.crinkle.mod.client.gui.screens.loader;

import java.util.List;

public record WidgetData(
        String type,
        String name,
        String style,
        String text,
        String color,
        String role,
        String setting,
        String format,
        List<String> dependsOn,
        Double step,
        int[] minSize,
        String[] horizontalSizing,
        String[] verticalSizing,
        Double stretchRatio,
        Integer separation,
        Integer margins,
        Integer tabWidth,
        Integer tabMargin,
        Integer contentMargin,
        Boolean draggable,
        Boolean visible,
        List<WidgetData> children,
        List<TabData> tabs
) {
}
