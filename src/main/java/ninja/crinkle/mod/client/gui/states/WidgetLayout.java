package ninja.crinkle.mod.client.gui.states;

import ninja.crinkle.mod.client.gui.builders.GenericBuilder;
import ninja.crinkle.mod.client.gui.layouts.BoxModel;
import ninja.crinkle.mod.client.gui.properties.*;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class WidgetLayout implements BoxModel, Cloneable {
    private final Border border;
    private final Margin margin;
    private final Padding padding;
    private final Position position;
    private final Positioning positioning;
    private final Size size;

    public WidgetLayout() {
        this(Position.absolute(Point.ZERO), Size.ofPixels(20, 20), Margin.ZERO, Border.ZERO, Padding.ZERO,
                Positioning.Absolute);
    }

    public WidgetLayout(@NotNull Position position, @NotNull Size size, @NotNull Margin margin,
                        @NotNull Border border, @NotNull Padding padding, @NotNull Positioning positioning) {
        if (position.absolute() != (positioning == Positioning.Absolute)) {
            throw new IllegalArgumentException("Positioning of position must match layout positioning");
        }
        this.positioning = positioning;
        this.position = position;
        this.size = size;
        this.margin = margin;
        this.border = border;
        this.padding = padding;
    }

    public static LayoutBuilder builder() {
        return new LayoutBuilder();
    }

    public LayoutBuilder toBuilder() {
        return new LayoutBuilder(this);
    }

    @Override
    public WidgetLayout clone() {
        try {
            return (WidgetLayout) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }


    public WidgetLayout(@NotNull Position position, @NotNull Size size, @NotNull Margin margin,
                        @NotNull Border border, @NotNull Padding padding) {
        this.position = position;
        this.size = size;
        this.margin = margin;
        this.border = border;
        this.padding = padding;
        this.positioning = position.absolute() ? Positioning.Absolute : Positioning.Relative;
    }

    public Border border() {
        return border;
    }

    @Override
    public int hashCode() {
        return Objects.hash(position, size, margin, border, padding, positioning);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (WidgetLayout) obj;
        return Objects.equals(this.position, that.position) &&
                Objects.equals(this.size, that.size) &&
                Objects.equals(this.margin, that.margin) &&
                Objects.equals(this.border, that.border) &&
                Objects.equals(this.padding, that.padding) &&
                Objects.equals(this.positioning, that.positioning);
    }

    @Override
    public String toString() {
        return "WidgetLayout{" +
                "position=" + position +
                ", size=" + size +
                ", margin=" + margin +
                ", border=" + border +
                ", padding=" + padding +
                '}';
    }

    public Margin margin() {
        return margin;
    }

    public Padding padding() {
        return padding;
    }

    public Position position() {
        return position;
    }

    public Size size() {
        return size;
    }

    public Positioning positioning() {
        return positioning;
    }

    public static class LayoutBuilder extends GenericBuilder<LayoutBuilder, WidgetLayout> {
        private Border border;
        private Margin margin;
        private Padding padding;
        private Position position;
        private Positioning positioning;
        private Size size;

        public LayoutBuilder() {
            this.border = Border.ZERO;
            this.margin = Margin.ZERO;
            this.padding = Padding.ZERO;
            this.position = Position.ABSOLUTE_ZERO;
            this.positioning = Positioning.Absolute;
            this.size = Size.ZERO;
        }

        public LayoutBuilder(WidgetLayout layout) {
            this.border = layout.border;
            this.margin = layout.margin;
            this.padding = layout.padding;
            this.position = layout.position;
            this.positioning = layout.positioning;
            this.size = layout.size;
        }

        public LayoutBuilder border(Border border) {
            this.border = border;
            return self();
        }

        public LayoutBuilder margin(Margin margin) {
            this.margin = margin;
            return self();
        }

        public LayoutBuilder padding(Padding padding) {
            this.padding = padding;
            return self();
        }

        public LayoutBuilder position(Position position) {
            this.position = position;
            return this.positioning(position.absolute() ? Positioning.Absolute : Positioning.Relative);
        }

        public LayoutBuilder positioning(Positioning positioning) {
            this.positioning = positioning;
            return self();
        }

        public LayoutBuilder size(Size size) {
            this.size = size;
            return self();
        }

        @Override
        public WidgetLayout build() {
            return new WidgetLayout(position, size, margin, border, padding, positioning);
        }

        @Override
        protected LayoutBuilder self() {
            return this;
        }
    }
}
