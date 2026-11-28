package ninja.crinkle.mod.datagen;

import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import ninja.crinkle.mod.CrinkleMod;
import ninja.crinkle.mod.undergarment.DesignRegistry;

public class CrinkleItemModelProvider extends ItemModelProvider {
    public CrinkleItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, CrinkleMod.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        simpleArmorItem();
    }

    private void simpleArmorItem() {
        String design_id = DesignRegistry.PLAIN_ID.getPath();
        withExistingParent(design_id, ResourceLocation.fromNamespaceAndPath("minecraft", "item" + "/generated"))
                .texture("layer0", CrinkleMod.loc("item/" + design_id));
    }
}
