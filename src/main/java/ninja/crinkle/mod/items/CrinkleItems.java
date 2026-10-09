package ninja.crinkle.mod.items;

import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import ninja.crinkle.mod.CrinkleMod;
import ninja.crinkle.mod.items.custom.DiaperArmorItem;
import ninja.crinkle.mod.items.custom.DunnyItem;

import static ninja.crinkle.mod.blocks.CrinkleBlocks.DUNNY_BLOCK;

public class CrinkleItems {
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS,
            CrinkleMod.MODID);

    public static final RegistryObject<Item> DUNNY_BLOCK_ITEM = ITEMS.register("dunny",
            () -> new DunnyItem(DUNNY_BLOCK.get(), new Item.Properties()));

    public static final RegistryObject<Item> DIAPER = ITEMS.register("diaper",
            () -> new DiaperArmorItem(CrinkleArmorMaterials.DIAPER,
                    new Item.Properties().stacksTo(16)));

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
    }
}