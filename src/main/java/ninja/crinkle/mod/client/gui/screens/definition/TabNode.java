package ninja.crinkle.mod.client.gui.screens.definition;

import java.util.List;

public record TabNode(String id, String label, List<WidgetNode> children) {
}
