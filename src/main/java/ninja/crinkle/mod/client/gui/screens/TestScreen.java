package ninja.crinkle.mod.client.gui.screens;

import com.mojang.logging.LogUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import ninja.crinkle.mod.client.gui.layouts.Layout;
import ninja.crinkle.mod.client.gui.properties.ImmutablePoint;
import ninja.crinkle.mod.client.gui.widgets.AbstractContainer;
import ninja.crinkle.mod.util.ClientUtil;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

public class TestScreen extends AbstractScreen {
    private static final Logger LOGGER = LogUtils.getLogger();

    public TestScreen() {
        super(Component.literal("Test Screen"), ClientUtil.screenSize());
    }

    @Override
    public String name() {
        return "TestScreen";
    }

    @Override
    public void init() {
        AbstractContainer vPanel = root().addContainer()
                .name("window0")
                .relative(20, 20)
                .size(300, 200)
                .widgetTheme("panel")
                .layoutManager(Layout.vertical().alignment(Layout.Alignment.CENTER).spacing(5))
                .margin(10)
                .padding(10)
                .draggable(true)
                .pushAndReturn();

//        int height = ClientUtil.getMinecraft().font.lineHeight + 13;
//        int textboxHeight = ClientUtil.getMinecraft().font.lineHeight + 11;
//        vPanel.addContainer()
//                .name("textboxContainer0")
//                .layoutManager(Layout.horizontal().alignment(Layout.Alignment.CENTER).spacing(5))
//                .size(vPanel.cachedBoxes().contentBox().size().widthInt(), textboxHeight)
//                .pushAndReturn()
//                .addTextBox()
//                .name("textbox0")
//                .placeholder("Enter text here")
//                .text("Diapers R COOL")
//                .padding(1)
//                .widgetTheme("textbox")
//                .size(vPanel.cachedBoxes().contentBox().size().widthInt(), textboxHeight)
//                .push();

        for (int p = 0; p < 4; p++) {
            AbstractContainer hPanel = vPanel.addContainer()
                    .name("container" + p)
                    .layoutManager(Layout.horizontal().alignment(Layout.Alignment.CENTER).spacing(5))
                    .size(vPanel.cachedBoxes().contentBox().size().widthInt(), height)
                    .pushAndReturn();

            int width = ClientUtil.getMinecraft().font.width("Button 00") + 12;
            String[] themes = {"button_primary", "button_secondary", "button"};
            for (int i = 0; i < 3; i++) {
                hPanel.addButton()
                        .name("button" + i + p)
                        .widgetTheme(themes[i])
                        .text("Button " + i + p)
                        .onClick((event, widget) -> LOGGER.info("Button {} clicked", widget.name()))
                        .relative(ImmutablePoint.ZERO)
                        .margin(2)
                        .padding(3, 3, 3, 3)
                        .size(width, height)
                        .pushAndReturn();
            }
        }
        super.init();
    }

    @Override
    public void render(@NotNull GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        renderBackground(pGuiGraphics);
        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
    }

    @Override
    public void renderBackground(GuiGraphics pGuiGraphics) {
        super.renderBackground(pGuiGraphics);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}