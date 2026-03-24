package ninja.crinkle.mod.client.gui.themes.loader;

import ninja.crinkle.mod.client.gui.textures.ColorFilters;
import ninja.crinkle.mod.client.gui.themes.Style;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public record StyleData(String id, Map<Style.Variant, VariantData> variants, String parent) {
    public record VariantData(VariantData.LayerData background, VariantData.LayerData foreground) {
        public record LayerData(String texture, String color, String shadow, List<String> filters) {
            public List<ColorFilters> colorFilters() {
                return Optional.ofNullable(filters)
                        .orElse(List.of())
                        .stream()
                        .map(ColorFilters::fromString)
                        .toList();
            }
        }
    }
}
