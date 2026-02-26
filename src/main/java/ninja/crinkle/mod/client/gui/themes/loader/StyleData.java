package ninja.crinkle.mod.client.gui.themes.loader;

import ninja.crinkle.mod.client.gui.textures.ColorFilters;
import ninja.crinkle.mod.client.gui.themes.Style;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public record StyleData(String id, Map<Style.Variant, Variant> variants, String parent) {
    public record Variant(Variant.Data background, Variant.Data foreground) {
        public record Data(String texture, String color, boolean shadow, List<String> filters) {
            public List<ColorFilters> getFilters() {
                return Optional.ofNullable(filters)
                        .orElse(List.of())
                        .stream()
                        .map(ColorFilters::fromString)
                        .toList();
            }
        }
    }
}
