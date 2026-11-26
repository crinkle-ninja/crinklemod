package ninja.crinkle.mod.menus;

import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import ninja.crinkle.mod.CrinkleMod;
import ninja.crinkle.mod.client.gui.containers.menus.CrinkleGearMenu;

public class CrinkleMenus {
    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(ForgeRegistries.MENU_TYPES
            , CrinkleMod.MODID);

    public static final RegistryObject<MenuType<DunnyContainer>> DUNNY_CONTAINER = MENU_TYPES.register("dunny",
            () -> IForgeMenuType.create((windowId, inv, data) -> new DunnyContainer(windowId, inv.player,
                    data.readBlockPos())));

    public static final RegistryObject<MenuType<CrinkleGearMenu>> CRINKLE_GEAR_MENU = MENU_TYPES.register("crinkle-gear",
            () -> IForgeMenuType.create(((windowId, inv, data) -> new CrinkleGearMenu(windowId, inv.player))));




    public static void register(IEventBus modEventBus) {
        MENU_TYPES.register(modEventBus);
    }
}
