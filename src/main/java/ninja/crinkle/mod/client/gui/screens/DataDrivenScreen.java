package ninja.crinkle.mod.client.gui.screens;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import ninja.crinkle.mod.client.gui.events.KeyEvent;
import ninja.crinkle.mod.client.gui.events.listeners.KeyListener;
import ninja.crinkle.mod.client.gui.screens.binding.PendingSettings;
import ninja.crinkle.mod.client.gui.screens.binding.StepperBinder;
import ninja.crinkle.mod.client.gui.screens.definition.ScreenDefinition;
import ninja.crinkle.mod.client.gui.screens.definition.WidgetNode;
import ninja.crinkle.mod.client.gui.widgets.AbstractContainer;
import ninja.crinkle.mod.client.gui.widgets.AbstractWidget;
import ninja.crinkle.mod.client.gui.widgets.Button;
import ninja.crinkle.mod.client.gui.widgets.TextBox;
import ninja.crinkle.mod.util.ClientUtil;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Map;

public class DataDrivenScreen extends AbstractScreen implements KeyListener {

    private final ScreenDefinition definition;
    private final Map<String, AbstractWidget> roleWidgets = new LinkedHashMap<>();
    private Map<String, StepperBinder> stepperBinders = Map.of();
    private PendingSettings pending;

    public DataDrivenScreen(ScreenDefinition definition) {
        super(Component.translatable(definition.title()),
                ClientUtil.screenWidth(), ClientUtil.screenHeight());
        this.definition = definition;
    }

    @Override
    public String name() {
        return "DataDrivenScreen:" + definition.id();
    }

    @Override
    public boolean layoutEditorEnabled() {
        return false;
    }

    @Override
    public void onKey(KeyEvent event) {
        KeyListener.super.onKey(event);
    }

    @Override
    public void registerLayoutEntries() {
    }

    @Override
    public void resolveLayoutPositions() {
    }

    private Player player() {
        return ClientUtil.getPlayer();
    }

    @Override
    public void init() {
        Player p = player();
        if (p != null) {
            pending = new PendingSettings(definition.settingKeys(), p);
        }

        int fontHeight = ClientUtil.getMinecraft().font.lineHeight;
        int rowHeight = fontHeight + 13;

        // Prepare role actions that buttons can bind to during construction
        Map<String, Runnable> roleActions = new LinkedHashMap<>();
        roleActions.put("save", this::onSave);
        roleActions.put("reset", this::onReset);
        roleActions.put("close", this::onClose);

        WidgetTreeBuilder builder = new WidgetTreeBuilder(pending, roleWidgets, roleActions, rowHeight);
        builder.build(definition.root(), root());
        stepperBinders = builder.stepperBinders();

        applyInitialToggleStates();

        super.init();
    }

    private void applyInitialToggleStates() {
        for (Map.Entry<String, AbstractWidget> entry : roleWidgets.entrySet()) {
            if (entry.getKey().startsWith("toggle:")) {
                String settingKey = entry.getKey().substring("toggle:".length());
                if (pending != null) {
                    boolean value = pending.get(settingKey);
                    applyToggleDependents(settingKey, value);
                }
            }
        }
    }

    private void applyToggleDependents(String settingKey, boolean value) {
        applyToggleDependentsFromNode(definition.root(), settingKey, value);
    }

    private void applyToggleDependentsFromNode(WidgetNode node, String settingKey, boolean value) {
        if (node instanceof WidgetNode.SettingToggleNode) {
            WidgetNode.SettingToggleNode toggle = (WidgetNode.SettingToggleNode) node;
            if (settingKey.equals(toggle.setting()) && toggle.dependsOn() != null) {
                for (String dep : toggle.dependsOn()) {
                    AbstractWidget depWidget = roleWidgets.get("stepper:" + dep);
                    if (depWidget != null) {
                        setActiveRecursive(depWidget, value);
                    }
                }
            }
        }
        for (WidgetNode child : node.childNodes()) {
            applyToggleDependentsFromNode(child, settingKey, value);
        }
    }

    private void setActiveRecursive(AbstractWidget widget, boolean active) {
        widget.active(active);
        if (widget instanceof AbstractContainer container) {
            for (AbstractWidget child : container.children()) {
                setActiveRecursive(child, active);
            }
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (pending != null) {
            boolean dirty = pending.isDirty();
            AbstractWidget dirtyIndicator = roleWidgets.get("dirty_indicator");
            if (dirtyIndicator != null) {
                dirtyIndicator.visible(dirty);
            }
            AbstractWidget saveButton = roleWidgets.get("save");
            if (saveButton != null) {
                saveButton.active(dirty);
            }
        }
    }

    private void onSave() {
        if (pending != null) {
            pending.save();
        }
    }

    private void onReset() {
        if (pending != null) {
            pending.reset();
            refreshAllDisplays();
            applyInitialToggleStates();
        }
    }

    private void refreshAllDisplays() {
        for (Map.Entry<String, StepperBinder> entry : stepperBinders.entrySet()) {
            AbstractWidget valueBox = roleWidgets.get("stepper_value:" + entry.getKey());
            if (valueBox instanceof TextBox tb) {
                tb.text(entry.getValue().displayValue());
            }
        }
        for (Map.Entry<String, AbstractWidget> entry : roleWidgets.entrySet()) {
            if (entry.getKey().startsWith("toggle:")) {
                String settingKey = entry.getKey().substring("toggle:".length());
                if (entry.getValue() instanceof Button btn) {
                    boolean val = pending.get(settingKey);
                    btn.text().text(val ? "ON" : "OFF");
                }
            }
        }
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
