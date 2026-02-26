package ninja.crinkle.mod.client.gui.layouts;

import ninja.crinkle.mod.client.gui.properties.*;
import ninja.crinkle.mod.client.gui.renderers.ThemeGraphics;
import ninja.crinkle.mod.client.gui.states.Positioning;

public interface BoxModel {
    Position position();
    Positioning positioning();
    Margin margin();
    Padding padding();
    Border border();
    Size size();
}
