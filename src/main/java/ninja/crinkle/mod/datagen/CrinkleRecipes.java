package ninja.crinkle.mod.datagen;

import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import ninja.crinkle.mod.items.CrinkleItems;
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
