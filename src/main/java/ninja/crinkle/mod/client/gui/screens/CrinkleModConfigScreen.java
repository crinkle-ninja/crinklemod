package ninja.crinkle.mod.client.gui.screens;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import ninja.crinkle.mod.api.ServerUpdater;
import ninja.crinkle.mod.client.color.Color;
import ninja.crinkle.mod.client.gui.properties.Sizing;
import ninja.crinkle.mod.client.gui.widgets.*;
import ninja.crinkle.mod.metabolism.MetabolismSettings;
import ninja.crinkle.mod.settings.Setting;
import ninja.crinkle.mod.util.ClientUtil;
import org.jetbrains.annotations.NotNull;

public class CrinkleModConfigScreen extends AbstractScreen {

    // Pending values
    private int pendingTimer;
    private boolean pendingN1Enabled;
    private double pendingN1Chance;
    private int pendingN1SafeRolls;
    private boolean pendingN2Enabled;
    private double pendingN2Chance;
    private int pendingN2SafeRolls;

    // Widgets that need updating
    private TextBox timerTextBox;
    private Button n1EnabledButton;
    private TextBox n1ChanceTextBox;
    private TextBox n1SafeRollsTextBox;
    private Button n2EnabledButton;
    private TextBox n2ChanceTextBox;
    private TextBox n2SafeRollsTextBox;
    private Label dirtyLabel;
    private Button saveButton;

    // Number One/Two dependent widgets (for dimming)
    private Button n1ChanceMinus, n1ChancePlus, n1SafeRollsMinus, n1SafeRollsPlus;
    private Button n2ChanceMinus, n2ChancePlus, n2SafeRollsMinus, n2SafeRollsPlus;

    public CrinkleModConfigScreen() {
        super(Component.translatable("gui.crinklemod.screen.config.title"),
                ClientUtil.screenWidth(), ClientUtil.screenHeight());
        loadCurrentValues();
    }

    @Override
    public String name() {
        return "CrinkleModConfigScreen";
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

    private Player player() {
        return ClientUtil.getPlayer();
    }

    private void loadCurrentValues() {
        Player p = player();
        if (p == null) return;
        pendingTimer = MetabolismSettings.TIMER.get(p);
        pendingN1Enabled = MetabolismSettings.NUMBER_ONE_ENABLED.get(p);
        pendingN1Chance = MetabolismSettings.NUMBER_ONE_CHANCE.getDouble(p);
        pendingN1SafeRolls = MetabolismSettings.NUMBER_ONE_SAFE_ROLLS.get(p);
        pendingN2Enabled = MetabolismSettings.NUMBER_TWO_ENABLED.get(p);
        pendingN2Chance = MetabolismSettings.NUMBER_TWO_CHANCE.getDouble(p);
        pendingN2SafeRolls = MetabolismSettings.NUMBER_TWO_SAFE_ROLLS.get(p);
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

        // Title label
        Label title = new Label.Builder(window)
                .name("title")
                .text(Component.translatable("gui.crinklemod.screen.config.title").getString())
                .color(Color.PURPLE)
                .minSize(0, rowHeight)
                .horizontalSizing(Sizing.Fill)
                .verticalSizing(Sizing.ShrinkBegin)
                .build();
        window.add(title);

        // Tabbed panel
        TabbedPanelContainer tabs = new TabbedPanelContainer.Builder(window)
                .name("tabs")
                .tabWidth(90)
                .tabMargin(4)
                .horizontalSizing(Sizing.Fill)
                .verticalSizing(Sizing.Expand, Sizing.Fill)
                .build();
        window.add(tabs);

        // === General tab ===
        VBoxContainer generalContent = tabs.addTab("general",
                Component.translatable("gui.crinklemod.config.tab.general").getString());
        buildTimerRow(generalContent, rowHeight);

        // === Number One tab ===
        VBoxContainer n1Content = tabs.addTab("number_one",
                Component.translatable("gui.crinklemod.config.tab.number_one").getString());
        buildNumberOneTab(n1Content, rowHeight);

        // === Number Two tab ===
        VBoxContainer n2Content = tabs.addTab("number_two",
                Component.translatable("gui.crinklemod.config.tab.number_two").getString());
        buildNumberTwoTab(n2Content, rowHeight);

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
                .horizontalSizing(Sizing.Expand, Sizing.Fill)
                .push();

        new Button.Builder(row)
                .name("timer_minus")
                .text("-")
                .minSize(20, rowHeight)
                .onClick((e, w) -> { pendingTimer = Math.max(10, pendingTimer - 5); refreshTimerDisplay(); })
                .push();

        timerTextBox = row.addTextBox()
                .name("timer_value")
                .text(String.valueOf(pendingTimer))
                .minSize(45, rowHeight)
                .style("textbox")
                .readOnly(true)
                .pushAndReturn();

        new Button.Builder(row)
                .name("timer_plus")
                .text("+")
                .minSize(20, rowHeight)
                .onClick((e, w) -> { pendingTimer += 5; refreshTimerDisplay(); })
                .push();
    }

    private void buildNumberOneTab(VBoxContainer parent, int rowHeight) {
        // Enabled toggle row
        HBoxContainer enabledRow = new HBoxContainer.Builder(parent)
                .name("n1_enabled_row")
                .separation(4)
                .minSize(0, rowHeight)
                .horizontalSizing(Sizing.Fill)
                .verticalSizing(Sizing.ShrinkBegin)
                .pushAndReturn();

        new Label.Builder(enabledRow)
                .name("n1_enabled_label")
                .text(MetabolismSettings.NUMBER_ONE_ENABLED.label().getString())
                .horizontalSizing(Sizing.Expand, Sizing.Fill)
                .push();

        n1EnabledButton = new Button.Builder(enabledRow)
                .name("n1_enabled_btn")
                .text(pendingN1Enabled ? "ON" : "OFF")
                .minSize(50, rowHeight)
                .onClick((e, w) -> { pendingN1Enabled = !pendingN1Enabled; refreshN1Display(); })
                .pushAndReturn();

        // Chance row
        HBoxContainer chanceRow = new HBoxContainer.Builder(parent)
                .name("n1_chance_row")
                .separation(4)
                .minSize(0, rowHeight)
                .horizontalSizing(Sizing.Fill)
                .verticalSizing(Sizing.ShrinkBegin)
                .pushAndReturn();

        new Label.Builder(chanceRow)
                .name("n1_chance_label")
                .text(MetabolismSettings.NUMBER_ONE_CHANCE.label().getString())
                .horizontalSizing(Sizing.Expand, Sizing.Fill)
                .push();

        n1ChanceMinus = new Button.Builder(chanceRow)
                .name("n1_chance_minus")
                .text("-")
                .minSize(20, rowHeight)
                .onClick((e, w) -> { pendingN1Chance = Math.max(0.0, pendingN1Chance - 0.05); refreshN1Display(); })
                .pushAndReturn();

        n1ChanceTextBox = chanceRow.addTextBox()
                .name("n1_chance_value")
                .text(formatPercent(pendingN1Chance))
                .minSize(45, rowHeight)
                .style("textbox")
                .readOnly(true)
                .pushAndReturn();

        n1ChancePlus = new Button.Builder(chanceRow)
                .name("n1_chance_plus")
                .text("+")
                .minSize(20, rowHeight)
                .onClick((e, w) -> { pendingN1Chance = Math.min(1.0, pendingN1Chance + 0.05); refreshN1Display(); })
                .pushAndReturn();

        // Safe Rolls row
        HBoxContainer safeRollsRow = new HBoxContainer.Builder(parent)
                .name("n1_safe_rolls_row")
                .separation(4)
                .minSize(0, rowHeight)
                .horizontalSizing(Sizing.Fill)
                .verticalSizing(Sizing.ShrinkBegin)
                .pushAndReturn();

        new Label.Builder(safeRollsRow)
                .name("n1_safe_rolls_label")
                .text(MetabolismSettings.NUMBER_ONE_SAFE_ROLLS.label().getString())
                .horizontalSizing(Sizing.Expand, Sizing.Fill)
                .push();

        n1SafeRollsMinus = new Button.Builder(safeRollsRow)
                .name("n1_safe_rolls_minus")
                .text("-")
                .minSize(20, rowHeight)
                .onClick((e, w) -> { pendingN1SafeRolls = Math.max(0, pendingN1SafeRolls - 1); refreshN1Display(); })
                .pushAndReturn();

        n1SafeRollsTextBox = safeRollsRow.addTextBox()
                .name("n1_safe_rolls_value")
                .text(String.valueOf(pendingN1SafeRolls))
                .minSize(45, rowHeight)
                .style("textbox")
                .readOnly(true)
                .pushAndReturn();

        n1SafeRollsPlus = new Button.Builder(safeRollsRow)
                .name("n1_safe_rolls_plus")
                .text("+")
                .minSize(20, rowHeight)
                .onClick((e, w) -> { pendingN1SafeRolls += 1; refreshN1Display(); })
                .pushAndReturn();
    }

    private void buildNumberTwoTab(VBoxContainer parent, int rowHeight) {
        // Enabled toggle row
        HBoxContainer enabledRow = new HBoxContainer.Builder(parent)
                .name("n2_enabled_row")
                .separation(4)
                .minSize(0, rowHeight)
                .horizontalSizing(Sizing.Fill)
                .verticalSizing(Sizing.ShrinkBegin)
                .pushAndReturn();

        new Label.Builder(enabledRow)
                .name("n2_enabled_label")
                .text(MetabolismSettings.NUMBER_TWO_ENABLED.label().getString())
                .horizontalSizing(Sizing.Expand, Sizing.Fill)
                .push();

        n2EnabledButton = new Button.Builder(enabledRow)
                .name("n2_enabled_btn")
                .text(pendingN2Enabled ? "ON" : "OFF")
                .minSize(50, rowHeight)
                .onClick((e, w) -> { pendingN2Enabled = !pendingN2Enabled; refreshN2Display(); })
                .pushAndReturn();

        // Chance row
        HBoxContainer chanceRow = new HBoxContainer.Builder(parent)
                .name("n2_chance_row")
                .separation(4)
                .minSize(0, rowHeight)
                .horizontalSizing(Sizing.Fill)
                .verticalSizing(Sizing.ShrinkBegin)
                .pushAndReturn();

        new Label.Builder(chanceRow)
                .name("n2_chance_label")
                .text(MetabolismSettings.NUMBER_TWO_CHANCE.label().getString())
                .horizontalSizing(Sizing.Expand, Sizing.Fill)
                .push();

        n2ChanceMinus = new Button.Builder(chanceRow)
                .name("n2_chance_minus")
                .text("-")
                .minSize(20, rowHeight)
                .onClick((e, w) -> { pendingN2Chance = Math.max(0.0, pendingN2Chance - 0.05); refreshN2Display(); })
                .pushAndReturn();

        n2ChanceTextBox = chanceRow.addTextBox()
                .name("n2_chance_value")
                .text(formatPercent(pendingN2Chance))
                .minSize(45, rowHeight)
                .style("textbox")
                .readOnly(true)
                .pushAndReturn();

        n2ChancePlus = new Button.Builder(chanceRow)
                .name("n2_chance_plus")
                .text("+")
                .minSize(20, rowHeight)
                .onClick((e, w) -> { pendingN2Chance = Math.min(1.0, pendingN2Chance + 0.05); refreshN2Display(); })
                .pushAndReturn();

        // Safe Rolls row
        HBoxContainer safeRollsRow = new HBoxContainer.Builder(parent)
                .name("n2_safe_rolls_row")
                .separation(4)
                .minSize(0, rowHeight)
                .horizontalSizing(Sizing.Fill)
                .verticalSizing(Sizing.ShrinkBegin)
                .pushAndReturn();

        new Label.Builder(safeRollsRow)
                .name("n2_safe_rolls_label")
                .text(MetabolismSettings.NUMBER_TWO_SAFE_ROLLS.label().getString())
                .horizontalSizing(Sizing.Expand, Sizing.Fill)
                .push();

        n2SafeRollsMinus = new Button.Builder(safeRollsRow)
                .name("n2_safe_rolls_minus")
                .text("-")
                .minSize(20, rowHeight)
                .onClick((e, w) -> { pendingN2SafeRolls = Math.max(0, pendingN2SafeRolls - 1); refreshN2Display(); })
                .pushAndReturn();

        n2SafeRollsTextBox = safeRollsRow.addTextBox()
                .name("n2_safe_rolls_value")
                .text(String.valueOf(pendingN2SafeRolls))
                .minSize(45, rowHeight)
                .style("textbox")
                .readOnly(true)
                .pushAndReturn();

        n2SafeRollsPlus = new Button.Builder(safeRollsRow)
                .name("n2_safe_rolls_plus")
                .text("+")
                .minSize(20, rowHeight)
                .onClick((e, w) -> { pendingN2SafeRolls += 1; refreshN2Display(); })
                .pushAndReturn();
    }

    // --- Display refresh ---

    private void refreshTimerDisplay() {
        timerTextBox.text(String.valueOf(pendingTimer));
    }

    private void refreshN1Display() {
        n1EnabledButton.text().text(pendingN1Enabled ? "ON" : "OFF");
        n1ChanceTextBox.text(formatPercent(pendingN1Chance));
        n1SafeRollsTextBox.text(String.valueOf(pendingN1SafeRolls));

        // Dim dependent controls when disabled
        n1ChanceMinus.active(pendingN1Enabled);
        n1ChancePlus.active(pendingN1Enabled);
        n1ChanceTextBox.active(pendingN1Enabled);
        n1SafeRollsMinus.active(pendingN1Enabled);
        n1SafeRollsPlus.active(pendingN1Enabled);
        n1SafeRollsTextBox.active(pendingN1Enabled);
    }

    private void refreshN2Display() {
        n2EnabledButton.text().text(pendingN2Enabled ? "ON" : "OFF");
        n2ChanceTextBox.text(formatPercent(pendingN2Chance));
        n2SafeRollsTextBox.text(String.valueOf(pendingN2SafeRolls));

        // Dim dependent controls when disabled
        n2ChanceMinus.active(pendingN2Enabled);
        n2ChancePlus.active(pendingN2Enabled);
        n2ChanceTextBox.active(pendingN2Enabled);
        n2SafeRollsMinus.active(pendingN2Enabled);
        n2SafeRollsPlus.active(pendingN2Enabled);
        n2SafeRollsTextBox.active(pendingN2Enabled);
    }

    private void refreshAllDisplays() {
        refreshTimerDisplay();
        refreshN1Display();
        refreshN2Display();
    }

    // --- Dirty state ---

    private boolean isDirty() {
        Player p = player();
        if (p == null) return false;
        return pendingTimer != MetabolismSettings.TIMER.get(p)
                || pendingN1Enabled != MetabolismSettings.NUMBER_ONE_ENABLED.get(p)
                || Math.abs(pendingN1Chance - MetabolismSettings.NUMBER_ONE_CHANCE.getDouble(p)) > 0.001
                || pendingN1SafeRolls != MetabolismSettings.NUMBER_ONE_SAFE_ROLLS.get(p)
                || pendingN2Enabled != MetabolismSettings.NUMBER_TWO_ENABLED.get(p)
                || Math.abs(pendingN2Chance - MetabolismSettings.NUMBER_TWO_CHANCE.getDouble(p)) > 0.001
                || pendingN2SafeRolls != MetabolismSettings.NUMBER_TWO_SAFE_ROLLS.get(p);
    }

    @Override
    public void tick() {
        super.tick();
        if (dirtyLabel != null) {
            boolean dirty = isDirty();
            dirtyLabel.visible(dirty);
            if (saveButton != null) {
                saveButton.active(dirty);
            }
        }
    }

    // --- Actions ---

    private void onSave() {
        Player p = player();
        if (p == null) return;

        saveSetting(MetabolismSettings.TIMER, pendingTimer, p);
        saveSetting(MetabolismSettings.NUMBER_ONE_ENABLED, pendingN1Enabled, p);
        saveSetting(MetabolismSettings.NUMBER_ONE_CHANCE, clampDouble(pendingN1Chance, 0.0, 1.0), p);
        saveSetting(MetabolismSettings.NUMBER_ONE_SAFE_ROLLS, Math.max(0, pendingN1SafeRolls), p);
        saveSetting(MetabolismSettings.NUMBER_TWO_ENABLED, pendingN2Enabled, p);
        saveSetting(MetabolismSettings.NUMBER_TWO_CHANCE, clampDouble(pendingN2Chance, 0.0, 1.0), p);
        saveSetting(MetabolismSettings.NUMBER_TWO_SAFE_ROLLS, Math.max(0, pendingN2SafeRolls), p);
    }

    private <T extends Comparable<? super T>> void saveSetting(Setting<T> setting, Object value, Player provider) {
        setting.set(provider, value);
        setting.syncer(provider).ifPresent(ServerUpdater::syncServer);
    }

    private void onReset() {
        Player p = player();
        if (p == null) return;
        pendingTimer = MetabolismSettings.TIMER.getDefault(p);
        pendingN1Enabled = MetabolismSettings.NUMBER_ONE_ENABLED.getDefault(p);
        pendingN1Chance = MetabolismSettings.NUMBER_ONE_CHANCE.getDefaultDouble(p);
        pendingN1SafeRolls = MetabolismSettings.NUMBER_ONE_SAFE_ROLLS.getDefault(p);
        pendingN2Enabled = MetabolismSettings.NUMBER_TWO_ENABLED.getDefault(p);
        pendingN2Chance = MetabolismSettings.NUMBER_TWO_CHANCE.getDefaultDouble(p);
        pendingN2SafeRolls = MetabolismSettings.NUMBER_TWO_SAFE_ROLLS.getDefault(p);
        refreshAllDisplays();
    }

    // --- Utility ---

    private static String formatPercent(double value) {
        return String.format("%d%%", Math.round(value * 100));
    }

    private static double clampDouble(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    // --- Screen overrides ---

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
