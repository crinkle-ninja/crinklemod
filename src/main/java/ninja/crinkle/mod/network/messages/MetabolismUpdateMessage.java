package ninja.crinkle.mod.network.messages;

import com.mojang.logging.LogUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;
import ninja.crinkle.mod.capabilities.IMetabolism;
import ninja.crinkle.mod.capabilities.MetabolismCapabilities;
import ninja.crinkle.mod.capabilities.MetabolismImpl;
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
    private final int indicatorPositionX;
    private final int indicatorPositionY;
    private final double numberOneChance;
    private final boolean numberOneEnabled;
    private final int numberOneRolls;
    private final int numberOneSafeRolls;
    private final double numberTwoChance;
    private final boolean numberTwoEnabled;
    private final int numberTwoRolls;
    private final int numberTwoSafeRolls;
    private final int timer;

    /**
     * Create a new update message from a metabolism
     *
     * @param metabolism The metabolism to create the message from
     * @see IMetabolism
     */
    public MetabolismUpdateMessage(@NotNull IMetabolism metabolism) {
        this.numberOneEnabled = metabolism.isNumberOneEnabled();
        this.numberTwoEnabled = metabolism.isNumberTwoEnabled();
        this.timer = metabolism.getTimer();
        this.numberOneRolls = metabolism.getNumberOneRolls();
        this.numberOneSafeRolls = metabolism.getNumberOneSafeRolls();
        this.numberOneChance = metabolism.getNumberOneChance();
        this.numberTwoRolls = metabolism.getNumberTwoRolls();
        this.numberTwoSafeRolls = metabolism.getNumberTwoSafeRolls();
        this.numberTwoChance = metabolism.getNumberTwoChance();
        this.indicatorPositionX = metabolism.getIndicatorPositionX();
        this.indicatorPositionY = metabolism.getIndicatorPositionY();
    }

    /**
     * Create a new update message from a buffer
     *
     * @param buffer The buffer to create the message from
     * @see FriendlyByteBuf
     */
    public MetabolismUpdateMessage(@NotNull FriendlyByteBuf buffer) {
        this.numberOneEnabled = buffer.readBoolean();
        this.numberTwoEnabled = buffer.readBoolean();
        this.timer = buffer.readInt();
        this.numberOneRolls = buffer.readInt();
        this.numberOneSafeRolls = buffer.readInt();
        this.numberOneChance = buffer.readDouble();
        this.numberTwoRolls = buffer.readInt();
        this.numberTwoSafeRolls = buffer.readInt();
        this.numberTwoChance = buffer.readDouble();
        this.indicatorPositionX = buffer.readInt();
        this.indicatorPositionY = buffer.readInt();
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
        buffer.writeBoolean(numberOneEnabled);
        buffer.writeBoolean(numberTwoEnabled);
        buffer.writeInt(timer);
        buffer.writeInt(numberOneRolls);
        buffer.writeInt(numberOneSafeRolls);
        buffer.writeDouble(numberOneChance);
        buffer.writeInt(numberTwoRolls);
        buffer.writeInt(numberTwoSafeRolls);
        buffer.writeDouble(numberTwoChance);
        buffer.writeInt(indicatorPositionX);
        buffer.writeInt(indicatorPositionY);
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

        IMetabolism metabolism = player.getCapability(MetabolismCapabilities.METABOLISM).orElseThrow(() ->
                new IllegalStateException("Player does not have a metabolism capability"));
        metabolism.setNumberOneEnabled(numberOneEnabled);
        metabolism.setNumberTwoEnabled(numberTwoEnabled);
        metabolism.setTimer(timer);
        metabolism.setNumberOneRolls(numberOneRolls);
        metabolism.setNumberOneSafeRolls(numberOneSafeRolls);
        metabolism.setNumberOneChance(numberOneChance);
        metabolism.setNumberTwoRolls(numberTwoRolls);
        metabolism.setNumberTwoSafeRolls(numberTwoSafeRolls);
        metabolism.setNumberTwoChance(numberTwoChance);
        metabolism.setIndicatorPositionX(indicatorPositionX);
        metabolism.setIndicatorPositionY(indicatorPositionY);
        ctx.get().setPacketHandled(true);
    }
}