package ninja.crinkle.mod.client.gui.states;

import ninja.crinkle.mod.client.gui.properties.Box;
import ninja.crinkle.mod.client.gui.properties.Size;
import ninja.crinkle.mod.client.gui.widgets.AbstractWidget;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public record CalculatedBoxes(Box borderBox, Box paddingBox, Box backgroundBox, Box contentBox) {
    public CalculatedBoxes {
        List<String> validations = new ArrayList<>();
        if (borderBox == null)
            validations.add("Border box is null");
        if (paddingBox == null)
            validations.add("Padding box is null");
        if (backgroundBox == null)
            validations.add("Background box is null");
        if (contentBox == null)
            validations.add("Content box is null");
        if (!validations.isEmpty())
            throw new IllegalArgumentException(String.join(", ", validations));

        Map<String, Box> boxes = Map.of(
                "borderBox", borderBox,
                "paddingBox", paddingBox,
                "backgroundBox", backgroundBox,
                "contentBox", contentBox);
        for (Map.Entry<String, Box> entry : boxes.entrySet()) {
            Box box = entry.getValue();
            if (box == null) {
                validations.add("Box " + entry.getKey() + " is null");
                continue;
            }

            if (box.position() == null)
                validations.add("Box " + entry.getKey() + " has no position");
            else if (!box.position().absolute())
                validations.add("Box " + entry.getKey() + " is not absolute");

            if (box.size() == null)
                validations.add("Box " + entry.getKey() + " has no size");
            else if (box.size().unit() != Size.Unit.Pixels)
                validations.add("Box " + entry.getKey() + " is not in pixels");
        }
        if (!validations.isEmpty())
            throw new IllegalArgumentException("Invalid boxes: " + String.join(", ", validations));
    }

    public static CalculatedBoxes calculate(AbstractWidget widget, Box borderBox) {
        WidgetLayout layout = widget.layout();
        if (borderBox.position().relative()) {
            throw new IllegalStateException("Cannot resolve boxes from a base box with a relative position");
        }
        Box baseBox = borderBox.clone();
        Box backgroundBox = baseBox.shrink(layout.border());
        Box paddingBox = backgroundBox.shrink(widget.textureBorder());
        Box contentBox = paddingBox.shrink(layout.padding());
        return new CalculatedBoxes(borderBox, backgroundBox, paddingBox, contentBox);
    }
}
