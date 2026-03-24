package ninja.crinkle.mod.network.messages;

import com.mojang.logging.LogUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;
import ninja.crinkle.mod.CrinkleMod;
import ninja.crinkle.mod.events.AccidentEvent;
import ninja.crinkle.mod.events.CrinkleEvent;
import ninja.crinkle.mod.util.ClientUtil;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.function.Supplier;

public class AccidentEventMessage {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final int amount;
    private final CrinkleEvent.Type type;

    public AccidentEventMessage(CrinkleEvent.Type type, int amount) {
        this.type = type;
        this.amount = amount;
    }

    public AccidentEventMessage(FriendlyByteBuf buf) {
        this.type = CrinkleEvent.Type.values()[buf.readInt()];
        this.amount = buf.readInt();
    }

    public static AccidentEventMessage decoder(FriendlyByteBuf buf) {
        return new AccidentEventMessage(buf);
    }

    public void encoder(FriendlyByteBuf buf) {
        buf.writeInt(this.type.ordinal());
        buf.writeInt(this.amount);
    }

    public void messageConsumer(@NotNull Supplier<NetworkEvent.Context> ctx) {
        AccidentEvent.Side side = ctx.get().getDirection().getOriginationSide().isClient() ?
                AccidentEvent.Side.CLIENT : AccidentEvent.Side.SERVER;
        Player player = ctx.get().getSender() != null ? ctx.get().getSender() : ClientUtil.getPlayer();
        if (player == null) {
            ctx.get().setPacketHandled(false);
            return;
        }
        LOGGER.debug("Received {} accident event message from {} with amount {} from {}", this.type,
                player.getName().getString(), this.amount, side);
        CrinkleMod.EVENT_BUS.post(new AccidentEvent(player, this.amount, side, type));
    }

}
