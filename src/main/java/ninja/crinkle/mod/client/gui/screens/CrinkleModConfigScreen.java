package ninja.crinkle.mod.client.gui.screens;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import ninja.crinkle.mod.client.color.Color;
import ninja.crinkle.mod.client.gui.actors.TabActor;
import ninja.crinkle.mod.client.gui.properties.Sizing;
import ninja.crinkle.mod.client.gui.widgets.*;
import ninja.crinkle.mod.config.ClientConfig;
import ninja.crinkle.mod.metabolism.Metabolism;
import ninja.crinkle.mod.metabolism.MetabolismSettings;
import ninja.crinkle.mod.settings.Setting;
import ninja.crinkle.mod.undergarment.DiaperDesign;
import ninja.crinkle.mod.undergarment.DiaperDesignRegistry;
import ninja.crinkle.mod.undergarment.Undergarment;
import ninja.crinkle.mod.util.ClientUtil;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class CrinkleModConfigScreen extends AbstractScreen implements TabActor {
    private final Player player;
    private Label tabTitle;
    private TabbedPanelContainer tabs;
    private CenterContainer titleRow;

    public CrinkleModConfigScreen() {
        super(Component.translatable("gui.crinklemod.screen.config.title"), ClientUtil.screenWidth(), ClientUtil.screenHeight());
        this.player = ClientUtil.getPlayer();
    }

    @Override
    public void onClose() {
        MetabolismTab.Mess.onClose();
        MetabolismTab.Wet.onClose();
        super.onClose();
    }

    @Override
    public boolean layoutEditorEnabled() {
        return false;
    }

    @Override
    public void registerLayoutEntries() {
        // No-op: this screen manages its own layout and does not support the layout editor.
    }

    @Override
    public void resolveLayoutPositions() {
        // No-op: prevent the layout system from overwriting our centered window position.
    }

    @Override
    public String name() {
        return "CrinkleModConfigScreen";
    }

    @Override
    public void init() {
        int fontHeight = ClientUtil.getMinecraft().font.lineHeight;
        int rowHeight = fontHeight + 8;

        // Root window container (centered, draggable panel)
        VBoxContainer window = new VBoxContainer.Builder(root())
                .name("config_window")
                .minSize(320, 220)
                .separation(2)
                .style("panel")
                .draggable(true)
                .focusable(true)
                .horizontalSizing(Sizing.ShrinkCenter)
                .verticalSizing(Sizing.ShrinkCenter)
                .pushAndReturn();

        titleRow = new CenterContainer.Builder(window)
                .name("title_row")
                .separation(2)
                .horizontalSizing(Sizing.Expand, Sizing.Fill)
                .pushAndReturn();


        HBoxContainer titleContainer = new HBoxContainer.Builder(titleRow)
                .name("title_container")
                .separation(2)
                .pushAndReturn();

        // Title label
        new Label.Builder(titleContainer)
                .name("title")
                .text(Component.translatable("gui.crinklemod.screen.config.title").getString())
                .color(Color.PURPLE)
                .style("panel_title")
                .minSize(0, rowHeight)
                .horizontalSizing(Sizing.ShrinkCenter)
                .pushAndReturn();

        new Label.Builder(titleContainer)
                .name("title_spacer")
                .text("::")
                .color(Color.CYAN)
                .style("panel_title")
                .minSize(0, rowHeight)
                .horizontalSizing(Sizing.ShrinkCenter)
                .pushAndReturn();

        // Tab Title
        tabTitle = new Label.Builder(titleContainer)
                .name("tab_title")
                .text(MetabolismSettings.WET.label().getString())
                .color(Color.MAGENTA)
                .style("panel_title")
                .minSize(0, rowHeight)
                .horizontalSizing(Sizing.ShrinkCenter)
                .pushAndReturn();

        // Tabbed panel
        tabs = new TabbedPanelContainer.Builder(window)
                .name("tabs")
                .tabWidth(90)
                .tabMargin(4)
                .contentMargin(6)
                .horizontalSizing(Sizing.Fill)
                .verticalSizing(Sizing.Expand, Sizing.Fill)
                .build();
        window.add(tabs);

        // === Mess tab ===
        MetabolismTab.Mess.buildTab(tabs.addTab("mess", MetabolismSettings.MESS.label().getString()), rowHeight);
        // === Wet tab ===
        MetabolismTab.Wet.buildTab(tabs.addTab("wet", MetabolismSettings.WET.label().getString()), rowHeight);


        // === Undergarment tab ===
        ItemStack itemStack = Undergarment.getWornUndergarment(player());
        if (Undergarment.hasUndergarmentData(itemStack)) {
            buildUndergarmentTab(tabs.addTab("undergarment", itemStack.getDisplayName().getString()), rowHeight, itemStack);
        }

        // Footer
        MarginContainer footerMargin = new MarginContainer.Builder(window)
                .margins(4)
                .pushAndReturn();
        HBoxContainer footer = new HBoxContainer.Builder(footerMargin)
                .name("footer")
                .separation(6)
                .minSize(0, rowHeight)
                .horizontalSizing(Sizing.Fill)
                .verticalSizing(Sizing.ShrinkEnd)
                .pushAndReturn();

        new Button.Builder(footer)
                .name("reset_btn")
                .text(Component.translatable("gui.crinklemod.shared.reset_button.title").getString())
                .style("button_secondary")
                .minSize(60, rowHeight)
                .horizontalSizing(Sizing.Expand, Sizing.Fill)
                .onClick(w -> onReset())
                .push();

        new Button.Builder(footer)
                .name("back_btn")
                .text(Component.translatable("gui.crinklemod.shared.back_button.title").getString())
                .style("button")
                .minSize(60, rowHeight)
                .horizontalSizing(Sizing.Expand, Sizing.Fill)
                .onClick(w -> onClose())
                .push();

        super.init();
    }

    @Override
    public void onTabChanged(TabbedPanelContainer.@NotNull Entry tab) {
        tabTitle.text(tab.tabButton().text().text());
        titleRow.arrange();
    }

    private void buildUndergarmentTab(VBoxContainer parent, int rowHeight, ItemStack itemStack) {
        Undergarment undergarment = Undergarment.of(itemStack);
        HBoxContainer wetnessRow = new HBoxContainer.Builder(parent)
                .name("wetness_row")
                .separation(4)
                .minSize(0, rowHeight)
                .pushAndReturn();

        new Label.Builder(wetnessRow)
                .name("wetness_progress_label")
                .text(Component.translatable("setting.crinklemod.undergarment.liquids.label").getString())
                .horizontalSizing(Sizing.Expand, Sizing.ShrinkBegin)
                .push();


        ProgressBar wetnessBar = new ProgressBar.Builder(wetnessRow)
                .fillColor(ClientConfig.wetFillColors().entrySet().stream()
                        .sorted(Comparator.comparingInt(a -> a.getValue().color()))
                        .map(Map.Entry::getValue).findFirst().orElse(Color.RAINBOW))
                .backgroundColor(Color.of(128, 128, 128, 1.0f))
                .maxValue(undergarment.getMaxLiquids())
                .value(undergarment.getLiquids())
                .showPercent(true)
                .horizontalSizing(Sizing.Expand, Sizing.Fill)
                .visible(true)
                .pushAndReturn();

        HBoxContainer messinessRow = new HBoxContainer.Builder(parent)
                .name("messiness_row")
                .separation(4)
                .minSize(0, rowHeight)
                .horizontalSizing(Sizing.Fill)
                .verticalSizing(Sizing.ShrinkBegin)
                .pushAndReturn();

        new Label.Builder(messinessRow)
                .name("messiness_progress_label")
                .text(Component.translatable("setting.crinklemod.undergarment.solids.label").getString())
                .horizontalSizing(Sizing.Expand, Sizing.ShrinkBegin)
                .push();


        ProgressBar messinessBar = new ProgressBar.Builder(messinessRow)
                .fillColor(ClientConfig.messFillColors().entrySet().stream()
                        .sorted(Comparator.comparingInt(a -> a.getValue().color()))
                        .map(Map.Entry::getValue).findFirst().orElse(Color.RAINBOW))
                .backgroundColor(Color.of(128, 128, 128, 1.0f))
                .maxValue(undergarment.getMaxSolids())
                .value(undergarment.getSolids())
                .showPercent(true)
                .horizontalSizing(Sizing.Expand, Sizing.Fill)
                .visible(true)
                .pushAndReturn();

        HBoxContainer buttonRow = new HBoxContainer.Builder(parent)
                .name("undergarment_button_row")
                .separation(4)
                .minSize(0, rowHeight)
                .horizontalSizing(Sizing.ShrinkBegin)
                .verticalSizing(Sizing.ShrinkCenter)
                .pushAndReturn();
        new Button.Builder(buttonRow)
                .name("undergarment_clean_action")
                .text("Clean")
                .minSize(20, rowHeight)
                .onClick(w -> {
                    if (player() == null) return;
                    undergarment.setLiquids(0);
                    undergarment.setSolids(0);
                    wetnessBar.value(undergarment.getLiquids());
                    messinessBar.value(undergarment.getSolids());
                    undergarment.syncServer();
                })
                .push();

        // Design section
        buildDesignGrid(parent, rowHeight);
    }

    private void buildDesignGrid(VBoxContainer parent, int rowHeight) {
        Collection<DiaperDesign> designs = DiaperDesignRegistry.getAllDesigns(true);
        if (designs.isEmpty()) return;

        new Label.Builder(parent)
                .name("design_label")
                .text("Design")
                .color(Color.CYAN)
                .minSize(0, rowHeight)
                .horizontalSizing(Sizing.ShrinkBegin)
                .push();

        int iconSize = 20;
        int iconSeparation = 2;

        ScrollContainer scroll = new ScrollContainer.Builder(parent)
                .name("design_scroll")
                .separation(iconSeparation)
                .horizontalSizing(Sizing.Fill)
                .minSize(0, 64)
                .verticalSizing(Sizing.Expand, Sizing.Fill)
                .pushAndReturn();

        int estimatedWidth = 160;
        int columns = Math.max(1, (estimatedWidth + iconSeparation) / (iconSize + iconSeparation));

        TextureAtlas blockAtlas = ClientUtil.getMinecraft().getModelManager()
                .getAtlas(InventoryMenu.BLOCK_ATLAS);

        List<DiaperDesign> designList = new ArrayList<>(designs);
        for (int i = 0; i < designList.size(); i += columns) {
            HBoxContainer row = new HBoxContainer.Builder(scroll)
                    .name("design_row_" + (i / columns))
                    .separation(iconSeparation)
                    .minSize(0, iconSize)
                    .horizontalSizing(Sizing.ShrinkBegin)
                    .verticalSizing(Sizing.ShrinkBegin)
                    .pushAndReturn();

            for (int j = i; j < Math.min(i + columns, designList.size()); j++) {
                DiaperDesign design = designList.get(j);
                new IconButton.Builder(row)
                        .atlas(blockAtlas)
                        .texture(design.itemTexture())
                        .minSize(iconSize, iconSize)
                        .activePredicate(w -> design != Undergarment.of(player).getDesign().orElse(null))
                        .onClick(w -> onDesignSelected(design, w))
                        .name("design_" + design.id().getPath())
                        .pushAndReturn();
            }
        }
    }

    private void onDesignSelected(DiaperDesign design, AbstractWidget widget) {
        ItemStack itemStack = Undergarment.getWornUndergarment(player());
        Undergarment.of(itemStack).setDesign(design);
        Undergarment.of(itemStack).syncServer();
        widget.focused(false);
        widget.active(false);
    }

    private void onReset() {
        switch (tabs.selectedIndex()) {
            case 0:
                MetabolismTab.Wet.onReset();
                break;
            case 1:
                MetabolismTab.Mess.onReset();
                break;
            case 2:
                // todo
                break;
        }
    }

    private Player player() {
        return player;
    }

    @Override
    public void render(@NotNull GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        renderBackground(pGuiGraphics);
        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
    }

    @Override
    public void renderBackground(@NotNull GuiGraphics pGuiGraphics) {
        super.renderBackground(pGuiGraphics);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private enum MetabolismTab {
        Wet(MetabolismSettings.WET),
        Mess(MetabolismSettings.MESS);

        private final MetabolismSettings settings;
        private final Map<Setting<?>, TextBox> valueBoxes = new HashMap<>();
        private Button enableButton;

        MetabolismTab(MetabolismSettings setting) {
            this.settings = setting;
        }

        private Metabolism metabolism() {
            Player player = ClientUtil.getPlayer();
            return Metabolism.of(player, settings().type());
        }

        private <T extends Comparable<? super T>> void buildSettingRow(VBoxContainer parent, int rowHeight, Setting<T> setting) {
            HBoxContainer rowContainer = new HBoxContainer.Builder(parent)
                    .name("%s.row".formatted(setting.key()))
                    .separation(4)
                    .minSize(0, rowHeight)
                    .horizontalSizing(Sizing.Fill)
                    .hoverable(true)
                    .verticalSizing(Sizing.ShrinkBegin)
                    .pushAndReturn();

            new Label.Builder(rowContainer)
                    .name("%s.label".formatted(setting.key()))
                    .text(setting.label().getString())
                    .horizontalSizing(Sizing.Expand, Sizing.ShrinkBegin)
                    .push();

            HBoxContainer controls = new HBoxContainer.Builder(rowContainer)
                    .name("%s.controls".formatted(setting.key()))
                    .separation(1)
                    .horizontalSizing(Sizing.ShrinkEnd)
                    .verticalSizing(Sizing.Fill)
                    .pushAndReturn();

            List<String> errors = new ArrayList<>();
            final TextBox valueBox = new TextBox.Builder(controls)
                    .name("%s.control.textbox".formatted(setting.key()))
                    .style("config.value.textbox")
                    .minSize(50, rowHeight)
                    .readOnly(false)
                    .activePredicate(w -> metabolism().enabled())
                    .horizontalSizing(Sizing.ShrinkCenter)
                    .verticalSizing(Sizing.Expand)
                    .onTextChanged(v -> {
                        errors.clear();
                        rowContainer.tooltip(null);
                        try {
                            if (setting instanceof Setting.DoubleValue dv) {
                                double value = Double.parseDouble(v);
                                errors.addAll(setValue(dv, value));
                            } else if (setting instanceof Setting.IntValue iv) {
                                int value = Integer.parseInt(v);
                                errors.addAll(setValue(iv, value));
                            }
                        } catch (NumberFormatException numberFormatException) {
                            errors.add(Component.translatable("validation.crinklemod.metabolism.failure.invalid.general",
                                    v, setting.displayName()).getString());
                        }
                        if (!errors.isEmpty()) {
                            List<Component> lines = new ArrayList<>();
                            lines.add(Component.literal("Errors found:").withStyle(ChatFormatting.WHITE));
                            for (String error : errors) {
                                if (error.isBlank()) continue;
                                lines.add(Component.translatable(error).withStyle(ChatFormatting.RED));
                            }
                            rowContainer.tooltip(lines);
                            return true;
                        }
                        return true;
                    })
                    .build();
            valueBox.value(metabolism().getAsString(setting));
            valueBoxes.put(setting, valueBox);

            Button minus = new Button.Builder(controls)
                    .name("%s.control.minus".formatted(setting.key()))
                    .text("-")
                    .minSize(20, rowHeight)
                    .onClick(w -> {
                        errors.clear();
                        rowContainer.tooltip(null);
                        if (setting instanceof Setting.DoubleValue dv) {
                            double value = metabolism().getAsDouble(dv);
                            errors.addAll(setValue(dv, dv.subtract(value, modifierValue(dv))));
                        } else if (setting instanceof Setting.IntValue iv) {
                            int value = metabolism().getAsInt(iv);
                            errors.addAll(setValue(iv, iv.subtract(value, modifierValue(iv))));
                        }
                        valueBox.value(metabolism().getAsString(setting));
                    })
                    .activePredicate(w -> metabolism().enabled())
                    .build();


            Button plus = new Button.Builder(controls)
                    .name("%s.control.plus".formatted(setting.key()))
                    .text("+")
                    .minSize(20, rowHeight)
                    .onClick(w -> {
                        errors.clear();
                        rowContainer.tooltip(null);
                        if (setting instanceof Setting.DoubleValue dv) {
                            double value = metabolism().getAsDouble(dv);
                            errors.addAll(setValue(dv, dv.add(value, modifierValue(dv))));
                        } else if (setting instanceof Setting.IntValue iv) {
                            int value = metabolism().getAsInt(iv);
                            errors.addAll(setValue(iv, iv.add(value, modifierValue(iv))));
                        }
                        valueBox.value(metabolism().getAsString(setting));
                    })
                    .activePredicate(w -> metabolism().enabled())
                    .build();

            controls.add(minus);
            controls.add(valueBox);
            controls.add(plus);

            new Label.Builder(controls).text("?")
                    .tooltip(Collections.singletonList(setting.tooltip()))
                    .hoverable(true)
                    .pushAndReturn();

            new Label.Builder(controls).text("*")
                    .visiblePredicate(w -> !errors.isEmpty())
                    .color(Color.RED).pushAndReturn();
        }

        private <T extends Comparable<? super T>> List<String> setValue(Setting<T> setting, T value) {
            List<String> errors = new ArrayList<>();
            if (setting.isValid(value)) {
                metabolism().setValue(setting, value);
            } else {
                errors.add(setting.errors(value).stream().map(Component::getString).collect(Collectors.joining(" ")));
            }
            return errors;
        }

        void buildTab(VBoxContainer parent, int rowHeight) {
            // Enabled toggle row
            HBoxContainer enabledRow = new HBoxContainer.Builder(parent)
                    .name("%s.row".formatted(settings.enabled().key()))
                    .separation(4)
                    .minSize(0, rowHeight)
                    .horizontalSizing(Sizing.Fill)
                    .verticalSizing(Sizing.ShrinkEnd)
                    .pushAndReturn();

            new Label.Builder(enabledRow)
                    .name("%s.label".formatted(settings.enabled().key()))
                    .text(settings().enabled().label().getString())
                    .horizontalSizing(Sizing.ShrinkBegin, Sizing.Expand)
                    .push();

            enableButton = new Button.Builder(enabledRow)
                    .name("%s.button".formatted(settings.enabled().key()))
                    .text(metabolism().enabled() ? "ON" : "OFF")
                    .minSize(92, rowHeight)
                    .onClick(w -> {
                        metabolism().enabled(!metabolism().enabled());
                        ((Button) w).text(metabolism().enabled() ? "ON" : "OFF");
                        Metabolism.of(ClientUtil.getPlayer(), settings().type()).syncServer();
                    })
                    .pushAndReturn();

            // control
            buildSettingRow(parent, rowHeight, settings().control());
            // ticks
            buildSettingRow(parent, rowHeight, settings().interval());
            // slopeDegradation
            buildSettingRow(parent, rowHeight, settings().controlDecay());
            // frequencyCompression
            buildSettingRow(parent, rowHeight, settings().intervalDecay());
            // intensity
            buildSettingRow(parent, rowHeight, settings().intensity());
        }

        private <T extends Comparable<? super T>> T modifierValue(Setting<T> setting) {
            List<Setting.ModifierValue> modifierValues = new ArrayList<>();
            if (hasShiftDown()) modifierValues.add(Setting.ModifierValue.Shift);
            if (hasControlDown()) modifierValues.add(Setting.ModifierValue.Ctrl);
            if (hasAltDown()) modifierValues.add(Setting.ModifierValue.Alt);
            if (modifierValues.isEmpty()) modifierValues.add(Setting.ModifierValue.None);
            return setting.modifierValue(modifierValues);
        }

        private void onReset() {
            metabolism().resetDefaults();
            enableButton.text(metabolism().enabled() ? "ON" : "OFF");
            for (Map.Entry<Setting<?>, TextBox> entry : valueBoxes.entrySet()) {
                TextBox valueBox = entry.getValue();
                Setting<?> setting = entry.getKey();
                valueBox.value(metabolism().getAsString(setting));
            }
        }

        private void onClose() {
            valueBoxes.forEach((key, value) -> value.focused(false));
            metabolism().reset();
        }

        public MetabolismSettings settings() {
            return settings;
        }
    }
}
