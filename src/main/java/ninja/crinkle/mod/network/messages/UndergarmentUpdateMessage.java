package ninja.crinkle.mod.network.messages;

import com.mojang.logging.LogUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import ninja.crinkle.mod.network.CrinkleChannel;
import ninja.crinkle.mod.undergarment.Undergarment;
import ninja.crinkle.mod.util.ClientUtil;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.function.Supplier;

public class UndergarmentUpdateMessage {
    private static final Logger LOGGER = LogUtils.getLogger();
    private Integer liquids;
    private Integer maxLiquids;
    private Integer maxSolids;
    private Integer solids;

    UndergarmentUpdateMessage(@Nullable Integer liquids, @Nullable Integer solids,
                              @Nullable Integer maxLiquids, @Nullable Integer maxSolids) {
        this.liquids = liquids;
        this.solids = solids;
        this.maxLiquids = maxLiquids;
        this.maxSolids = maxSolids;
    }

    UndergarmentUpdateMessage(@NotNull FriendlyByteBuf buffer) {
        if (buffer.readBoolean()) {
            this.liquids = buffer.readInt();
        }
        if (buffer.readBoolean()) {
            this.solids = buffer.readInt();
        }
        if (buffer.readBoolean()) {
            this.maxLiquids = buffer.readInt();
        }
        if (buffer.readBoolean()) {
            this.maxSolids = buffer.readInt();
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    @Contract("_ -> new")
    public static @NotNull UndergarmentUpdateMessage decoder(FriendlyByteBuf buffer) {
        return new UndergarmentUpdateMessage(buffer);
    }

    public void encoder(@NotNull FriendlyByteBuf buffer) {
        buffer.writeBoolean(this.liquids != null);
        if (this.liquids != null) {
            buffer.writeInt(this.liquids);
        }
        buffer.writeBoolean(this.solids != null);
        if (this.solids != null) {
            buffer.writeInt(this.solids);
        }
        buffer.writeBoolean(this.maxLiquids != null);
        if (this.maxLiquids != null) {
            buffer.writeInt(this.maxLiquids);
        }
        buffer.writeBoolean(this.maxSolids != null);
        if (this.maxSolids != null) {
            buffer.writeInt(this.maxSolids);
        }
    }

    public void messageConsumer(@NotNull Supplier<NetworkEvent.Context> ctx) {
        Player player = ctx.get().getSender() != null ? ctx.get().getSender() : ClientUtil.getPlayer();
        if (player == null) {
            LOGGER.warn("Failed to update undergarment of player");
            ctx.get().setPacketHandled(false);
            return;
        }
        ItemStack itemStack = Undergarment.getWornUndergarment(player);
        if (itemStack.isEmpty()) {
            LOGGER.warn("Failed to update undergarment of player: no undergarment found");
            ctx.get().setPacketHandled(false);
            return;
        }
        Undergarment undergarment = Undergarment.of(itemStack);
        if (this.liquids != null) {
            undergarment.setLiquids(this.liquids);
        }
        if (this.solids != null) {
            undergarment.setSolids(this.solids);
        }
        if (this.maxLiquids != null) {
            undergarment.setMaxLiquids(this.maxLiquids);
        }
        if (this.maxSolids != null) {
            undergarment.setMaxSolids(this.maxSolids);
        }
    }

    public void sendToServer() {
        CrinkleChannel.INSTANCE.sendToServer(this);
    }

    public static class Builder {
        private Integer liquids;
        private Integer maxLiquids;
        private Integer maxSolids;
        private Integer solids;

        Builder() {
        }

        public UndergarmentUpdateMessage build() {
            return new UndergarmentUpdateMessage(this.liquids, this.solids, this.maxLiquids, this.maxSolids);
        }

        public Builder liquids(int liquids) {
            this.liquids = liquids;
            return this;
        }

        public Builder maxLiquids(int maxLiquids) {
            this.maxLiquids = maxLiquids;
            return this;
        }

        public Builder maxSolids(int maxSolids) {
            this.maxSolids = maxSolids;
            return this;
        }

        public Builder solids(int solids) {
            this.solids = solids;
            return this;
        }
    }
}
