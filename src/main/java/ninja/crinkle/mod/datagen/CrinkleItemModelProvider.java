package ninja.crinkle.mod.datagen;

import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import ninja.crinkle.mod.CrinkleMod;
import ninja.crinkle.mod.items.CrinkleItems;

import java.util.Objects;

public class CrinkleItemModelProvider extends ItemModelProvider {
    public CrinkleItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, CrinkleMod.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        simpleArmorItem();
    }

    private void simpleArmorItem() {
        withExistingParent(Objects.requireNonNull(CrinkleItems.DIAPER.getId()).getPath(), new ResourceLocation("item" +
                "/generated"))
                .texture("layer0", new ResourceLocation(CrinkleMod.MODID,
                        "item/" + CrinkleItems.DIAPER.getId().getPath()));
    }
}
