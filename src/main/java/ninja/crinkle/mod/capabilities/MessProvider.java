package ninja.crinkle.mod.capabilities;

import com.mojang.logging.LogUtils;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import ninja.crinkle.mod.CrinkleMod;
import ninja.crinkle.mod.capabilities.versioning.MetabolismVersion;
import ninja.crinkle.mod.metabolism.Metabolism;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class MessProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {
    public static final String NAME_FORMAT = "metabolism.mess";
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Player player;
    private final MessCapability storage;
    private final LazyOptional<MessCapability> instance;

    public MessProvider(Player player) {
        this.player = player;
        this.storage = new MetabolismImpl(Metabolism.Type.Mess);
        this.instance = LazyOptional.of(() -> storage);
    }

    /**
     * Attach the metabolism capability to an entity.
     *
     * @param event The event that is fired when an entity is created and ready for attachment.
     * @see net.minecraftforge.event.AttachCapabilitiesEvent
     */
    public static void attach(final @NotNull AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player player) {
            LOGGER.debug("Attaching mess metabolism capability to player");
            event.addCapability(new ResourceLocation(CrinkleMod.MODID, NAME_FORMAT), new MessProvider(player));
        }
    }

    /**
     * Get the capability of an entity.
     *
     * @param cap  The capability to check
     * @param side The Side to check from,
     *             <strong>CAN BE NULL</strong>. Null is defined to represent 'internal' or 'self'
     * @param <T>  The type of the capability
     * @return The capability if it exists, or an empty optional if it does not
     */
    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return MetabolismCapabilities.MESS.orEmpty(cap, instance);
    }

    /**
     * Serialize the capability to NBT.
     *
     * @return The serialized capability
     */
    @Override
    public CompoundTag serializeNBT() {
        return storage.serializeNBT();
    }

    /**
     * Deserialize the capability from NBT.
     *
     * @param nbt The serialized capability
     */
    @Override
    public void deserializeNBT(CompoundTag nbt) {
        storage.deserializeNBT(MetabolismVersion.performUpgrades(this.player, nbt));
    }
}