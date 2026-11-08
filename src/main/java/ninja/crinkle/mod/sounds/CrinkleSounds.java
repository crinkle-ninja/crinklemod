package ninja.crinkle.mod.sounds;

import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import ninja.crinkle.mod.CrinkleMod;

public class CrinkleSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, CrinkleMod.MODID);

    public static final RegistryObject<SoundEvent> CRINKLE_SOUND = SOUNDS.register("crinkle_sound",
            () -> SoundEvent.createFixedRangeEvent(CrinkleMod.loc("crinkle_sound"), 1f));

    public static void register(IEventBus bus) {
        SOUNDS.register(bus);
    }
}
