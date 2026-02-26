package ninja.crinkle.mod.client.gui.addons;

import com.mojang.logging.LogUtils;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraftforge.client.event.ScreenEvent;
import ninja.crinkle.mod.client.gui.properties.Position;
import ninja.crinkle.mod.client.gui.properties.Size;
import ninja.crinkle.mod.client.gui.screens.TestScreen;
import ninja.crinkle.mod.client.gui.widgets.Button;
import ninja.crinkle.mod.util.ClientUtil;
import org.slf4j.Logger;

public class PauseScreenAddOn extends AbstractAddOn {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Button testButton;
    public PauseScreenAddOn() {
        super(PauseScreen.class);
        this.testButton = Button.builder(root())
                .text("Test Button")
                .widgetTheme("button")
                .position(Position.absolute(100, 100))
                .size(Size.ofPixels(100, 20))
                .visible(true)
                .onClick((e, w) -> ClientUtil.getMinecraft().setScreen(new TestScreen()))
                .build();
        this.root().add(testButton);
    }

    @Override
    public void onScreenRenderPost(ScreenEvent.Render.Post event) {
        super.onScreenRenderPost(event);
        if (event.isCanceled()) return;
        if (!root().active() || !root().visible()) return;
        if (ClientUtil.getMinecraft() == null || ClientUtil.getMinecraft().screen == null) return;
        LOGGER.info("PauseScreenAddOn rendered dimensions: {}x{}", testButton.cachedBoxes().borderBox().size().width(),
                testButton.cachedBoxes().borderBox().size().height());
    }
}
