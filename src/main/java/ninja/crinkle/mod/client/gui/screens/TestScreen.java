package ninja.crinkle.mod.client.gui.screens;

import com.mojang.logging.LogUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import ninja.crinkle.mod.client.gui.layouts.SizeFlags;
import ninja.crinkle.mod.client.gui.widgets.HBoxContainer;
import ninja.crinkle.mod.client.gui.widgets.VBoxContainer;
import ninja.crinkle.mod.util.ClientUtil;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

public class TestScreen extends AbstractScreen {
    private static final Logger LOGGER = LogUtils.getLogger();

    public TestScreen() {
        super(Component.literal("Test Screen"), ClientUtil.screenWidth(), ClientUtil.screenHeight());
    }

    @Override
    public String name() {
        return "TestScreen";
    }

    @Override
    public void init() {
        VBoxContainer vPanel = VBoxContainer.builder(root())
                .name("window0")
                .minSize(300, 200)
                .separation(5)
                .widgetTheme("panel")
                .draggable(true)
                .build();
        root().add(vPanel);

        int height = ClientUtil.getMinecraft().font.lineHeight + 13;

        for (int p = 0; p < 4; p++) {
            HBoxContainer hPanel = HBoxContainer.builder(vPanel)
                    .name("container" + p)
                    .separation(5)
                    .hSizeFlags(SizeFlags.EXPAND, SizeFlags.FILL)
                    .build();
            vPanel.add(hPanel);

            int btnWidth = ClientUtil.getMinecraft().font.width("Button 00") + 12;
            String[] themes = {"button_primary", "button_secondary", "button"};
            for (int i = 0; i < 3; i++) {
                hPanel.addButton()
                        .name("button" + i + p)
                        .widgetTheme(themes[i])
                        .text("Button " + i + p)
                        .onClick((event, widget) -> LOGGER.info("Button {} clicked", widget.name()))
                        .minSize(btnWidth, height)
                        .hSizeFlags(SizeFlags.EXPAND, SizeFlags.FILL)
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
