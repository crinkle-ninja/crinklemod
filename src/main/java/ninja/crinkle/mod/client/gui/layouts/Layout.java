package ninja.crinkle.mod.client.gui.layouts;

import ninja.crinkle.mod.client.gui.widgets.AbstractContainer;

public interface Layout {
    static AbstractLayout.AbstractBuilder<Horizontal.Builder> horizontal() {
        return Horizontal.builder();
    }

    static AbstractLayout.AbstractBuilder<Vertical.Builder> vertical() {
        return Vertical.builder();
    }

    Alignment alignment();

    void arrange(AbstractContainer container);

    int spacing();

    enum Alignment {TOP, RIGHT, BOTTOM, LEFT, CENTER}

}
