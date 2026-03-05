package ninja.crinkle.mod.client.gui.screens;

import ninja.crinkle.mod.client.color.Color;
import ninja.crinkle.mod.client.gui.properties.Sizing;
import ninja.crinkle.mod.client.gui.screens.binding.PendingSettings;
import ninja.crinkle.mod.client.gui.screens.binding.SettingRegistry;
import ninja.crinkle.mod.client.gui.screens.binding.StepperBinder;
import ninja.crinkle.mod.client.gui.screens.binding.ToggleBinder;
import ninja.crinkle.mod.client.gui.screens.definition.TabNode;
import ninja.crinkle.mod.client.gui.screens.definition.WidgetNode;
import ninja.crinkle.mod.client.gui.widgets.*;
import ninja.crinkle.mod.settings.Setting;
import net.minecraft.network.chat.Component;

import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.Map;

public class WidgetTreeBuilder {

    private final PendingSettings pending;
    private final Map<String, AbstractWidget> roleWidgets;
    private final Map<String, Runnable> roleActions;
    private final Map<String, StepperBinder> stepperBinders = new LinkedHashMap<>();
    private final int rowHeight;

    public WidgetTreeBuilder(PendingSettings pending, Map<String, AbstractWidget> roleWidgets,
                             Map<String, Runnable> roleActions, int rowHeight) {
        this.pending = pending;
        this.roleWidgets = roleWidgets;
        this.roleActions = roleActions;
        this.rowHeight = rowHeight;
    }

    public Map<String, StepperBinder> stepperBinders() {
        return stepperBinders;
    }

    public void build(WidgetNode node, AbstractContainer parent) {
        if (node instanceof WidgetNode.LabelNode n) buildLabel(n, parent);
        else if (node instanceof WidgetNode.ButtonNode n) buildButton(n, parent);
        else if (node instanceof WidgetNode.TextBoxNode n) buildTextBox(n, parent);
        else if (node instanceof WidgetNode.VBoxNode n) buildVBox(n, parent);
        else if (node instanceof WidgetNode.HBoxNode n) buildHBox(n, parent);
        else if (node instanceof WidgetNode.MarginNode n) buildMargin(n, parent);
        else if (node instanceof WidgetNode.CenterNode n) buildCenter(n, parent);
        else if (node instanceof WidgetNode.PanelNode n) buildPanel(n, parent);
        else if (node instanceof WidgetNode.TabbedPanelNode n) buildTabbedPanel(n, parent);
        else if (node instanceof WidgetNode.SettingStepperNode n) buildSettingStepper(n, parent);
        else if (node instanceof WidgetNode.SettingToggleNode n) buildSettingToggle(n, parent);
    }

    private void buildLabel(WidgetNode.LabelNode n, AbstractContainer parent) {
        Label.Builder b = new Label.Builder(parent);
        applyCommon(b, n);
        if (n.text() != null) b.text(resolveText(n.text()));
        if (n.color() != null) b.color(resolveColor(n.color()));
        Label label = b.build();
        parent.add(label);
        if (n.role() != null) roleWidgets.put(n.role(), label);
    }

    private void buildButton(WidgetNode.ButtonNode n, AbstractContainer parent) {
        Button.Builder b = new Button.Builder(parent);
        applyCommon(b, n);
        if (n.text() != null) b.text(resolveText(n.text()));
        if (n.role() != null && roleActions.containsKey(n.role())) {
            Runnable action = roleActions.get(n.role());
            b.onClick((e, w) -> action.run());
        }
        Button button = b.build();
        parent.add(button);
        if (n.role() != null) roleWidgets.put(n.role(), button);
    }

    private void buildTextBox(WidgetNode.TextBoxNode n, AbstractContainer parent) {
        TextBox.Builder b = parent.addTextBox();
        applyCommon(b, n);
        if (n.text() != null) b.text(resolveText(n.text()));
        TextBox textBox = b.build();
        parent.add(textBox);
    }

    private void buildVBox(WidgetNode.VBoxNode n, AbstractContainer parent) {
        VBoxContainer.Builder b = new VBoxContainer.Builder(parent);
        applyCommon(b, n);
        if (n.separation() != null) b.separation(n.separation());
        VBoxContainer container = b.build();
        parent.add(container);
        for (WidgetNode child : n.children()) {
            build(child, container);
        }
    }

    private void buildHBox(WidgetNode.HBoxNode n, AbstractContainer parent) {
        HBoxContainer.Builder b = new HBoxContainer.Builder(parent);
        applyCommon(b, n);
        if (n.separation() != null) b.separation(n.separation());
        HBoxContainer container = b.build();
        parent.add(container);
        for (WidgetNode child : n.children()) {
            build(child, container);
        }
    }

    private void buildMargin(WidgetNode.MarginNode n, AbstractContainer parent) {
        MarginContainer.Builder b = new MarginContainer.Builder(parent);
        applyCommon(b, n);
        if (n.margins() != null) b.margins(n.margins());
        MarginContainer container = b.build();
        parent.add(container);
        for (WidgetNode child : n.children()) {
            build(child, container);
        }
    }

    private void buildCenter(WidgetNode.CenterNode n, AbstractContainer parent) {
        CenterContainer.Builder b = new CenterContainer.Builder(parent);
        applyCommon(b, n);
        CenterContainer container = b.build();
        parent.add(container);
        for (WidgetNode child : n.children()) {
            build(child, container);
        }
    }

    private void buildPanel(WidgetNode.PanelNode n, AbstractContainer parent) {
        VBoxContainer.Builder b = new VBoxContainer.Builder(parent);
        applyCommon(b, n);
        VBoxContainer container = b.build();
        parent.add(container);
        for (WidgetNode child : n.children()) {
            build(child, container);
        }
    }

    private void buildTabbedPanel(WidgetNode.TabbedPanelNode n, AbstractContainer parent) {
        TabbedPanelContainer.Builder b = new TabbedPanelContainer.Builder(parent);
        applyCommon(b, n);
        if (n.tabWidth() != null) b.tabWidth(n.tabWidth());
        if (n.tabMargin() != null) b.tabMargin(n.tabMargin());
        if (n.contentMargin() != null) b.contentMargin(n.contentMargin());
        TabbedPanelContainer container = b.build();
        parent.add(container);
        for (TabNode tab : n.tabs()) {
            VBoxContainer tabContent = container.addTab(tab.id(), resolveText(tab.label()));
            for (WidgetNode child : tab.children()) {
                build(child, tabContent);
            }
        }
    }

    private void buildSettingStepper(WidgetNode.SettingStepperNode n, AbstractContainer parent) {
        Setting<?> setting = SettingRegistry.get(n.setting());
        StepperBinder binder = new StepperBinder(pending, n.setting(), n.step(), n.format());

        HBoxContainer row = new HBoxContainer.Builder(parent)
                .name(n.name() != null ? n.name() : n.setting() + "_row")
                .separation(4)
                .minSize(0, rowHeight)
                .horizontalSizing(Sizing.Fill)
                .verticalSizing(Sizing.ShrinkBegin)
                .build();
        parent.add(row);

        Label label = new Label.Builder(row)
                .name(n.setting() + "_label")
                .text(setting.label().getString())
                .horizontalSizing(Sizing.Expand, Sizing.Fill)
                .build();
        row.add(label);

        HBoxContainer controls = new HBoxContainer.Builder(row)
                .name(n.setting() + "_controls")
                .separation(1)
                .horizontalSizing(Sizing.ShrinkEnd)
                .verticalSizing(Sizing.Fill)
                .build();
        row.add(controls);

        TextBox valueBox = controls.addTextBox()
                .name(n.setting() + "_value")
                .text(binder.displayValue())
                .minSize(45, rowHeight)
                .style("textbox")
                .readOnly(true)
                .build();

        new Button.Builder(controls)
                .name(n.setting() + "_minus")
                .text("-")
                .minSize(20, rowHeight)
                .onClick((e, w) -> {
                    binder.decrement();
                    valueBox.text(binder.displayValue());
                })
                .pushAndReturn();

        controls.add(valueBox);

        new Button.Builder(controls)
                .name(n.setting() + "_plus")
                .text("+")
                .minSize(20, rowHeight)
                .onClick((e, w) -> {
                    binder.increment();
                    valueBox.text(binder.displayValue());
                })
                .pushAndReturn();

        // Register for dependent widget activation and reset refresh
        roleWidgets.put("stepper:" + n.setting(), row);
        roleWidgets.put("stepper_value:" + n.setting(), valueBox);
        stepperBinders.put(n.setting(), binder);
    }

    private void buildSettingToggle(WidgetNode.SettingToggleNode n, AbstractContainer parent) {
        Setting<?> setting = SettingRegistry.get(n.setting());
        ToggleBinder binder = new ToggleBinder(pending, n.setting());

        HBoxContainer row = new HBoxContainer.Builder(parent)
                .name(n.name() != null ? n.name() : n.setting() + "_row")
                .separation(4)
                .minSize(0, rowHeight)
                .horizontalSizing(Sizing.Fill)
                .verticalSizing(Sizing.ShrinkBegin)
                .build();
        parent.add(row);

        Label label = new Label.Builder(row)
                .name(n.setting() + "_label")
                .text(setting.label().getString())
                .horizontalSizing(Sizing.Expand, Sizing.Fill)
                .build();
        row.add(label);

        Button toggleButton = new Button.Builder(row)
                .name(n.setting() + "_btn")
                .text(binder.displayValue())
                .minSize(50, rowHeight)
                .onClick((e, w) -> {
                    binder.toggle();
                    ((Button) w).text().text(binder.displayValue());
                    // Update dependent widgets
                    if (n.dependsOn() != null) {
                        for (String dep : n.dependsOn()) {
                            AbstractWidget depWidget = roleWidgets.get("stepper:" + dep);
                            if (depWidget != null) {
                                setActiveRecursive(depWidget, binder.value());
                            }
                        }
                    }
                })
                .build();
        row.add(toggleButton);

        // Register for initial state application
        roleWidgets.put("toggle:" + n.setting(), toggleButton);
    }

    private void setActiveRecursive(AbstractWidget widget, boolean active) {
        widget.active(active);
        if (widget instanceof AbstractContainer container) {
            for (AbstractWidget child : container.children()) {
                setActiveRecursive(child, active);
            }
        }
    }

    private void applyCommon(AbstractWidget.AbstractBuilder<?> b, WidgetNode n) {
        if (n.name() != null) b.name(n.name());
        if (n.style() != null) b.style(n.style());
        if (n.draggable() != null) b.draggable(n.draggable());
        if (n.visible() != null) b.visible(n.visible());
        int[] minSize = resolveMinSize(n.minSize());
        if (minSize != null) b.minSize(minSize[0], minSize[1]);
        if (n.horizontalSizing() != null) b.horizontalSizing(parseSizing(n.horizontalSizing()));
        if (n.verticalSizing() != null) b.verticalSizing(parseSizing(n.verticalSizing()));
        if (n.stretchRatio() != null) b.stretchRatio(n.stretchRatio());
    }

    private int[] resolveMinSize(int[] minSize) {
        if (minSize == null || minSize.length < 2) return null;
        return minSize;
    }

    private String resolveText(String text) {
        if (text == null) return null;
        // $variable substitution
        text = text.replace("$rowHeight", String.valueOf(rowHeight));
        // Resolve translation keys (dot-separated identifiers)
        if (text.matches("[a-z][a-z0-9_.]+")) {
            text = Component.translatable(text).getString();
        }
        return text;
    }

    private static Color resolveColor(String color) {
        return switch (color.toUpperCase()) {
            case "WHITE" -> Color.WHITE;
            case "BLACK" -> Color.BLACK;
            case "RED" -> Color.RED;
            case "GREEN" -> Color.GREEN;
            case "BLUE" -> Color.BLUE;
            case "PURPLE" -> Color.PURPLE;
            case "YELLOW" -> Color.YELLOW;
            case "ORANGE" -> Color.ORANGE;
            case "PINK" -> Color.PINK;
            case "CYAN" -> Color.CYAN;
            case "MAGENTA" -> Color.MAGENTA;
            case "BROWN" -> Color.BROWN;
            case "RAINBOW" -> Color.RAINBOW;
            default -> Color.of(color);
        };
    }

    private static Sizing[] parseSizing(String[] values) {
        if (values == null) return new Sizing[0];
        Sizing[] result = new Sizing[values.length];
        for (int i = 0; i < values.length; i++) {
            result[i] = Sizing.valueOf(values[i]);
        }
        return result;
    }
}
