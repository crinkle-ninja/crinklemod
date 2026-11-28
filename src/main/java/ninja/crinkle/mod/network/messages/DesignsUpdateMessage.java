package ninja.crinkle.mod.network.messages;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import ninja.crinkle.mod.undergarment.Design;
import ninja.crinkle.mod.undergarment.DesignRegistry;

import java.util.function.Supplier;

public record DesignsUpdateMessage(java.util.Collection<Design> designs) {
    public static void encoder(DesignsUpdateMessage message, FriendlyByteBuf buf) {
        buf.writeCollection(message.designs(), Design::toNetwork);
    }

    public static DesignsUpdateMessage decoder(FriendlyByteBuf buf) {
        return new DesignsUpdateMessage(buf.readList(Design::fromNetwork));
    }

    public static void messageConsumer(DesignsUpdateMessage message, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DesignRegistry.replaceAllDesigns(message.designs()));
        ctx.get().setPacketHandled(true);
    }

}
