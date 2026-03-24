package ninja.crinkle.mod.client.gui.screens;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import ninja.crinkle.mod.api.ServerUpdater;
import ninja.crinkle.mod.client.color.Color;
import ninja.crinkle.mod.client.gui.events.TabChangedEvent;
import ninja.crinkle.mod.client.gui.properties.Sizing;
import ninja.crinkle.mod.client.gui.widgets.*;
import ninja.crinkle.mod.metabolism.MetabolismSettings;
import ninja.crinkle.mod.settings.Setting;
import ninja.crinkle.mod.util.ClientUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CrinkleModConfigScreen extends AbstractScreen {
    private final Player player;
    private Label dirtyLabel;
    private Label errorLabel;
    // Pending values
    private int pendingTimer;
    private Button saveButton;
    private Label tabTitle;
    // Widgets that need updating
    private TextBox timerTextBox;
    private CenterContainer titleRow;

    public CrinkleModConfigScreen() {
        super(Component.translatable("gui.crinklemod.screen.config.title"),
                ClientUtil.screenWidth(), ClientUtil.screenHeight());
        this.player = ClientUtil.getPlayer();
        pendingTimer = MetabolismSettings.TIMER.get(player);
        MetabolismTab.NumberOne.init(player);
        MetabolismTab.NumberTwo.init(player);
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
        int rowHeight = fontHeight + 13;

        // Root window container (centered, draggable panel)
        VBoxContainer window = new VBoxContainer.Builder(root())
                .name("config_window")
                .minSize(300, 220)
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
                .text("General")
                .color(Color.MAGENTA)
                .style("panel_title")
                .minSize(0, rowHeight)
                .horizontalSizing(Sizing.ShrinkCenter)
                .pushAndReturn();

        // Tabbed panel
        TabbedPanelContainer tabs = new TabbedPanelContainer.Builder(window)
                .name("tabs")
                .tabWidth(90)
                .tabMargin(4)
                .contentMargin(4)
                .horizontalSizing(Sizing.Fill)
                .verticalSizing(Sizing.Expand, Sizing.Fill)
                .build();
        window.add(tabs);
        eventManager().addListener(TabChangedEvent.KEY, 0, this::onTabChanged);

        // === General tab ===
        buildTimerRow(tabs.addTab("general",
                Component.translatable("gui.crinklemod.config.tab.general").getString()), rowHeight);

        // === Number One tab ===
        MetabolismTab.NumberOne.buildTab(tabs.addTab("number_one",
                Component.translatable("gui.crinklemod.config.tab.number_one").getString()), rowHeight);

        // === Number Two tab ===
        MetabolismTab.NumberTwo.buildTab(tabs.addTab("number_two",
                Component.translatable("gui.crinklemod.config.tab.number_two").getString()), rowHeight);

        // Dirty label
        dirtyLabel = new Label.Builder(window)
                .name("dirty_label")
                .text(Component.translatable("gui.crinklemod.shared.unsaved_changes.label").getString())
                .minSize(0, fontHeight + 6)
                .horizontalSizing(Sizing.Fill)
                .verticalSizing(Sizing.ShrinkBegin)
                .visible(false)
                .build();
        window.add(dirtyLabel);

        errorLabel = new Label.Builder(window)
                .name("error_label")
                .minSize(0, fontHeight + 6)
                .horizontalSizing(Sizing.Fill)
                .verticalSizing(Sizing.ShrinkBegin)
                .visible(false)
                .build();
        window.add(errorLabel);

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

        saveButton = new Button.Builder(footer)
                .name("save_btn")
                .text(Component.translatable("gui.crinklemod.shared.save_button.title").getString())
                .style("button_primary")
                .minSize(60, rowHeight)
                .horizontalSizing(Sizing.Expand, Sizing.Fill)
                .onClick((e, w) -> onSave())
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

    private void buildTimerRow(VBoxContainer parent, int rowHeight) {
        HBoxContainer row = new HBoxContainer.Builder(parent)
                .name("timer_row")
                .separation(4)
                .minSize(0, rowHeight)
                .horizontalSizing(Sizing.Fill)
                .verticalSizing(Sizing.ShrinkBegin)
                .pushAndReturn();

        new Label.Builder(row)
                .name("timer_label")
                .text(MetabolismSettings.TIMER.label().getString())
                .horizontalSizing(Sizing.Expand, Sizing.ShrinkBegin)
                .push();

        HBoxContainer controls = new HBoxContainer.Builder(row)
                .name("timer_controls")
                .separation(1)
                .horizontalSizing(Sizing.ShrinkEnd)
                .verticalSizing(Sizing.Fill)
                .pushAndReturn();

        new Button.Builder(controls)
                .name("timer_minus")
                .text("-")
                .minSize(20, rowHeight)
                .onClick((e, w) -> {
                    pendingTimer = Math.max(10, pendingTimer - 5);
                    refreshTimerDisplay();
                })
                .push();

        timerTextBox = controls.addTextBox()
                .name("timer_value")
                .text(String.valueOf(pendingTimer))
                .minSize(45, rowHeight)
                .style("textbox")
                .readOnly(true)
                .pushAndReturn();

        new Button.Builder(controls)
                .name("timer_plus")
                .text("+")
                .minSize(20, rowHeight)
                .onClick((e, w) -> {
                    pendingTimer += 5;
                    refreshTimerDisplay();
                })
                .push();
    }

    private void onSave() {
        saveSetting(MetabolismSettings.TIMER, pendingTimer, player());
        MetabolismTab.NumberOne.onSave();
        MetabolismTab.NumberTwo.onSave();
    }

    private void onReset() {
        MetabolismTab.NumberOne.onReset();
        MetabolismTab.NumberTwo.onReset();
        refreshTimerDisplay();
    }

    private void refreshTimerDisplay() {
        timerTextBox.text(String.valueOf(pendingTimer));
    }

    private static <T extends Comparable<? super T>> void saveSetting(Setting<T> setting, Object value,
                                                                      Player provider) {
        setting.set(provider, value);
        setting.syncer(provider).ifPresent(ServerUpdater::syncServer);
    }

    @Override
    public void tick() {
        super.tick();
        List<Component> errors = Stream.of(
                        timerErrors(),
                        MetabolismTab.NumberOne.errors(),
                        MetabolismTab.NumberTwo.errors())
                .flatMap(Collection::stream)
                .toList();
        if (!errors.isEmpty()) {
            errorLabel.text(errors.stream().map(Component::toString).collect(Collectors.joining("\n")));
            errorLabel.visible(true);
            saveButton.active(false);
        } else {
            errorLabel.visible(false);
            errorLabel.text("");
            boolean dirty = isDirty();
            dirtyLabel.visible(dirty);
            saveButton.active(dirty);
        }
    }

    private List<Component> timerErrors() {
        return MetabolismSettings.TIMER.errors(player(), pendingTimer);
    }

    // --- Display refresh ---

    private boolean isDirty() {
        Player p = player();
        if (p == null) return false;
        return pendingTimer != MetabolismSettings.TIMER.get(p)
                || MetabolismTab.NumberOne.isDirty()
                || MetabolismTab.NumberTwo.isDirty();
    }

    private Player player() {
        return player;
    }

    @Override
    public void render(@NotNull GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        renderBackground(pGuiGraphics);
        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
    }


    // --- Screen overrides ---

    @Override
    public void renderBackground(@NotNull GuiGraphics pGuiGraphics) {
        super.renderBackground(pGuiGraphics);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private enum MetabolismTab {
        NumberOne("n1",
                MetabolismSettings.NUMBER_ONE_ENABLED,
                MetabolismSettings.NUMBER_ONE_CHANCE,
                MetabolismSettings.NUMBER_ONE_SAFE_ROLLS
        ),
        NumberTwo("n2",
                MetabolismSettings.NUMBER_TWO_ENABLED,
                MetabolismSettings.NUMBER_TWO_CHANCE,
                MetabolismSettings.NUMBER_TWO_SAFE_ROLLS
        );

        private final Setting<Double> chance;
        // settings
        private final Setting<Boolean> enabled;
        private final String prefix;
        private final Setting<Integer> safeRolls;
        private TextBox chanceTextBox;
        // widgets
        private Button enabledButton;
        private boolean formChanged;
        private double pendingChance;
        // values
        private boolean pendingEnabled;
        private int pendingSafeRolls;
        private Player player;
        private TextBox safeRollsTextBox;

        MetabolismTab(String prefix, Setting<Boolean> enabled, Setting<Double> chance, Setting<Integer> safeRolls) {
            this.prefix = prefix;
            this.enabled = enabled;
            this.chance = chance;
            this.safeRolls = safeRolls;
            this.player = null;
        }

        void buildTab(VBoxContainer parent, int rowHeight) {
            // Enabled toggle row
            HBoxContainer enabledRow = new HBoxContainer.Builder(parent)
                    .name(prefix + "_enabled_row")
                    .separation(4)
                    .minSize(0, rowHeight)
                    .horizontalSizing(Sizing.Fill)
                    .verticalSizing(Sizing.ShrinkEnd)
                    .pushAndReturn();

            new Label.Builder(enabledRow)
                    .name(prefix + "_enabled_label")
                    .text(Component.translatable("setting.crinklemod.metabolism.enabled.label").getString())
                    .horizontalSizing(Sizing.ShrinkBegin, Sizing.Expand)
                    .push();

            enabledButton = new Button.Builder(enabledRow)
                    .name(prefix + "_enabled_btn")
                    .text(pendingEnabled ? "ON" : "OFF")
                    .minSize(50, rowHeight)
                    .onClick((e, w) -> {
                        pendingEnabled = !pendingEnabled;
                        refreshDisplay();
                    })
                    // .horizontalSizing(Sizing.ShrinkEnd)
                    .pushAndReturn();

            // Chance row
            HBoxContainer chanceRow = new HBoxContainer.Builder(parent)
                    .name(prefix + "_chance_row")
                    .separation(4)
                    .minSize(0, rowHeight)
                    .horizontalSizing(Sizing.Fill)
                    .verticalSizing(Sizing.ShrinkBegin)
                    .pushAndReturn();

            new Label.Builder(chanceRow)
                    .name(prefix + "_chance_label")
                    .text(Component.translatable("setting.crinklemod.metabolism.chance.label").getString())
                    .horizontalSizing(Sizing.Expand, Sizing.ShrinkBegin)
                    .push();

            HBoxContainer chanceControls = new HBoxContainer.Builder(chanceRow)
                    .name(prefix + "_chance_controls")
                    .separation(1)
                    .horizontalSizing(Sizing.ShrinkEnd)
                    .verticalSizing(Sizing.Fill)
                    .pushAndReturn();

            new Button.Builder(chanceControls)
                    .name(prefix + "_chance_minus")
                    .text("-")
                    .minSize(20, rowHeight)
                    .onClick((e, w) -> {
                        pendingChance = pendingChance - doubleModifierValue();
                        refreshDisplay();
                    })
                    .activePredicate(w -> pendingEnabled)
                    .pushAndReturn();

            chanceTextBox = chanceControls.addTextBox()
                    .name(prefix + "_chance_value")
                    .text(formatPercent(pendingChance))
                    .minSize(45, rowHeight)
                    .style("textbox")
                    .activePredicate(w -> pendingEnabled)
                    .pushAndReturn();

            new Button.Builder(chanceControls)
                    .name(prefix + "_chance_plus")
                    .text("+")
                    .minSize(20, rowHeight)
                    .onClick((e, w) -> {
                        pendingChance = pendingChance + doubleModifierValue();
                        refreshDisplay();
                    })
                    .activePredicate(w -> pendingEnabled)
                    .pushAndReturn();

            // Safe Rolls row
            HBoxContainer safeRollsRow = new HBoxContainer.Builder(parent)
                    .name(prefix + "_safe_rolls_row")
                    .separation(4)
                    .minSize(0, rowHeight)
                    .horizontalSizing(Sizing.Fill)
                    .verticalSizing(Sizing.ShrinkBegin)
                    .pushAndReturn();

            new Label.Builder(safeRollsRow)
                    .name(prefix + "_safe_rolls_label")
                    .text(Component.translatable("setting.crinklemod.metabolism.safeRolls.label").getString())
                    .horizontalSizing(Sizing.Expand, Sizing.ShrinkBegin)
                    .push();

            HBoxContainer safeRollsControls = new HBoxContainer.Builder(safeRollsRow)
                    .name(prefix + "_safe_rolls_controls")
                    .separation(1)
                    .horizontalSizing(Sizing.ShrinkEnd)
                    .verticalSizing(Sizing.Fill)
                    .pushAndReturn();

            new Button.Builder(safeRollsControls)
                    .name(prefix + "_safe_rolls_minus")
                    .text("-")
                    .minSize(20, rowHeight)
                    .onClick((e, w) -> {
                        pendingSafeRolls = pendingSafeRolls - intModifierValue();
                        refreshDisplay();
                    })
                    .activePredicate(w -> pendingEnabled)
                    .pushAndReturn();

            safeRollsTextBox = safeRollsControls.addTextBox()
                    .name(prefix + "_safe_rolls_value")
                    .text(String.valueOf(pendingSafeRolls))
                    .minSize(45, rowHeight)
                    .style("textbox")
                    .activePredicate(w -> pendingEnabled)
                    .pushAndReturn();

            new Button.Builder(safeRollsControls)
                    .name(prefix + "_safe_rolls_plus")
                    .text("+")
                    .minSize(20, rowHeight)
                    .onClick((e, w) -> {
                        pendingSafeRolls = pendingSafeRolls + intModifierValue();
                        refreshDisplay();
                    })
                    .activePredicate(w -> pendingEnabled)
                    .pushAndReturn();
        }

        private void refreshDisplay() {
            enabledButton.text().text(pendingEnabled ? "ON" : "OFF");
            chanceTextBox.text(formatPercent(pendingChance));
            safeRollsTextBox.text(String.valueOf(pendingSafeRolls));
        }

        private double doubleModifierValue() {
            if (hasControlDown()) return 0.1d;
            if (hasShiftDown()) return 0.05d;
            return 0.01d;
        }

        private static String formatPercent(double value) {
            return String.format("%d%%", Math.round(value * 100));
        }

        private int intModifierValue() {
            return (int) (doubleModifierValue() * 100);
        }

        private List<Component> errors() {
            return Stream.of(
                            enabled.errors(player, pendingEnabled),
                            chance.errors(player, pendingChance),
                            safeRolls.errors(player, pendingSafeRolls))
                    .flatMap(Collection::stream)
                    .toList();
        }

        void init(Player player) {
            if (player == null) return;
            this.pendingChance = chance.get(player);
            this.pendingEnabled = enabled.get(player);
            this.pendingSafeRolls = safeRolls.get(player);
            this.player = player;
        }

        private boolean isDirty() {
            player().ifPresent(p -> formChanged = pendingEnabled != enabled.get(p)
                    || Math.abs(pendingChance - chance.getDouble(p)) > 0.001
                    || pendingSafeRolls != safeRolls.get(p));
            return formChanged;
        }

        public Optional<Player> player() {
            return Optional.ofNullable(player);
        }

        private void onReset() {
            player().ifPresent(p -> {
                pendingEnabled = enabled.getDefault(p);
                pendingChance = chance.getDefaultDouble(p);
                pendingSafeRolls = safeRolls.getDefault(p);
                refreshDisplay();
            });
        }

        private void onSave() {
            player().ifPresent(p -> {
                saveSetting(enabled, pendingEnabled, p);
                saveSetting(chance, pendingChance, p);
                saveSetting(safeRolls, pendingSafeRolls, p);
            });
        }
    }
}
