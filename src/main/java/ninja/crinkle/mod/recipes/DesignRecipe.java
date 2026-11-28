package ninja.crinkle.mod.recipes;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.logging.LogUtils;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.RecipeMatcher;
import net.minecraftforge.registries.ForgeRegistries;
import ninja.crinkle.mod.CrinkleMod;
import ninja.crinkle.mod.undergarment.Design;
import ninja.crinkle.mod.undergarment.DesignRegistry;
import ninja.crinkle.mod.undergarment.Undergarment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DesignRecipe implements CraftingRecipe {
    private final Item baseItem;
    private final Design design;
    private final ResourceLocation id;

    public DesignRecipe(ResourceLocation id, Item baseItem, Design design) {
        this.id = id;
        this.baseItem = baseItem;
        this.design = design;
    }

    @Override public @NotNull RecipeType<?> getType() {
        return RecipeType.CRAFTING;
    }

    @Override public @NotNull CraftingBookCategory category() {
        return CraftingBookCategory.EQUIPMENT;
    }

    @Override public boolean matches(@NotNull CraftingContainer pContainer, @NotNull Level pLevel) {
        List<ItemStack> inputs = new ArrayList<>();
        for (int i = 0; i < pContainer.getContainerSize(); i++) {
            ItemStack stack = pContainer.getItem(i);
            if (!stack.isEmpty()) {
                inputs.add(stack);
            }
        }
        return inputs.size() == design().ingredients().size() && RecipeMatcher.findMatches(inputs,
                design().ingredients()) != null;
    }

    @Override public @NotNull ItemStack assemble(@NotNull CraftingContainer pContainer,
                                                 @NotNull RegistryAccess pRegistryAccess) {
        ItemStack existing = ItemStack.EMPTY;
        for (int i = 0; i < pContainer.getContainerSize(); i++) {
            ItemStack slotItem = pContainer.getItem(i);
            if (slotItem.is(baseItem())) {
                existing = slotItem;
            }
        }
        ItemStack finished = existing.copyWithCount(1);
        Undergarment.of(finished).setDesign(design());
        return finished;
    }

    public Item baseItem() {
        return baseItem;
    }

    @Override public boolean canCraftInDimensions(int pWidth, int pHeight) {
        return pWidth * pHeight >= 2;
    }

    @Override public @NotNull ItemStack getResultItem(@NotNull RegistryAccess pRegistryAccess) {
        ItemStack preview = new ItemStack(baseItem());
        Undergarment.of(preview).setDesign(design());
        return preview;
    }

    @Override public @NotNull NonNullList<Ingredient> getIngredients() {
        return design().ingredients();
    }

    @Override public @NotNull ResourceLocation getId() {
        return id;
    }

    @Override public @NotNull RecipeSerializer<?> getSerializer() {
        return CrinkleRecipeSerializers.DESIGN_SERIALIZER.get();
    }

    public Design design() {
        return this.design;
    }

    public static class Generator {
        private static final Logger LOGGER = LogUtils.getLogger();

        public static void addRecipes(Map<ResourceLocation, JsonElement> map, ResourceManager resourceManager) {
            Map<ResourceLocation, Resource> found =
                    resourceManager.listResources("designs", id -> id.getPath().endsWith(".json"));

            for (var entry : found.entrySet()) {
                try (var reader = entry.getValue().openAsReader()) {
                    JsonObject designJson = GsonHelper.parse(reader);
                    Design design = Design.fromJson(entry.getKey(), designJson);
                    if (!design.ingredients().isEmpty()) {
                        map.put(design.id(), buildRecipeJson(design));
                        LOGGER.info("Added recipe for {}", design.id());
                    }
                } catch (Exception e) {
                    throw new JsonSyntaxException(e);
                }
            }
        }

        private static JsonElement buildRecipeJson(Design design) {
            JsonObject output = new JsonObject();
            ResourceLocation designSerializer = CrinkleRecipeSerializers.DESIGN_SERIALIZER.getId();
            if (designSerializer == null) {
                throw new IllegalStateException("design serializer not found!");
            }
            output.addProperty("type", designSerializer.toString());
            output.addProperty("design", design.id().toString());
            output.addProperty("base_item", design.itemId().toString());
            output.addProperty("category", CraftingBookCategory.EQUIPMENT.getSerializedName());
            output.addProperty("group", "%s_designs".formatted(CrinkleMod.MODID));
            return output;
        }
    }

    public static class Serializer implements RecipeSerializer<DesignRecipe> {
        private static final Logger LOGGER = LogUtils.getLogger();

        @Override public @NotNull DesignRecipe fromJson(@NotNull ResourceLocation pRecipeId,
                                                        JsonObject pSerializedRecipe) {
            ResourceLocation designId = ResourceLocation.parse(pSerializedRecipe.get("design").getAsString());
            ResourceLocation itemId = ResourceLocation.parse(pSerializedRecipe.get("base_item").getAsString());
            Item baseItem = ForgeRegistries.ITEMS.getValue(itemId);
            if (baseItem == null || baseItem == Items.AIR) {
                throw new JsonSyntaxException("unknown base item: " + itemId);
            }
            Design design = DesignRegistry.getDesign(designId).orElseThrow(() -> new JsonSyntaxException("unknown " +
                    "design id: " + designId));
            return new DesignRecipe(pRecipeId, baseItem, design);
        }

        @Override public @Nullable DesignRecipe fromNetwork(@NotNull ResourceLocation pRecipeId,
                                                            FriendlyByteBuf pBuffer) {
            ResourceLocation itemId = ResourceLocation.parse(pBuffer.readUtf());
            Item baseItem = ForgeRegistries.ITEMS.getValue(itemId);
            if (baseItem == null || baseItem == Items.AIR) {
                LOGGER.warn("skipping recipe with invalid base item '{}' for: {}", itemId, pRecipeId);
                return null;
            }
            Design design = Design.fromNetwork(pBuffer);
            return new DesignRecipe(pRecipeId, baseItem, design);
        }

        @Override public void toNetwork(@NotNull FriendlyByteBuf pBuffer, DesignRecipe pRecipe) {
            ResourceLocation baseItem = ForgeRegistries.ITEMS.getKey(pRecipe.baseItem());
            if (baseItem == null) {
                throw new IllegalArgumentException("base item not found: " + pRecipe.baseItem().toString());
            }
            pBuffer.writeUtf(baseItem.toString());
            Design.toNetwork(pBuffer, pRecipe.design());
        }
    }
}
