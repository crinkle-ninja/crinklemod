package ninja.crinkle.mod.menus;

import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import ninja.crinkle.mod.CrinkleMod;

public class CrinkleMenus {
    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(ForgeRegistries.MENU_TYPES
            , CrinkleMod.MODID);

    public static final RegistryObject<MenuType<DunnyContainer>> DUNNY_CONTAINER = MENU_TYPES.register("dunny",
            () -> IForgeMenuType.create((windowId, inv, data) -> new DunnyContainer(windowId, inv.player,
                    data.readBlockPos())));

    public static void register(IEventBus modEventBus) {
        MENU_TYPES.register(modEventBus);
    }
}
