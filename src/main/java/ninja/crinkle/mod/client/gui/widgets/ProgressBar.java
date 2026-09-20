package ninja.crinkle.mod.client.gui.widgets;

import ninja.crinkle.mod.client.color.Color;
import ninja.crinkle.mod.client.gui.properties.Point;
import ninja.crinkle.mod.client.gui.properties.Rect;
import ninja.crinkle.mod.client.gui.renderers.ThemeGraphics;
import org.jetbrains.annotations.NotNull;

public class ProgressBar extends AbstractWidget {
    private static final int DEFAULT_HEIGHT = 9;
    private Color backgroundColor;
    private Color fillColor;
    private double maxValue;
    private boolean showPercent;
    private double value;

    protected ProgressBar(@NotNull Builder builder) {
        super(builder);
        this.value = builder.value();
        this.maxValue = builder.maxValue();
        this.fillColor = builder.fillColor();
        this.backgroundColor = builder.backgroundColor();
        this.showPercent = builder.showPercent();
    }

    public double value() {
        return value;
    }

    public void value(double value) {
        this.value = value;
    }

    public double maxValue() {
        return maxValue;
    }

    public void maxValue(double maxValue) {
        this.maxValue = maxValue;
    }

    public double progress() {
        if (maxValue <= 0) return 0;
        return Math.min(1.0, Math.max(0.0, value / maxValue));
    }

    public Color fillColor() {
        return fillColor;
    }

    public void fillColor(Color fillColor) {
        this.fillColor = fillColor;
    }

    public Color backgroundColor() {
        return backgroundColor;
    }

    public void backgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public boolean showPercent() {
        return showPercent;
    }

    public void showPercent(boolean showPercent) {
        this.showPercent = showPercent;
    }

    @Override
    public int minimumHeight() {
        int explicit = super.minimumHeight();
        return explicit > 0 ? explicit : DEFAULT_HEIGHT;
    }

    @Override
    public int minimumWidth() {
        int explicit = super.minimumWidth();
        return explicit > 0 ? explicit : 60;
    }

    @Override
    public void renderContent(ThemeGraphics graphics, Point pMouse, Rect renderedRect, float pPartialTick) {
        double pct = progress();

        // Background (always drawn)
        graphics.fill(renderedRect, backgroundColor, zIndex());

        // Fill (only the proportional part)
        if (pct > 0 && renderedRect.width() > 0) {
            int fillWidth = (int) Math.round(renderedRect.width() * pct);
            fillWidth = Math.min(fillWidth, renderedRect.width());
            if (fillWidth > 0) {
                Rect fillRect = new Rect(renderedRect.x(), renderedRect.y(), fillWidth, renderedRect.height());
                graphics.fill(fillRect, fillColor, zIndex() + 1);
            }
        }

        // Percent text with shadow
        if (showPercent) {
            String text = String.format("%d%%", Math.round(pct * 100));
            int textWidth = graphics.textWidth(text);
            int textHeight = graphics.textHeight();
            int tx = renderedRect.x() + (renderedRect.width() - textWidth) / 2;
            int ty = renderedRect.y() + (renderedRect.height() - textHeight) / 2;
            graphics.text(text, Point.of(tx, ty), zIndex() + 2, Color.WHITE, Color.BLACK);
        }
    }

    @Override
    public AbstractWidget visualCopy(AbstractContainer newParent) {
        ProgressBar copy = new ProgressBar.Builder(newParent)
                .value(value).maxValue(maxValue)
                .fillColor(fillColor).backgroundColor(backgroundColor)
                .showPercent(showPercent)
                .build();
        copy.copyVisualProperties(this);
        return copy;
    }

    public static class Builder extends AbstractBuilder<Builder> {
        private double value = 0;
        private double maxValue = 1;
        private Color fillColor = Color.GREEN;
        private Color backgroundColor = Color.LIGHT_GRAY;
        private boolean showPercent = false;

        public Builder(AbstractContainer parent) {
            super(parent);
            active(true);
        }

        @Override
        public ProgressBar build() {
            return new ProgressBar(this);
        }

        @Override
        public AbstractContainer push() {
            return parent().add(this);
        }

        @Override
        protected Builder self() {
            return this;
        }

        public ProgressBar pushAndReturn() {
            ProgressBar bar = new ProgressBar(this);
            parent().add(bar);
            return bar;
        }

        public Builder value(double value) {
            this.value = value;
            return self();
        }

        public double value() {
            return value;
        }

        public Builder maxValue(double maxValue) {
            this.maxValue = maxValue;
            return self();
        }

        public double maxValue() {
            return maxValue;
        }

        public Builder fillColor(Color fillColor) {
            this.fillColor = fillColor;
            return self();
        }

        public Color fillColor() {
            return fillColor;
        }

        public Builder backgroundColor(Color backgroundColor) {
            this.backgroundColor = backgroundColor;
            return self();
        }

        public Color backgroundColor() {
            return backgroundColor;
        }

        public Builder showPercent(boolean showPercent) {
            this.showPercent = showPercent;
            return self();
        }

        public boolean showPercent() {
            return showPercent;
        }
    }
}
