package ninja.crinkle.mod.network.messages;

import com.mojang.logging.LogUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;
import ninja.crinkle.mod.client.textures.Textures;
import ninja.crinkle.mod.undergarment.DiaperDesign;
import ninja.crinkle.mod.undergarment.DiaperDesignRegistry;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

public class DiaperDesignSyncMessage {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Map<ResourceLocation, DiaperDesign> designs;

    public DiaperDesignSyncMessage(Map<ResourceLocation, DiaperDesign> designs) {
        this.designs = designs;
    }

    DiaperDesignSyncMessage(@NotNull FriendlyByteBuf buffer) {
        int count = buffer.readInt();
        this.designs = new LinkedHashMap<>();
        for (int i = 0; i < count; i++) {
            DiaperDesign design = DiaperDesign.fromNetwork(buffer);
            this.designs.put(design.id(), design);
        }
    }

    @Contract("_ -> new")
    public static @NotNull DiaperDesignSyncMessage decoder(FriendlyByteBuf buffer) {
        return new DiaperDesignSyncMessage(buffer);
    }

    public void encoder(@NotNull FriendlyByteBuf buffer) {
        buffer.writeInt(designs.size());
        for (DiaperDesign design : designs.values()) {
            design.toNetwork(buffer);
        }
    }

    public void messageConsumer(@NotNull Supplier<NetworkEvent.Context> ctx) {
        DiaperDesignRegistry.setClientDesigns(designs);
        Textures.rebuildGenerators();
    }

    public static DiaperDesignSyncMessage fromServerRegistry() {
        return new DiaperDesignSyncMessage(DiaperDesignRegistry.getServerDesigns());
    }
}