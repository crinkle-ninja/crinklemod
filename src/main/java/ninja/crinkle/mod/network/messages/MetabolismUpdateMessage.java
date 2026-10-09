package ninja.crinkle.mod.network.messages;

import com.mojang.logging.LogUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;
import ninja.crinkle.mod.capabilities.IMetabolism;
import ninja.crinkle.mod.capabilities.MetabolismImpl;
import ninja.crinkle.mod.events.CrinkleEvent;
import ninja.crinkle.mod.metabolism.Metabolism;
import ninja.crinkle.mod.metabolism.Pang;
import ninja.crinkle.mod.util.ClientUtil;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.function.Supplier;

/**
 * A message that is used to sync the metabolism of a player to the client or server.
 *
 * @see MetabolismImpl
 */
public class MetabolismUpdateMessage {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Metabolism.Type type;
    private final boolean enabled;
    private final double control;
    private final int interval;
    private final double controlDecay;
    private final double intervalDecay;
    private final double intensity;
    private final double currentControl;
    private final Pang pang;
    private final int pangDuration;
    private final double currentInterval;
    private final int tickCount;

    /**
     * Create a new update message from a metabolism
     *
     * @param metabolism The metabolism to create the message from
     * @see IMetabolism
     */
    public MetabolismUpdateMessage(@NotNull Metabolism metabolism) {
        this.type = metabolism.type();
        this.enabled = metabolism.enabled();
        this.control = metabolism.control();
        this.interval = metabolism.interval();
        this.controlDecay = metabolism.controlDecay();
        this.intervalDecay = metabolism.intervalDecay();
        this.intensity = metabolism.intensity();
        this.currentControl = metabolism.currentControl();
        this.pang = metabolism.pang();
        this.pangDuration = metabolism.pangDuration();
        this.currentInterval = metabolism.currentInterval();
        this.tickCount = metabolism.tickCount();
    }

    /**
     * Create a new update message from a buffer
     *
     * @param buffer The buffer to create the message from
     * @see FriendlyByteBuf
     */
    public MetabolismUpdateMessage(@NotNull FriendlyByteBuf buffer) {
        this.type = buffer.readEnum(Metabolism.Type.class);
        this.enabled = buffer.readBoolean();
        this.control = buffer.readDouble();
        this.interval = buffer.readInt();
        this.controlDecay = buffer.readDouble();
        this.intervalDecay = buffer.readDouble();
        this.intensity = buffer.readDouble();
        this.currentControl = buffer.readDouble();
        this.pang = buffer.readEnum(Pang.class);
        this.pangDuration = buffer.readInt();
        this.currentInterval = buffer.readDouble();
        this.tickCount = buffer.readInt();
    }

    /**
     * Decode a message from a buffer
     *
     * @param buffer The buffer to decode the message from
     * @return The decoded message
     */
    @Contract("_ -> new")
    public static @NotNull MetabolismUpdateMessage decoder(FriendlyByteBuf buffer) {
        return new MetabolismUpdateMessage(buffer);
    }

    /**
     * Encode a message to a buffer
     *
     * @param buffer The buffer to encode the message to
     * @implSpec The order of the encoded values must match the order of the decoded values found in the constructor
     */
    public void encoder(@NotNull FriendlyByteBuf buffer) {
        buffer.writeEnum(this.type);
        buffer.writeBoolean(this.enabled);
        buffer.writeDouble(this.control);
        buffer.writeInt(this.interval);
        buffer.writeDouble(this.controlDecay);
        buffer.writeDouble(this.intervalDecay);
        buffer.writeDouble(this.intensity);
        buffer.writeDouble(this.currentControl);
        buffer.writeEnum(this.pang);
        buffer.writeInt(this.pangDuration);
        buffer.writeDouble(this.currentInterval);
        buffer.writeInt(this.tickCount);
    }

    /**
     * Consume a message and update the player's metabolism capability
     *
     * @param ctx The context of the message
     */
    public void messageConsumer(@NotNull Supplier<NetworkEvent.Context> ctx) {
        Player player = ctx.get().getSender() != null ? ctx.get().getSender() : ClientUtil.getPlayer();
        if (player == null) {
            LOGGER.warn("Failed to update metabolism of player");
            ctx.get().setPacketHandled(false);
            return;
        }
        Metabolism metabolism = Metabolism.of(player, type);
        metabolism.enabled(enabled);
        metabolism.control(control);
        metabolism.interval(interval);
        metabolism.controlDecay(controlDecay);
        metabolism.intervalDecay(intervalDecay);
        metabolism.intensity(intensity);
        metabolism.currentControl(currentControl);
        metabolism.pang(pang);
        metabolism.currentInterval(currentInterval);
        metabolism.pangDuration(pangDuration);
        metabolism.tickCount(tickCount);
        ctx.get().setPacketHandled(true);
        LOGGER.trace("({}) [{}] Consuming message", CrinkleEvent.side().name(), metabolism.type());
    }
}