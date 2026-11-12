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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CrinkleGearProvider  implements ICapabilitySerializable<CompoundTag> {
    public static final Capability<CrinkleGear> CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {
    });
    public static final ResourceLocation RESOURCE_LOCATION = CrinkleMod.loc("gear");
    private CrinkleGear storage;
    private final LazyOptional<CrinkleGear> instance = LazyOptional.of(this::getOrCreateStorage);

    @SubscribeEvent
    public static void onAttachCapabilitiesPlayer(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player) {
            if (!event.getObject().getCapability(CrinkleGearProvider.CAPABILITY).isPresent()) {
                event.addCapability(CrinkleGearProvider.RESOURCE_LOCATION, new CrinkleGearProvider());
            }
        }
    }

    private CrinkleGear getOrCreateStorage() {
        if (this.storage == null) {
            this.storage = new CrinkleGearImpl();
        }
        return this.storage;
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
        if (cap == CAPABILITY) {
            return instance.cast();
        }
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
        getOrCreateStorage().deserializeNBT(nbt);
    }
}
