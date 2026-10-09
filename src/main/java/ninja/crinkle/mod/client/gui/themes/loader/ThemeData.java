package ninja.crinkle.mod.client.gui.themes.loader;

import java.util.List;
import java.util.Map;

public record ThemeData(String id, String name, String namespace, String description, String version,
                        List<String> authors,
                        Map<String, String> colors, List<TextureData> textures, List<StyleData> styles,
                        List<AnimationData> animations) {
}
