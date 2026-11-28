package ninja.crinkle.mod.mixin;

import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.crafting.RecipeManager;
import ninja.crinkle.mod.recipes.DesignRecipe;
import ninja.crinkle.mod.undergarment.DesignRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(RecipeManager.class)
public class RecipeManagerMixin {

    @Inject(method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V",
            at = @At("HEAD"))
    private void crinklemod$injectDesignRecipes(Map<ResourceLocation, JsonElement> map,
                                             ResourceManager resourceManager,
                                             ProfilerFiller profiler,
                                             CallbackInfo ci) {
        DesignRegistry.loadDesigns(resourceManager);
        DesignRecipe.Generator.addRecipes(map, resourceManager);
    }
}
