package ninja.crinkle.mod.network.messages;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import ninja.crinkle.mod.util.ClientUtil;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class CarriedItemUpdateMessage {
    private final ItemStack carried;

    public CarriedItemUpdateMessage(ItemStack carried) {
        this.carried = carried;
    }

    public static CarriedItemUpdateMessage decoder(FriendlyByteBuf buf) {
        return new CarriedItemUpdateMessage(buf.readItem());
    }

    public void encoder(FriendlyByteBuf buf) {
        buf.writeItem(carried);
    }

    public void messageConsumer(@NotNull Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context ctx = contextSupplier.get();
        ctx.enqueueWork(() -> {
            LocalPlayer player = ClientUtil.getMinecraft().player;
            if (player != null) {
                player.containerMenu.setCarried(carried);
            }
        });
        ctx.setPacketHandled(true);
    }
}
