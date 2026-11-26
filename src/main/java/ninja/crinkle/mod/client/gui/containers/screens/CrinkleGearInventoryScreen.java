package ninja.crinkle.mod.client.gui.containers.screens;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import ninja.crinkle.mod.client.color.Color;
import ninja.crinkle.mod.client.gui.containers.menus.CrinkleGearMenu;
import ninja.crinkle.mod.client.gui.properties.Sizing;
import ninja.crinkle.mod.client.gui.widgets.*;
import ninja.crinkle.mod.util.ClientUtil;

public class CrinkleGearInventoryScreen extends ThemedEffectsContainerScreen<CrinkleGearMenu> {

    public CrinkleGearInventoryScreen(CrinkleGearMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
    }

    @Override protected void init() {
        super.init();
        int fontHeight = ClientUtil.getMinecraft().font.lineHeight;
        int rowHeight = fontHeight + 10;
        VBoxContainer window = new VBoxContainer.Builder(root())
                .name("crinkle_gear_window")
                .minSize(64, 64)
                .separation(2)
                .style("panel")
                .focusable(true)
                .horizontalSizing(Sizing.ShrinkCenter)
                .verticalSizing(Sizing.ShrinkCenter)
                .pushAndReturn();

        HBoxContainer header = new HBoxContainer.Builder(window)
                .horizontalSizing(Sizing.Fill, Sizing.Expand)
                .pushAndReturn();

        new HBoxContainer.Builder(header)
                .horizontalSizing(Sizing.ShrinkBegin)
                .pushAndReturn();

        CenterContainer titleRow = new CenterContainer.Builder(header)
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
                .text(Component.translatable("gui.crinklemod.screen.gear.title").getString())
                .color(Color.PURPLE)
                .style("panel_title")
                .minSize(0, rowHeight)
                .horizontalSizing(Sizing.ShrinkCenter)
                .pushAndReturn();

        HBoxContainer headerRight = new HBoxContainer.Builder(header)
                .horizontalSizing(Sizing.ShrinkEnd)
                .pushAndReturn();

        MarginContainer rightMargin = new MarginContainer.Builder(headerRight)
                .margins(2, 2, 0, 0)
                .pushAndReturn();
    }

    @Override public String name() {
        return "CrinkleGearScreen";
    }
}
