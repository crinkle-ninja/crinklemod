package ninja.crinkle.mod.client.gui.events;

import ninja.crinkle.mod.client.gui.events.sources.EventSource;
import ninja.crinkle.mod.client.gui.properties.Point;
import ninja.crinkle.mod.client.gui.properties.Scope;

public abstract class MouseEvent extends InputEvent {
    private final Button button;
    private final double x;
    private final double y;

    public MouseEvent(Type type, Scope scope, EventSource source, double x, double y, int button) {
        super(type, scope, source);
        this.x = x;
        this.y = y;
        this.button = Button.fromInt(button);
    }

    public Button button() {
        return button;
    }

    public boolean isLeftButton() {
        return button == Button.LEFT;
    }

    public boolean isMiddleButton() {
        return button == Button.MIDDLE;
    }

    public boolean isRightButton() {
        return button == Button.RIGHT;
    }

    public Point position() {
        return new Point(x, y);
    }

    @Override
    public String toString() {
        return "MouseEvent{" +
                "x=" + x +
                ", y=" + y +
                ", button=" + button +
                ", " + super.toString() +
                '}';
    }

    public double x() {
        return x;
    }

    public double y() {
        return y;
    }

    public enum Button {
        NONE(-1),
        LEFT(0),
        RIGHT(1),
        MIDDLE(2);
        private final int button;

        Button(int button) {
            this.button = button;
        }

        public int button() {
            return button;
        }

        public static Button fromInt(int button) {
            for (Button value : values()) {
                if (value.button == button) {
                    return value;
                }
            }
            return NONE;
        }
    }


}
