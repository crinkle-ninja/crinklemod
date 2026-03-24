package ninja.crinkle.mod.items;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import ninja.crinkle.mod.CrinkleMod;
import ninja.crinkle.mod.items.custom.DiaperArmorItem;
import ninja.crinkle.mod.undergarment.DiaperDesign;
import ninja.crinkle.mod.undergarment.DiaperDesignRegistry;

import java.util.Map;

public class CrinkleTabs {
    private static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, CrinkleMod.MODID);

    public static final RegistryObject<CreativeModeTab> CRINKLE_TAB = CREATIVE_MODE_TABS.register("crinkle_tab",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(CrinkleItems.DUNNY_BLOCK_ITEM.get()))
                    .displayItems((p, o) -> {
                        o.accept(CrinkleItems.DUNNY_BLOCK_ITEM.get());
                        Map<ResourceLocation, DiaperDesign> designs =
                                DiaperDesignRegistry.getServerDesigns();
                        if (designs.isEmpty()) {
                            o.accept(CrinkleItems.DIAPER.get());
                        } else {
                            for (Map.Entry<ResourceLocation, DiaperDesign> entry : designs.entrySet()) {
                                ItemStack stack = new ItemStack(CrinkleItems.DIAPER.get());
                                DiaperArmorItem.setDesignId(stack, entry.getKey());
                                o.accept(stack);
                            }
                        }
                    })
                    .title(Component.translatable("tab.crinklemod.crinkle"))
                    .build());

    public static void register(IEventBus bus) {
        CREATIVE_MODE_TABS.register(bus);
    }
}