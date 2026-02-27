package ninja.crinkle.mod.client.gui.addons;

import com.mojang.logging.LogUtils;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraftforge.client.event.ScreenEvent;
import ninja.crinkle.mod.client.gui.properties.Position;
import ninja.crinkle.mod.client.gui.properties.Size;
import ninja.crinkle.mod.client.gui.screens.TestScreen;
import ninja.crinkle.mod.client.gui.widgets.Button;
import ninja.crinkle.mod.client.gui.widgets.HBoxContainer;
import ninja.crinkle.mod.util.ClientUtil;
import org.slf4j.Logger;

public class PauseScreenAddOn extends AbstractAddOn {
    private static final Logger LOGGER = LogUtils.getLogger();

    public PauseScreenAddOn() {
        super(PauseScreen.class);
        HBoxContainer hBoxContainer = new HBoxContainer.Builder(this.root()).build();
        Button testButton = Button.builder(root())
                .text("Test Button")
                .widgetTheme("button")
                .position(Position.absolute(100, 100))
                .size(Size.ofPixels(100, 20))
                .visible(true)
                .onClick((e, w) -> ClientUtil.getMinecraft().setScreen(new TestScreen()))
                .build();
        hBoxContainer.add(testButton);
        root().add(hBoxContainer);
    }

    @Override
    public void onScreenRenderPost(ScreenEvent.Render.Post event) {
        super.onScreenRenderPost(event);
        if (event.isCanceled()) return;
        if (!root().active() || !root().visible()) return;
        if (ClientUtil.getMinecraft() == null || ClientUtil.getMinecraft().screen == null) return;
    }
}
