package ninja.crinkle.mod.datagen;

import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.data.SoundDefinition;
import net.minecraftforge.common.data.SoundDefinitionsProvider;
import ninja.crinkle.mod.CrinkleMod;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class CrinkleSoundProvider extends SoundDefinitionsProvider {
    private static final int COUNT = 6;
    private static final String PREFIX = "crinkle";

    /**
     * Creates a new instance of this data provider.
     *
     * @param output The {@linkplain PackOutput} instance provided by the data generator.
     * @param helper The existing file helper provided by the event you are initializing this provider in.
     */
    protected CrinkleSoundProvider(PackOutput output, ExistingFileHelper helper) {
        super(output, CrinkleMod.MODID, helper);
    }

    @Override
    public void registerSounds() {
        List<SoundDefinition.Sound> sounds = new ArrayList<>();
        IntStream.range(0, COUNT).forEach(i ->
                sounds.add(
                        sound(new ResourceLocation(CrinkleMod.MODID, String.format("%s%02d", PREFIX, i)))
                                .preload()
                ));
        this.add("crinkle_sound", definition()
                .subtitle("sound.crinklemod.crinkle_sound.subtitle")
                .with(sounds.toArray(new SoundDefinition.Sound[0]))
        );
    }
}
