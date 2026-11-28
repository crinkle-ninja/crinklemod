package ninja.crinkle.mod.integrations;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.ingredients.subtypes.IIngredientSubtypeInterpreter;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import net.minecraft.resources.ResourceLocation;
import ninja.crinkle.mod.CrinkleMod;
import ninja.crinkle.mod.items.CrinkleItems;
import ninja.crinkle.mod.recipes.DesignRecipe;
import ninja.crinkle.mod.undergarment.Undergarment;
import org.jetbrains.annotations.NotNull;

@JeiPlugin
public class JeiModPlugin implements IModPlugin {
    @Override public @NotNull ResourceLocation getPluginUid() {
        return CrinkleMod.loc("jei_plugin");
    }

    @Override
    public void registerItemSubtypes(ISubtypeRegistration registration) {
        registration.registerSubtypeInterpreter(
                CrinkleItems.DIAPER.get(),
                (stack, context) -> Undergarment.of(stack).getDesign()
                        .map(d -> d.id().toString())
                        .orElse(IIngredientSubtypeInterpreter.NONE));
    }
}
