package ninja.crinkle.mod.client.gui.addons;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.network.chat.Component;
import ninja.crinkle.mod.client.gui.properties.Sizing;
import ninja.crinkle.mod.client.gui.screens.CrinkleModConfigScreen;
import ninja.crinkle.mod.client.gui.widgets.Button;
import ninja.crinkle.mod.client.gui.widgets.MarginContainer;

public class PauseScreenAddOn extends AbstractAddOn {
    public PauseScreenAddOn() {
        super(PauseScreen.class);
        MarginContainer container = new MarginContainer.Builder(root())
                .margins(2)
                .verticalSizing(Sizing.ShrinkCenter)
                .horizontalSizing(Sizing.ShrinkCenter)
                .pushAndReturn();
        new Button.Builder(container)
                .text(Component.translatable("gui.crinklemod.screen.config.title").getString())
                .margins(8)
                .onClick((w) -> Minecraft.getInstance().setScreen(new CrinkleModConfigScreen()))
                .push();
    }
}
