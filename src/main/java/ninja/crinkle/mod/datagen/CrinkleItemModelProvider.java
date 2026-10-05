package ninja.crinkle.mod.datagen;

import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.FallbackResourceManager;
import net.minecraft.server.packs.resources.Resource;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import ninja.crinkle.mod.CrinkleMod;
import ninja.crinkle.mod.items.CrinkleItems;
import ninja.crinkle.mod.undergarment.DiaperDesign;
import ninja.crinkle.mod.undergarment.DiaperDesignRegistry;

import java.util.List;
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
        String design_id = DiaperDesignRegistry.PLAIN_ID.getPath();
        withExistingParent(design_id, new ResourceLocation("item" +
                "/generated"))
                .texture("layer0", new ResourceLocation(CrinkleMod.MODID,
                        "item/" + design_id));
    }
}
