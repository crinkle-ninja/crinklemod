package ninja.crinkle.mod.client.textures.generators;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.logging.LogUtils;
import ninja.crinkle.mod.client.color.Color;
import ninja.crinkle.mod.client.models.DiaperArmorModel;
import ninja.crinkle.mod.config.ClientConfig;
import ninja.crinkle.mod.undergarment.DiaperDesign;
import ninja.crinkle.mod.undergarment.Undergarment;
import ninja.crinkle.mod.util.MathUtil;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoVertex;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * The DiaperTextureGenerator class is responsible for generating diaper textures based on the fullness of the diaper.
 * It implements the TextureGenerator interface and specifically generates textures for Undergarment objects.
 */
public class DiaperTextureGenerator implements TextureGenerator<Undergarment> {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Map<Integer, Color> fillColors;
    private final EnumSet<Part> parts = EnumSet.noneOf(Part.class);
    private final Function<Undergarment, Double> percentFunction;
    private final int[] fadeABGR; // pre-computed for fast pixel-loop matching

    public DiaperTextureGenerator(Function<Undergarment, Double> percentFunction, Map<Integer, Color> fillColors,
                                  Set<Color> fadeColors, List<Part> parts) {
        this.percentFunction = percentFunction;
        this.parts.addAll(parts);
        this.fillColors = fillColors;
        this.fadeABGR = fadeColors.stream().mapToInt(Color::ABGR).toArray();
    }


    public static TextureGenerator<?> buildGenerator(@NotNull DiaperDesign design) {
        if (design.overlays().isEmpty()) return null;

        TextureGenerator<Undergarment> combined = null;
        for (DiaperDesign.OverlayConfig overlay : design.overlays()) {
            Map<Integer, Color> fillColors;
            Function<Undergarment, Double> percentFn;

            switch (overlay.type()) {
                case "wet" -> {
                    fillColors = ClientConfig.wetFillColors();
                    percentFn = Undergarment::getLiquidsPercent;
                }
                case "mess" -> {
                    fillColors = ClientConfig.messFillColors();
                    percentFn = Undergarment::getSolidsPercent;
                }
                default -> {
                    LOGGER.warn("unknown overlay type: {}", overlay.type());
                    continue;
                }
            }

            final Set<Color> fadeColors = overlay.fadeColors().stream().map(Color::of).collect(Collectors.toSet());
            final List<DiaperTextureGenerator.Part> parts = overlay.parts().stream().map(Part::fromString)
                    .filter(part -> !part.equals(Part.NONE)).toList();
            DiaperTextureGenerator gen = new DiaperTextureGenerator(percentFn, fillColors, fadeColors, parts);
            combined = combined == null ? gen : combined.andThen(gen);
        }
        return combined;
    }

    @Override
    public @NotNull NativeImage apply(@NotNull NativeImage pImage, @NotNull TextureData pData) {
        Data data = (Data) pData;
        double fullness = percentFunction.apply(data.undergarment());
        int pct = (int) (Math.ceil(fullness) * 100);

        // At 0% fullness, no change — return image unchanged
        if (pct == 0 || fillColors.isEmpty()) {
            NativeImage copy = new NativeImage(pImage.getWidth(), pImage.getHeight(), true);
            copy.copyFrom(pImage);
            return copy;
        }

        // Find the nearest fill color key at or above the current percentage
        Color fillColor = fillColors.entrySet().stream()
                .filter(e -> e.getKey() >= pct)
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(fillColors.get(fillColors.keySet().stream().mapToInt(Integer::intValue).max().orElse(100)));

        double fadeT = Math.min(fullness * 2.0, 1.0); // fully faded by 50% fullness

        NativeImage image = new NativeImage(pImage.getWidth(), pImage.getHeight(), true);
        image.copyFrom(pImage);
        applyPixels(image, data, (img, x, y, original) -> {
            Color current = Color.ofABGR(img.getPixelRGBA(x, y));
            Color orig = Color.ofABGR(original.getPixelRGBA(x, y));

            // Step 1: Stain all pixels — multiply with fill color, scaled by fullness
            Color stained = current.lerp(current.multiply(fillColor), fullness);

            // Step 2: Fade — fade_color pixels dissolve into the fill color at 2x rate
            int origABGR = orig.ABGR();
            boolean isFade = false;
            for (int fc : fadeABGR) {
                if (fc == origABGR) {
                    isFade = true;
                    break;
                }
            }
            if (isFade) {
                Color result = current.lerp(fillColor, fadeT);
                img.setPixelRGBA(x, y, result.withAlpha(1.0).ABGR());
            } else {
                img.setPixelRGBA(x, y, stained.withAlpha(1.0).ABGR());
            }
        });
        return image;
    }

    @FunctionalInterface
    private interface PixelConsumer {
        void accept(NativeImage img, int x, int y, NativeImage original);
    }

    private void applyPixels(NativeImage pImage, Data pData, PixelConsumer applyFunction) {
        NativeImage original = pData.getOriginalImage();
        BakedGeoModel model = pData.model().getBakedModel(pData.model().getModelResource(null));
        parts.forEach(part -> model.getBone(part.getBone()).ifPresentOrElse(b -> b.getCubes().forEach(c ->
                List.of(c.quads()).forEach(f -> {
                    List<GeoVertex> vertices = new ArrayList<>(List.of(f.vertices()));
                    vertices.sort(Comparator.comparingDouble(GeoVertex::texU).
                            thenComparingDouble(GeoVertex::texV));
                    GeoVertex v1 = vertices.get(0);
                    GeoVertex v2 = vertices.get(vertices.size() - 1);
                    int x = (int) (v1.texU() * pImage.getWidth());
                    int y = (int) (v1.texV() * pImage.getHeight());
                    int width = (int) ((v2.texU() - v1.texU()) * pImage.getWidth());
                    int height = (int) ((v2.texV() - v1.texV()) * pImage.getHeight());
                    for (int i = 0; i < width; i++) {
                        for (int j = 0; j < height; j++) {
                            applyFunction.accept(pImage, x + i, y + j, original);
                        }
                    }
                })), () -> LOGGER.warn("Could not find bone: {}", part.getBone())));
    }

    public enum Part {
        NONE,
        FRONT,
        FRONT_BOTTOM,
        BACK,
        BACK_BOTTOM,
        BOTTOM;

        public String getBone() {
            return name().toLowerCase();
        }

        public static Part fromString(String partName) {
            try {
                return Part.valueOf(partName);
            } catch (IllegalArgumentException e) {
                LOGGER.error("part not found: {}", partName);
                return NONE;
            }
        }
    }

    public static class Data implements TextureData {
        private final String name;
        private final DiaperArmorModel model;
        private final Undergarment undergarment;
        private NativeImage originalImage;

        public Data(String name, DiaperArmorModel model, Undergarment undergarment) {
            this.name = name;
            this.model = model;
            this.undergarment = undergarment;
        }

        public String name() {
            return name;
        }

        public DiaperArmorModel model() {
            return model;
        }

        public Undergarment undergarment() {
            return undergarment;
        }

        public void setOriginalImage(NativeImage image) {
            if (this.originalImage == null) {
                this.originalImage = new NativeImage(image.getWidth(), image.getHeight(), true);
                this.originalImage.copyFrom(image);
            }
        }

        public NativeImage getOriginalImage() {
            return originalImage;
        }

        @Override
        public String getName() {
            int pctL = MathUtil.clamp(
                    MathUtil.twenties((int) (undergarment.getLiquidsPercent() * 100)), 0, 100);
            int pctS = MathUtil.clamp(
                    MathUtil.twenties((int) (undergarment.getSolidsPercent() * 100)), 0, 100);
            return String.format("%s_l%d_s%d", name.replace(':', '_')
                    .replace('/', '.'), pctL, pctS);
        }
    }
}
