package ninja.crinkle.mod.capabilities;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import ninja.crinkle.mod.CrinkleMod;
import ninja.crinkle.mod.capabilities.versioning.MetabolismVersion;
import ninja.crinkle.mod.metabolism.Metabolism;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class WetProvider implements ICapabilitySerializable<CompoundTag> {
    public static final Capability<WetCapability> CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {
    });
    public static final ResourceLocation RESOURCE_LOCATION = CrinkleMod.loc("metabolism.wet");
    private final Player player;
    private WetCapability storage;
    private final LazyOptional<WetCapability> instance = LazyOptional.of(this::getOrCreateStorage);

    private WetCapability getOrCreateStorage() {
        if (this.storage == null) {
            this.storage = new MetabolismImpl(Metabolism.Type.Wet);
        }
        return this.storage;
    }

    @SubscribeEvent
    public static void onAttachCapabilitiesPlayer(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player p) {
            if (!event.getObject().getCapability(WetProvider.CAPABILITY).isPresent()) {
                event.addCapability(WetProvider.RESOURCE_LOCATION, new WetProvider(p));
            }
        }
    }

    public WetProvider(Player player) {
        this.player = player;
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
        if (cap == CAPABILITY) return instance.cast();
        return LazyOptional.empty();
    }

    /**
     * Serialize the capability to NBT.
     *
     * @return The serialized capability
     */
    @Override
    public CompoundTag serializeNBT() {
        return getOrCreateStorage().serializeNBT();
    }

    /**
     * Deserialize the capability from NBT.
     *
     * @param nbt The serialized capability
     */
    @Override
    public void deserializeNBT(CompoundTag nbt) {
        getOrCreateStorage().deserializeNBT(MetabolismVersion.performUpgrades(this.player, nbt));
    }
}
