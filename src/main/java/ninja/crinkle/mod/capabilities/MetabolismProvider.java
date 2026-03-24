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
import ninja.crinkle.mod.capabilities.versioning.MetabolismVersions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

/**
 * Metabolism provider class.
 * This class is used to provide the metabolism capability to an entity.
 *
 * @see IMetabolism
 * @see net.minecraftforge.common.capabilities.ICapabilityProvider
 * @see net.minecraftforge.common.util.INBTSerializable
 */
public class MetabolismProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {
    public static final String NAME = "metabolism";
    public static final ResourceLocation IDENTIFIER = new ResourceLocation(CrinkleMod.MODID, NAME);
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Player player;
    private final IMetabolism storage = new MetabolismImpl();
    private final LazyOptional<IMetabolism> instance = LazyOptional.of(() -> storage);

    public MetabolismProvider(Player player) {
        this.player = player;
    }

    /**
     * Attach the metabolism capability to an entity.
     *
     * @param event The event that is fired when an entity is created and ready for attachment.
     * @see net.minecraftforge.event.AttachCapabilitiesEvent
     */
    public static void attach(final @NotNull AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player player) {
            LOGGER.debug("Attaching metabolism capability to player");
            event.addCapability(IDENTIFIER, new MetabolismProvider(player));
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
        return MetabolismCapabilities.METABOLISM.orEmpty(cap, instance);
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
        storage.deserializeNBT(MetabolismVersions.performUpgrades(this.player, nbt));
    }
}
