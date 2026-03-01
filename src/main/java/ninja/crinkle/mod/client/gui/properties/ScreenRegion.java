package ninja.crinkle.mod.client.gui.properties;

public enum ScreenRegion {
    TOP_LEFT,
    TOP_CENTER,
    TOP_RIGHT,
    CENTER_LEFT,
    CENTER,
    CENTER_RIGHT,
    BOTTOM_LEFT,
    BOTTOM_CENTER,
    BOTTOM_RIGHT;

    public record AnchoredPosition(ScreenRegion anchor, int offsetX, int offsetY) {
        public Rect resolve(int screenWidth, int screenHeight, int widgetWidth, int widgetHeight) {
            Point anchorPoint = anchor.resolve(screenWidth, screenHeight);
            return new Rect(anchorPoint.xInt() + offsetX, anchorPoint.yInt() + offsetY, widgetWidth, widgetHeight);
        }
    }

    public Point resolve(int screenWidth, int screenHeight) {
        return switch (this) {
            case TOP_LEFT -> Point.of(0, 0);
            case TOP_CENTER -> Point.of(screenWidth / 2, 0);
            case TOP_RIGHT -> Point.of(screenWidth, 0);
            case CENTER_LEFT -> Point.of(0, screenHeight / 2);
            case CENTER -> Point.of(screenWidth / 2, screenHeight / 2);
            case CENTER_RIGHT -> Point.of(screenWidth, screenHeight / 2);
            case BOTTOM_LEFT -> Point.of(0, screenHeight);
            case BOTTOM_CENTER -> Point.of(screenWidth / 2, screenHeight);
            case BOTTOM_RIGHT -> Point.of(screenWidth, screenHeight);
        };
    }

    public static AnchoredPosition fromAbsolute(int x, int y, int widgetWidth, int widgetHeight, int screenWidth, int screenHeight) {
        int cx = x + widgetWidth / 2;
        int cy = y + widgetHeight / 2;

        ScreenRegion nearest = TOP_LEFT;
        double minDist = Double.MAX_VALUE;

        for (ScreenRegion region : values()) {
            Point anchor = region.resolve(screenWidth, screenHeight);
            double dist = Math.pow(cx - anchor.x(), 2) + Math.pow(cy - anchor.y(), 2);
            if (dist < minDist) {
                minDist = dist;
                nearest = region;
            }
        }

        Point anchorPoint = nearest.resolve(screenWidth, screenHeight);
        int offsetX = x - anchorPoint.xInt();
        int offsetY = y - anchorPoint.yInt();
        return new AnchoredPosition(nearest, offsetX, offsetY);
    }
}
