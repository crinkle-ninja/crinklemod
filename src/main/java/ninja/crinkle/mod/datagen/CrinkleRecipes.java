package ninja.crinkle.mod.datagen;

import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.world.item.Items;
import ninja.crinkle.mod.items.CrinkleItems;
import ninja.crinkle.mod.recipes.DesignRecipe;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class CrinkleRecipes extends RecipeProvider {
    public CrinkleRecipes(PackOutput pOutput) {
        super(pOutput);
    }

    @Override
    protected void buildRecipes(@NotNull Consumer<FinishedRecipe> consumer) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CrinkleItems.DIAPER.get())
                .unlockedBy("has_paper", has(Items.PAPER))
                .pattern("ppp")
                .pattern(" p ")
                .pattern("ppp")
                .define('p', Items.PAPER)
                .save(consumer);
    }
}
