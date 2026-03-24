package ninja.crinkle.mod.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.ForgeAdvancementProvider;
import net.minecraftforge.data.event.GatherDataEvent;
import ninja.crinkle.mod.CrinkleMod;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class CrinkleDataGeneration {
    public static void generate(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();
        CrinkleBlockTags blockTags = new CrinkleBlockTags(packOutput, lookupProvider, CrinkleMod.MODID,
                event.getExistingFileHelper());
        generator.addProvider(event.includeServer(), blockTags);
        generator.addProvider(event.includeClient(), new CrinkleItemModelProvider(packOutput,
                event.getExistingFileHelper()));
        generator.addProvider(event.includeServer(), new CrinkleRecipes(packOutput));
        generator.addProvider(event.includeClient(), new CrinkleSoundProvider(packOutput,
                event.getExistingFileHelper()));
        generator.addProvider(event.includeServer(), new ForgeAdvancementProvider(packOutput,
                lookupProvider,
                event.getExistingFileHelper(),
                List.of(new CrinkleAdvancements())
        ));
    }
}
