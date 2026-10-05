package ninja.crinkle.mod.client.gui.screens;

import com.mojang.logging.LogUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import ninja.crinkle.mod.client.color.Color;
import ninja.crinkle.mod.client.gui.events.TabChangedEvent;
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
import org.slf4j.Logger;

import java.util.*;
import java.util.stream.Collectors;

public class CrinkleModConfigScreen extends AbstractScreen {
    private final Player player;
    private Label errorLabel;
    private Label tabTitle;
    private TabbedPanelContainer tabs;
    private CenterContainer titleRow;

    public CrinkleModConfigScreen() {
        super(Component.translatable("gui.crinklemod.screen.config.title"), ClientUtil.screenWidth(), ClientUtil.screenHeight());
        this.player = ClientUtil.getPlayer();
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
        eventManager().addListener(TabChangedEvent.KEY, 0, this::onTabChanged);

        // === Wet tab ===
        MetabolismTab.Wet.buildTab(tabs.addTab("wet", MetabolismSettings.WET.label().getString()), rowHeight);
        // === Mess tab ===
        MetabolismTab.Mess.buildTab(tabs.addTab("mess", MetabolismSettings.MESS.label().getString()), rowHeight);


        // === Undergarment tab ===
        ItemStack itemStack = Undergarment.getWornUndergarment(player());
        if (Undergarment.hasUndergarmentData(itemStack)) {
            buildUndergarmentTab(tabs.addTab("undergarment", itemStack.getDisplayName().getString()), rowHeight, itemStack);
        }

        MarginContainer labelContainer = new MarginContainer.Builder(window)
                .name("info.container")
                .margins(4)
                .minSize(290, fontHeight + 8)
                .style("textbox")
                .horizontalSizing(Sizing.ShrinkCenter)
                .verticalSizing(Sizing.ShrinkCenter)
                .visible(true)
                .build();
        errorLabel = new Label.Builder(window)
                .name("error.label")
                .minSize(0, fontHeight + 6)
                .horizontalSizing(Sizing.ShrinkCenter)
                .verticalSizing(Sizing.ShrinkCenter)
                .color(Color.RED)
                .build();
        labelContainer.add(errorLabel);
        window.add(labelContainer);

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
                .onClick((e, w) -> onReset())
                .push();

        new Button.Builder(footer)
                .name("back_btn")
                .text(Component.translatable("gui.crinklemod.shared.back_button.title").getString())
                .style("button")
                .minSize(60, rowHeight)
                .horizontalSizing(Sizing.Expand, Sizing.Fill)
                .onClick((e, w) -> onClose())
                .push();

        super.init();
    }

    public void onTabChanged(TabChangedEvent event) {
        tabTitle.text(event.currentTab().tabButton().text().text());
        titleRow.arrange();
    }

    @Override
    public void tick() {
        super.tick();
        List<String> errors = MetabolismTab.Wet.errors();
        errors.addAll(MetabolismTab.Mess.errors());
        errorLabel.text(String.join(" ", errors));
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
                .onClick((e, w) -> {
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
                        .name("design_" + design.id().getPath())
                        .atlas(blockAtlas)
                        .texture(design.itemTexture())
                        .minSize(iconSize, iconSize)
                        .activePredicate(w -> design != Undergarment.of(player).getDesign().orElse(null))
                        .onClick((e, w) -> onDesignSelected(design, w))
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
        private final List<String> errors = new ArrayList<>();
        private final Map<Setting<?>, TextBox> valueBoxes = new HashMap<>();
        private static final Logger LOGGER = LogUtils.getLogger();
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

            final TextBox valueBox = new TextBox.Builder(controls)
                    .name("%s.control.textbox".formatted(setting.key()))
                    .style("config.value.textbox")
                    .minSize(50, rowHeight)
                    .readOnly(false)
                    .activePredicate(w -> metabolism().enabled())
                    .horizontalSizing(Sizing.ShrinkCenter)
                    .verticalSizing(Sizing.Expand)
                    .build();
            valueBox.text(metabolism().getAsString(setting));
            valueBoxes.put(setting, valueBox);

            Button minus = new Button.Builder(controls)
                    .name("%s.control.minus".formatted(setting.key()))
                    .text("-")
                    .minSize(20, rowHeight)
                    .onClick((e, w) -> {
                        if (setting instanceof Setting.DoubleValue dv) {
                            double value = metabolism().getAsDouble(dv);
                            setValue(dv, valueBox, dv.subtract(value, modifierValue(dv)));
                        } else if (setting instanceof Setting.IntValue iv) {
                            int value = metabolism().getAsInt(iv);
                            setValue(iv, valueBox, iv.subtract(value, modifierValue(iv)));
                        }
                    })
                    .activePredicate(w -> metabolism().enabled())
                    .build();


            Button plus = new Button.Builder(controls)
                    .name("%s.control.plus".formatted(setting.key()))
                    .text("+")
                    .minSize(20, rowHeight)
                    .onClick((e, w) -> {
                        if (setting instanceof Setting.DoubleValue dv) {
                            double value = metabolism().getAsDouble(dv);
                            setValue(dv, valueBox, dv.add(value, modifierValue(dv)));
                        } else if (setting instanceof Setting.IntValue iv) {
                            int value = metabolism().getAsInt(iv);
                            setValue(iv, valueBox, iv.add(value, modifierValue(iv)));
                        }
                    })
                    .activePredicate(w -> metabolism().enabled())
                    .build();

            controls.add(minus);
            controls.add(valueBox);
            controls.add(plus);
        }

        private <T extends Comparable<? super T>> void setValue(Setting<T> setting, TextBox valueBox, T value) {
            errors().clear();
            if (setting.isValid(value)) {
                valueBox.text(setting.formattedString(value));
                metabolism().setValue(setting, value);
                metabolism().reset();
            } else {
                errors().add(setting.errors(value).stream().map(Component::getString).collect(Collectors.joining(" ")));
            }
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
                    .onClick((e, w) -> {
                        metabolism().enabled(!metabolism().enabled());
                        ((Button) w).text(metabolism().enabled() ? "ON" : "OFF");
                        Metabolism.of(ClientUtil.getPlayer(), settings().type()).syncServer();
                    })
                    .pushAndReturn();

            // training
            buildSettingRow(parent, rowHeight, settings().training());
            // ticks
            buildSettingRow(parent, rowHeight, settings().ticks());
            // slopeDegradation
            buildSettingRow(parent, rowHeight, settings().slopeDegradation());
            // frequencyCompression
            buildSettingRow(parent, rowHeight, settings().frequencyCompression());
            // intensity
            buildSettingRow(parent, rowHeight, settings().intensity());


//            // Void button
//            HBoxContainer voidButtonRow = new HBoxContainer.Builder(parent)
//                    .name("%s.void.row".formatted(settings().type().name().toLowerCase()))
//                    .separation(4)
//                    .minSize(0, rowHeight)
//                    .horizontalSizing(Sizing.Fill)
//                    .verticalSizing(Sizing.Expand)
//                    .pushAndReturn();
//
//            new Button.Builder(voidButtonRow)
//                    .name("%s.void.button".formatted(settings().type().name().toLowerCase()))
//                    .text("Void")
//                    .minSize(20, rowHeight)
//                    .onClick((e, w) -> {
//                        switch(this) {
//                            case Wet -> Metabolism.wetOf(ClientUtil.getPlayer()).wet();
//                            case Mess -> Metabolism.messOf(ClientUtil.getPlayer()).mess();
//                        }
//                    })
//                    .activePredicate(w -> metabolism().enabled())
//                    .pushAndReturn();

        }

        private <T extends Comparable<? super T>> T modifierValue(Setting<T> setting) {
            List<Setting.ModifierValue> modifierValues = new ArrayList<>();
            if (hasShiftDown()) modifierValues.add(Setting.ModifierValue.Shift);
            if (hasControlDown()) modifierValues.add(Setting.ModifierValue.Ctrl);
            if (hasAltDown()) modifierValues.add(Setting.ModifierValue.Alt);
            if (modifierValues.isEmpty()) modifierValues.add(Setting.ModifierValue.None);
            return setting.modifierValue(modifierValues);
        }

        public List<String> errors() {
            return errors;
        }

        private void onReset() {
            metabolism().resetDefaults();
            enableButton.text(metabolism().enabled() ? "ON" : "OFF");
            for (Map.Entry<Setting<?>, TextBox> entry : valueBoxes.entrySet()) {
                TextBox valueBox = entry.getValue();
                Setting<?> setting = entry.getKey();
                valueBox.text(metabolism().getAsString(setting));
            }
        }

        public MetabolismSettings settings() {
            return settings;
        }
    }
}
