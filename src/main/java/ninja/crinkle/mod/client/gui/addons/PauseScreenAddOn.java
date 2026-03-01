package ninja.crinkle.mod.client.gui.addons;

import com.mojang.logging.LogUtils;
import net.minecraft.client.gui.screens.PauseScreen;
import ninja.crinkle.mod.client.gui.layouts.SizeFlags;
import ninja.crinkle.mod.client.gui.screens.TestScreen;
import ninja.crinkle.mod.client.gui.widgets.Button;
import ninja.crinkle.mod.client.gui.widgets.CenterContainer;
import ninja.crinkle.mod.client.gui.widgets.MarginContainer;
import ninja.crinkle.mod.client.gui.widgets.VBoxContainer;
import ninja.crinkle.mod.util.ClientUtil;
import org.slf4j.Logger;

public class PauseScreenAddOn extends AbstractAddOn {
    private static final Logger LOGGER = LogUtils.getLogger();

    public PauseScreenAddOn() {
        super(PauseScreen.class);
        // I want a menu anchored in the top-left with 8 pixel margins around a centered Vbox of two stacked buttons.
        MarginContainer panel = new MarginContainer.Builder(root())
                .margins(8)
                .pushAndReturn();
        CenterContainer centerContainer = new CenterContainer.Builder(panel).pushAndReturn();
        VBoxContainer buttonPanel = new VBoxContainer.Builder(centerContainer)
                .pushAndReturn();
        // Buttons should shrink to fit their children.
        new Button.Builder(buttonPanel)
                .text("Test 1")
                .style("button")
                .onClick((e, w) -> LOGGER.debug("Click 1!"))
                .push();
        new Button.Builder(buttonPanel)
                .text("Test 2")
                .style("button")
                .onClick((e, w) -> LOGGER.debug("Click 2!"))
                .push();
    }
}
