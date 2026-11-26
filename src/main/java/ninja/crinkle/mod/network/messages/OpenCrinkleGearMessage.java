package ninja.crinkle.mod.network.messages;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import ninja.crinkle.mod.client.gui.containers.menus.CrinkleGearMenu;
import ninja.crinkle.mod.network.CrinkleChannel;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class OpenCrinkleGearMessage {
    private final ItemStack carried;

    public OpenCrinkleGearMessage(ItemStack carried) {
        this.carried = carried;
    }

    public static OpenCrinkleGearMessage decoder(FriendlyByteBuf buf) {
        return new OpenCrinkleGearMessage(buf.readItem());
    }

    public void encoder(FriendlyByteBuf buf) {
        buf.writeItem(carried);
    }

    public void messageConsumer(@NotNull Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context ctx = contextSupplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player != null) {
                ItemStack itemStack = player.isCreative() ? carried : player.containerMenu.getCarried();
                player.containerMenu.setCarried(ItemStack.EMPTY);
                player.openMenu(new MenuProvider() {
                    @Override public @NotNull Component getDisplayName() {
                        return Component.translatable("gui.crinklemod.addon.inventory.button.gear");
                    }

                    @Override public @NotNull AbstractContainerMenu createMenu(int pContainerId, @NotNull Inventory pPlayerInventory, @NotNull Player pPlayer) {
                        return new CrinkleGearMenu(pContainerId, pPlayer);
                    }
                });
                if (!itemStack.isEmpty()) {
                    player.containerMenu.setCarried(itemStack);
                    CrinkleChannel.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new CarriedItemUpdateMessage(itemStack));
                }
            }
        });
        ctx.setPacketHandled(true);
    }
}
