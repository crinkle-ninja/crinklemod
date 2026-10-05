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
    private final double training;
    private final int ticks;
    private final double slopeDegradation;
    private final double frequencyCompression;
    private final double intensity;
    private final double currentTraining;
    private final Pang pang;
    private final int pangDuration;
    private final double currentFrequency;
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
        this.training = metabolism.training();
        this.ticks = metabolism.ticks();
        this.slopeDegradation = metabolism.slopeDegradation();
        this.frequencyCompression = metabolism.frequencyCompression();
        this.intensity = metabolism.intensity();
        this.currentTraining = metabolism.currentTraining();
        this.pang = metabolism.pang();
        this.pangDuration = metabolism.pangDuration();
        this.currentFrequency = metabolism.currentFrequency();
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
        this.training = buffer.readDouble();
        this.ticks = buffer.readInt();
        this.slopeDegradation = buffer.readDouble();
        this.frequencyCompression = buffer.readDouble();
        this.intensity = buffer.readDouble();
        this.currentTraining = buffer.readDouble();
        this.pang = buffer.readEnum(Pang.class);
        this.pangDuration = buffer.readInt();
        this.currentFrequency = buffer.readDouble();
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
        buffer.writeDouble(this.training);
        buffer.writeInt(this.ticks);
        buffer.writeDouble(this.slopeDegradation);
        buffer.writeDouble(this.frequencyCompression);
        buffer.writeDouble(this.intensity);
        buffer.writeDouble(this.currentTraining);
        buffer.writeEnum(this.pang);
        buffer.writeInt(this.pangDuration);
        buffer.writeDouble(this.currentFrequency);
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
        metabolism.training(training);
        metabolism.ticks(ticks);
        metabolism.slopeDegradation(slopeDegradation);
        metabolism.frequencyCompression(frequencyCompression);
        metabolism.intensity(intensity);
        metabolism.currentTraining(currentTraining);
        metabolism.pang(pang);
        metabolism.currentFrequency(currentFrequency);
        metabolism.pangDuration(pangDuration);
        metabolism.tickCount(tickCount);
        ctx.get().setPacketHandled(true);
        LOGGER.trace("({}) [{}] Consuming message", CrinkleEvent.side().name(), metabolism.type());
    }
}