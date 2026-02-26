package ninja.crinkle.mod.client.gui.properties;

public interface BoxProperty {
    static BoxProperty of(int top, int right, int bottom, int left) {
        return new BoxProperty() {
            @Override
            public int top() {
                return top;
            }
            @Override
            public int bottom() {
                return bottom;
            }
            @Override
            public int left() {
                return left;
            }
            @Override
            public int right() {
                return right;
            }
        };
    }

    int top();
    int bottom();
    int left();
    int right();
}
