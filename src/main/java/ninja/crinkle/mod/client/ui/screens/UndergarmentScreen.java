package ninja.crinkle.mod.client.ui.screens;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import ninja.crinkle.mod.CrinkleMod;
import ninja.crinkle.mod.client.ClientHooks;
import ninja.crinkle.mod.client.ui.menus.AbstractMenu;
import ninja.crinkle.mod.client.ui.menus.ConfigMenu;
import ninja.crinkle.mod.client.ui.menus.status.StatusBarEntry;
import ninja.crinkle.mod.client.ui.menus.status.StatusMenu;
import ninja.crinkle.mod.undergarment.Undergarment;
import ninja.crinkle.mod.undergarment.UndergarmentSettings;
import ninja.crinkle.mod.util.ColorUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;

public class UndergarmentScreen extends Screen {
    private static final Component TITLE = Component.translatable("gui.crinklemod.undergarment_screen.title");
    private static final Component LIQUIDS_MENU_TITLE = Component.translatable("gui.crinklemod.undergarment_screen.liquids_menu.title");
    private static final Component SOLIDS_MENU_TITLE = Component.translatable("gui.crinklemod.undergarment_screen.solids_menu.title");
    private static final ResourceLocation BACKGROUND_TEXTURE = new ResourceLocation(CrinkleMod.MODID, "textures/gui/metabolism_configuration_background.png");
    private final int imageWidth;
    private final int imageHeight;
    private int leftPos;
    private int topPos;

    private AbstractMenu mainMenu;
    private AbstractMenu currentMenu;
    private ConfigMenu<ItemStack> liquidsMenu;
    private ConfigMenu<ItemStack> solidsMenu;

    public UndergarmentScreen() {
        super(TITLE);

        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void setCurrentMenu(AbstractMenu menu) {
        if (currentMenu != null) {
            currentMenu.setVisible(false);
        }
        currentMenu = menu;
        currentMenu.setVisible(true);
    }

    @Override
    protected void init() {
        super.init();

        this.leftPos = (this.width - this.imageWidth) / 2;
        this.topPos = (this.height - this.imageHeight) / 2;

        Optional.ofNullable(DistExecutor.safeCallWhenOn(Dist.CLIENT, () -> ClientHooks::getMinecraft))
                .ifPresent(minecraft -> {
                    mainMenu = StatusMenu.builder(this)
                            .title(TITLE)
                            .font(font)
                            .leftPos(leftPos)
                            .topPos(topPos)
                            .lineHeight(15)
                            .lineSpacing(5)
                            .spacer(4)
                            .entry(StatusBarEntry.intBuilder(() -> Undergarment.getWornUndergarment(Objects.requireNonNull(minecraft.player)))
                                    .lineNumber(1)
                                    .setting(UndergarmentSettings.LIQUIDS)
                                    .onPress((menu) -> setCurrentMenu(liquidsMenu))
                                    .gradientStartColor(Undergarment.LIQUIDS_COLOR)
                                    .gradientEndColor(ColorUtil.darken(Undergarment.LIQUIDS_COLOR, 0.5f))
                                    .gradientBackgroundColor(ColorUtil.darken(Undergarment.LIQUIDS_COLOR, 0.25f))
                                    .build())
                            .entry(StatusBarEntry.intBuilder(() -> Undergarment.getWornUndergarment(Objects.requireNonNull(minecraft.player)))
                                    .lineNumber(2)
                                    .setting(UndergarmentSettings.SOLIDS)
                                    .onPress((menu) -> setCurrentMenu(solidsMenu))
                                    .gradientStartColor(Undergarment.SOLIDS_COLOR)
                                    .gradientEndColor(ColorUtil.darken(Undergarment.SOLIDS_COLOR, 0.5f))
                                    .gradientBackgroundColor(ColorUtil.darken(Undergarment.SOLIDS_COLOR, 0.25f))
                                    .build())
                            .build();
                    mainMenu.visitChildren(this::addRenderableWidget);
                    setCurrentMenu(mainMenu);
                    liquidsMenu = ConfigMenu.builder(this, font, LIQUIDS_MENU_TITLE, () -> Undergarment.getWornUndergarment(Objects.requireNonNull(minecraft.player)))
                            .origin(leftPos, topPos)
                            .onClose(m -> setCurrentMenu(mainMenu))
                            .entry(new ConfigMenu.Entry<>(1, 10, UndergarmentSettings.LIQUIDS))
                            .entry(new ConfigMenu.Entry<>(2, 10, UndergarmentSettings.MAX_LIQUIDS))
                            .visible(false)
                            .build();
                    liquidsMenu.visitChildren(this::addRenderableWidget);
                    solidsMenu = ConfigMenu.builder(this, font, SOLIDS_MENU_TITLE, () -> Undergarment.getWornUndergarment(Objects.requireNonNull(minecraft.player)))
                            .origin(leftPos, topPos)
                            .onClose(m -> setCurrentMenu(mainMenu))
                            .entry(new ConfigMenu.Entry<>(1, 10, UndergarmentSettings.SOLIDS))
                            .entry(new ConfigMenu.Entry<>(2, 10, UndergarmentSettings.MAX_SOLIDS))
                            .visible(false)
                            .build();
                    solidsMenu.visitChildren(this::addRenderableWidget);
                });
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        guiGraphics.blit(BACKGROUND_TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void tick() {
        super.tick();
        if (currentMenu != null)
            currentMenu.tick();
    }
}

