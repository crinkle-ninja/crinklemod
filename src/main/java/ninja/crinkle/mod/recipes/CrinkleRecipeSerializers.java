package ninja.crinkle.mod.recipes;

import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import ninja.crinkle.mod.CrinkleMod;

public class CrinkleRecipeSerializers {
    private static final DeferredRegister<RecipeSerializer<?>> REGISTER =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, CrinkleMod.MODID);

    public static final RegistryObject<RecipeSerializer<DesignRecipe>> DESIGN_SERIALIZER =
            REGISTER.register("design_serializer", DesignRecipe.Serializer::new);

    public static void register(IEventBus modEventBus) {
        REGISTER.register(modEventBus);
    }
}
