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
        MarginContainer panel = MarginContainer.builder(root())
                .margins(8)
                .pushAndReturn();
        CenterContainer centerContainer = CenterContainer.builder(panel).pushAndReturn();
        VBoxContainer buttonPanel = VBoxContainer.builder(centerContainer)
                .pushAndReturn();
        Button.builder(buttonPanel)
                .text("Test 1")
                .widgetTheme("button")
                .onClick((e, w) -> LOGGER.debug("Click 1!"))
                .push();
        Button.builder(buttonPanel)
                .text("Test 2")
                .widgetTheme("button")
                .onClick((e, w) -> LOGGER.debug("Click 2!"))
                .push();
    }
}
