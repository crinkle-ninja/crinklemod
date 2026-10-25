package ninja.crinkle.mod.client.gui.widgets;

import ninja.crinkle.mod.client.color.Color;
import ninja.crinkle.mod.client.gui.managers.DragManager;
import ninja.crinkle.mod.client.gui.properties.Sizing;

@SuppressWarnings("unused")
public class Modal extends MarginContainer {
    @SuppressWarnings("FieldCanBeLocal")
    private CenterContainer content;
    @SuppressWarnings("FieldCanBeLocal")
    private Label titleLabel;

    public Modal(AbstractContainer parent) {
        super(new MarginContainer.Builder(parent)
                .name("modal")
                .visible(true)
                .style("panel")
                .draggable(true)
                .focusable(true)
                .margins(8)
        );
    }

    @Override
    public int zIndex() {
        return DragManager.Z_MODAL + super.zIndex();
    }

    @Override
    public void init() {
        super.init();
        VBoxContainer layout = new VBoxContainer.Builder(this)
                .verticalSizing(Sizing.Expand)
                .horizontalSizing(Sizing.Expand)
                .pushAndReturn();

        HBoxContainer titleSection = new HBoxContainer.Builder(layout)
                .verticalSizing(Sizing.Expand)
                .horizontalSizing(Sizing.ShrinkBegin)
                .pushAndReturn();
        titleLabel = new Label.Builder(titleSection).text("Modal").color(Color.WHITE).pushAndReturn();
        Button closeButton = new Button.Builder(titleSection)
                .text("X")
                .onClick(w -> {
                    this.focused(false);
                    this.visible(false);
                })
                .pushAndReturn();
        this.content = new CenterContainer.Builder(layout).pushAndReturn();
        HBoxContainer footerSection = new HBoxContainer.Builder(layout).pushAndReturn();
    }
}
