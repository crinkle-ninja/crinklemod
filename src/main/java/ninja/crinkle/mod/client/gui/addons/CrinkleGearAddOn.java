package ninja.crinkle.mod.client.gui.addons;

import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import ninja.crinkle.mod.capabilities.CrinkleGear;
import ninja.crinkle.mod.client.color.Color;
import ninja.crinkle.mod.client.gui.properties.Sizing;
import ninja.crinkle.mod.client.gui.widgets.*;

public class CrinkleGearAddOn extends AbstractAddOn {
    private final ItemSlot undergarmentSlot;

    public CrinkleGearAddOn() {
        super(InventoryScreen.class);

        MarginContainer addOn = new MarginContainer.Builder(root())
                .margins(2)
                .pushAndReturn();

        CenterContainer panel = new CenterContainer.Builder(addOn)
                .style("panel_dark")
                .minSize(120, 120)
                .horizontalSizing(Sizing.ShrinkCenter)
                .verticalSizing(Sizing.ShrinkCenter)
                .pushAndReturn();

        VBoxContainer layout = new VBoxContainer.Builder(panel)
                .verticalSizing(Sizing.Expand, Sizing.Fill)
                .pushAndReturn();

        // header
        HBoxContainer header = new HBoxContainer.Builder(layout)
                .horizontalSizing(Sizing.ShrinkCenter)
                .pushAndReturn();

        new Label.Builder(header)
                .text("CrinkleGear")
                .color(Color.MAGENTA)
                .push();

        // body
        VBoxContainer body = new VBoxContainer.Builder(layout)
                .horizontalSizing(Sizing.Expand, Sizing.Fill)
                .verticalSizing(Sizing.Expand, Sizing.Fill)
                .pushAndReturn();

        undergarmentSlot = new ItemSlot.Builder(body)
                .slotType(CrinkleGear.SlotType.Undergarment)
                .horizontalSizing(Sizing.ShrinkCenter)
                .verticalSizing(Sizing.ShrinkCenter)
                .minSize(16, 16)
                .pushAndReturn();
    }
}
