package ninja.crinkle.mod.client.ui.screens;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import ninja.crinkle.mod.client.ClientHooks;
import ninja.crinkle.mod.client.color.Color;
import ninja.crinkle.mod.client.ui.menus.AbstractMenu;
import ninja.crinkle.mod.client.ui.menus.ConfigMenu;
import ninja.crinkle.mod.client.ui.menus.status.*;
import ninja.crinkle.mod.client.ui.themes.Theme;
import ninja.crinkle.mod.client.ui.widgets.Label;
import ninja.crinkle.mod.client.ui.widgets.themes.ThemedCheckbox;
import ninja.crinkle.mod.items.custom.DiaperArmorItem;
import ninja.crinkle.mod.undergarment.Undergarment;
import ninja.crinkle.mod.undergarment.UndergarmentSettings;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;

public class CrinkleScreen extends FlexContainerScreen {
    private static final Component METABOLISM_TITLE = Component.translatable("gui.crinklemod.metabolism_screen.title");
    private static final Component LIQUIDS_MENU_TITLE = Component.translatable("gui.crinklemod.metabolism_screen.liquids_menu.title");
    private static final Component BLADDER_MENU_TITLE = Component.translatable("gui.crinklemod.metabolism_screen.bladder_menu.title");
    private static final Component BOWEL_MENU_TITLE = Component.translatable("gui.crinklemod.metabolism_screen.bowel_menu.title");
    private static final Component SOLIDS_MENU_TITLE = Component.translatable("gui.crinklemod.metabolism_screen.solids_menu.title");
    private static final int LIQUIDS_COLOR = 0xff00ffff;
    private static final int SOLIDS_COLOR = 0xff90ee90;
    private static final int BLADDER_COLOR = 0xffffef00;
    private static final int BOWEL_COLOR = 0xFF836953;
    private final Screen previousScreen;
    private AbstractMenu mainMenu;
    private AbstractMenu metabolismMenu;
    private AbstractMenu undergarmentMenu;
    private AbstractMenu currentMenu;
    private ConfigMenu<Player> liquidsMenu;
    private ConfigMenu<Player> solidsMenu;
    private ConfigMenu<Player> bladderMenu;
    private ConfigMenu<Player> bowelMenu;
    private ConfigMenu<ItemStack> undergarmentLiquidsMenu;
    private ConfigMenu<ItemStack> undergarmentSolidsMenu;
    private boolean showDiaperTextureDebug = false;
    private int currentLine = 0;

    public CrinkleScreen(Screen previousScreen) {
        super(METABOLISM_TITLE, Theme.DEFAULT);
        this.previousScreen = previousScreen;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void setCurrentMenu(AbstractMenu menu) {
        if (currentMenu != null) {
            refreshFlex();
            currentMenu.setVisible(false);
        }
        currentMenu = menu;
        currentMenu.setVisible(true);
        updateUndergarmentVisibility();
        flex();
    }

    private int nextLine() {
        return currentLine++;
    }

    @Override
    protected void init() {
        super.init();
        final int lineSpacing = 5;
        final int lineHeight = 20;
        final int componentWidth = 20;
        setPadding(lineSpacing);
        Optional.ofNullable(DistExecutor.safeCallWhenOn(Dist.CLIENT, () -> ClientHooks::getMinecraft))
                .ifPresent(minecraft -> {
                    metabolismMenu = StatusMenu.builder(this)
                            .font(font)
                            .lineHeight(lineHeight)
                            .lineSpacing(lineSpacing)
                            .spacer(4)
                            .entry(LabelEntry.builder(font, METABOLISM_TITLE)
                                    .lineNumber(nextLine())
                                    .color(getTheme().getForegroundColor().color())
                                    .build())
                            .build();
                    // Creating this here so nextLine() still works
                    EquipmentSlotEntry equipmentSlotEntry = new EquipmentSlotEntry(nextLine(), EquipmentSlot.LEGS,
                            Component.translatable("gui.crinklemod.status.equipment_slot.title"),
                            Tooltip.create(Component.translatable("gui.crinklemod.status.equipment_slot.tooltip")));
                    undergarmentMenu = StatusMenu.builder(this)
                            .font(font)
                            .lineHeight(lineHeight)
                            .lineSpacing(lineSpacing)
                            .spacer(4)
                            .entry(StatusBarEntry.intBuilder(() ->
                                            Undergarment.getWornUndergarment(Objects.requireNonNull(minecraft.player)))
                                    .lineNumber(nextLine())
                                    .setting(UndergarmentSettings.LIQUIDS)
                                    .onPress((menu) -> setCurrentMenu(undergarmentLiquidsMenu))
                                    .gradientStartColor(Undergarment.LIQUIDS_COLOR)
                                    .gradientEndColor(Undergarment.LIQUIDS_COLOR)
                                    .gradientBackgroundColor(Color.BLACK.color())
                                    .build())
                            .entry(StatusBarEntry.intBuilder(() ->
                                            Undergarment.getWornUndergarment(Objects.requireNonNull(minecraft.player)))
                                    .lineNumber(nextLine())
                                    .setting(UndergarmentSettings.SOLIDS)
                                    .onPress((menu) -> setCurrentMenu(undergarmentSolidsMenu))
                                    .gradientStartColor(Undergarment.SOLIDS_COLOR)
                                    .gradientEndColor(Undergarment.SOLIDS_COLOR)
                                    .gradientBackgroundColor(Color.BLACK.color())
                                    .build())
                            .entry(CheckboxEntry.builder(nextLine())
                                    .checkbox(ThemedCheckbox.builder(getTheme(), Component
                                                    .translatable("gui.crinklemod.status.checkbox.debug.title"),
                                                    c -> showDiaperTextureDebug = c.isSelected())
                                            .bounds(0, 0, componentWidth, lineHeight)
                                            .selected(showDiaperTextureDebug)
                                            .label(Label.builder(getMinecraft().font, Component
                                                            .translatable("gui.crinklemod.status.checkbox.debug.title"))
                                                    .color(getTheme().getForegroundColor().color())
                                                    .build(), true)
                                            .tooltip(Tooltip.create(Component
                                                    .translatable("gui.crinklemod.status.checkbox.debug.tooltip")))
                                            .build())
                                    .build())
                            .build();
                    mainMenu = StatusMenu.builder(this)
                            .font(font)
                            .lineHeight(lineHeight)
                            .lineSpacing(lineSpacing)
                            .spacer(4)
                            .entry(equipmentSlotEntry)
                            .subMenu(metabolismMenu)
                            .subMenu(undergarmentMenu)
                            .build();
                    mainMenu.visitAll(this::addRenderableWidget);
                    setCurrentMenu(mainMenu);
                    undergarmentLiquidsMenu = ConfigMenu.builder(this, font, LIQUIDS_MENU_TITLE, () -> Undergarment.getWornUndergarment(Objects.requireNonNull(minecraft.player)))
                            .origin(0, 0)
                            .onClose(m -> setCurrentMenu(mainMenu))
                            .entry(new ConfigMenu.Entry<>(1, 10, UndergarmentSettings.LIQUIDS))
                            .entry(new ConfigMenu.Entry<>(2, 10, UndergarmentSettings.MAX_LIQUIDS))
                            .visible(false)
                            .build();
                    undergarmentLiquidsMenu.visitAll(this::addRenderableWidget);
                    undergarmentSolidsMenu = ConfigMenu.builder(this, font, SOLIDS_MENU_TITLE, () -> Undergarment.getWornUndergarment(Objects.requireNonNull(minecraft.player)))
                            .origin(0, 0)
                            .onClose(m -> setCurrentMenu(mainMenu))
                            .entry(new ConfigMenu.Entry<>(1, 10, UndergarmentSettings.SOLIDS))
                            .entry(new ConfigMenu.Entry<>(2, 10, UndergarmentSettings.MAX_SOLIDS))
                            .visible(false)
                            .build();
                    undergarmentSolidsMenu.visitAll(this::addRenderableWidget);
                });
    }

    private void updateUndergarmentVisibility() {
        if (currentMenu != mainMenu) return;
        boolean hasUndergarment = Undergarment.getWornUndergarment(Objects.requireNonNull(getMinecraft().player)) != ItemStack.EMPTY;
        if (undergarmentMenu.isVisible() != hasUndergarment) {
            undergarmentMenu.setVisible(hasUndergarment);
        }
    }

    @Override
    public void onClose() {
        super.onClose();
        Optional.ofNullable(DistExecutor.safeCallWhenOn(Dist.CLIENT, () -> ClientHooks::getMinecraft))
                .ifPresent(minecraft -> minecraft.setScreen(previousScreen));
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int pMouseX, int pMouseY, float pPartialTick) {
        super.render(graphics, pMouseX, pMouseY, pPartialTick);
        if (!showDiaperTextureDebug) return;
        ItemStack item = Undergarment.getWornUndergarment(Objects.requireNonNull(getMinecraft().player));
        if (item != ItemStack.EMPTY && item.getItem() instanceof DiaperArmorItem diaper) {
            graphics.blit(diaper.getTexture(), 0, 0, 0, 0, 256, 256);
            graphics.drawString(getMinecraft().font, Component.literal(diaper.getTexture().getPath()),
                    0, 260, Color.WHITE.color(), true);
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (currentMenu != null) {
            currentMenu.tick();
        }
    }
}

