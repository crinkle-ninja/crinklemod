package ninja.crinkle.mod.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.data.ExistingFileHelper;
import ninja.crinkle.mod.CrinkleMod;
import ninja.crinkle.mod.items.CrinkleItems;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class CrinkleItemTags extends ItemTagsProvider {
    public static TagKey<Item> UNDERGARMENTS = bind("undergarments");
    public static TagKey<Item> DIAPERS = bind("diapers");

    public CrinkleItemTags(PackOutput pOutput, CompletableFuture<HolderLookup.Provider> pLookupProvider, CompletableFuture<TagLookup<Block>> pBlockTags, String modId, @Nullable ExistingFileHelper existingFileHelper) {
        super(pOutput, pLookupProvider, pBlockTags, modId, existingFileHelper);
    }


    @Override protected void addTags(HolderLookup.@NotNull Provider pProvider) {
        tag(UNDERGARMENTS).add(CrinkleItems.DIAPER.get());
        tag(DIAPERS).add(CrinkleItems.DIAPER.get());
    }

    private static TagKey<Item> bind(String pName) {
        return TagKey.create(Registries.ITEM, CrinkleMod.loc(pName));
    }
}
