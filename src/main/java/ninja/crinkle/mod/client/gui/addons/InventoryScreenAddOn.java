package ninja.crinkle.mod.client.gui.addons;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import ninja.crinkle.mod.client.gui.containers.menus.CrinkleGearMenu;
import ninja.crinkle.mod.client.gui.properties.Sizing;
import ninja.crinkle.mod.client.gui.screens.CrinkleModConfigScreen;
import ninja.crinkle.mod.client.gui.widgets.Button;
import ninja.crinkle.mod.client.gui.widgets.VBoxContainer;
import ninja.crinkle.mod.util.ClientUtil;
import org.jetbrains.annotations.NotNull;

public class InventoryScreenAddOn extends AbstractAddOn {
    public InventoryScreenAddOn() {
        super(InventoryScreen.class);
        VBoxContainer container = new VBoxContainer.Builder(root())
                .name("inventory.panel.buttons")
                .verticalSizing(Sizing.ShrinkCenter)
                .horizontalSizing(Sizing.ShrinkCenter)
                .pushAndReturn();
        new Button.Builder(container)
                .text(Component.translatable("gui.crinklemod.screen.config.title").getString())
                .margins(4)
                .onClick((w) -> Minecraft.getInstance().setScreen(new CrinkleModConfigScreen()))
                .push();
        new Button.Builder(container)
                .text(Component.translatable("gui.crinklemod.addon.inventory.button.gear").getString())
                .margins(4)
                .onClick((w) -> ClientUtil.getPlayer().openMenu(new MenuProvider() {
                    @Override public @NotNull Component getDisplayName() {
                        return Component.translatable("gui.crinklemod.addon.inventory.button.gear");
                    }

                    @Override public @NotNull AbstractContainerMenu createMenu(int pContainerId, @NotNull Inventory pPlayerInventory, @NotNull Player pPlayer) {
                        return new CrinkleGearMenu(pContainerId, pPlayer);
                    }
                }))
                .push();
    }
}
