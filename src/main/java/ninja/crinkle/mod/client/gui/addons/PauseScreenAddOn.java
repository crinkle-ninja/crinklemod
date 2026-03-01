package ninja.crinkle.mod.client.gui.addons;

import com.mojang.logging.LogUtils;
import net.minecraft.client.gui.screens.PauseScreen;
import ninja.crinkle.mod.client.gui.properties.Sizing;
import ninja.crinkle.mod.client.gui.widgets.Button;
import ninja.crinkle.mod.client.gui.widgets.CenterContainer;
import ninja.crinkle.mod.client.gui.widgets.MarginContainer;
import ninja.crinkle.mod.client.gui.widgets.VBoxContainer;
import org.slf4j.Logger;

public class PauseScreenAddOn extends AbstractAddOn {
    private static final Logger LOGGER = LogUtils.getLogger();

    public PauseScreenAddOn() {
        super(PauseScreen.class);
        MarginContainer panel = new MarginContainer.Builder(root())
                .name("test_buttons")
                .margins(8)
                .horizontalSizing(Sizing.ShrinkBegin)
                .verticalSizing(Sizing.ShrinkBegin)
                .pushAndReturn();
        CenterContainer centerContainer = new CenterContainer.Builder(panel).pushAndReturn();
        VBoxContainer buttonPanel = new VBoxContainer.Builder(centerContainer)
                .pushAndReturn();
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
