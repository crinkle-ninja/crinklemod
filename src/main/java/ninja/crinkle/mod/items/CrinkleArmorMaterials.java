package ninja.crinkle.mod.items;

import net.minecraft.Util;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.LazyLoadedValue;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.EnumMap;
import java.util.function.Supplier;

public enum CrinkleArmorMaterials implements StringRepresentable, ArmorMaterial {
    DIAPER("diaper", 60, Util.make(new EnumMap<>(ArmorItem.Type.class),
            (map) -> map.put(ArmorItem.Type.LEGGINGS, 8)), 15, SoundEvents.ARMOR_EQUIP_LEATHER,
            8.0F, 0.5F, () -> Ingredient.of(Items.WHITE_WOOL));

    @SuppressWarnings("deprecation")
    public static final StringRepresentable.EnumCodec<ArmorMaterials> CODEC =
            StringRepresentable.fromEnum(ArmorMaterials::values);
    private static final EnumMap<ArmorItem.Type, Integer> HEALTH_FUNCTION_FOR_TYPE =
            Util.make(new EnumMap<>(ArmorItem.Type.class), (map) -> {
                map.put(ArmorItem.Type.BOOTS, 13);
                map.put(ArmorItem.Type.LEGGINGS, 15);
                map.put(ArmorItem.Type.CHESTPLATE, 16);
                map.put(ArmorItem.Type.HELMET, 11);
            });
    private final int durabilityMultiplier;
    private final int enchantmentValue;
    private final float knockbackResistance;
    private final String name;
    private final EnumMap<ArmorItem.Type, Integer> protectionFunctionForType;
    @SuppressWarnings("deprecation")
    private final LazyLoadedValue<Ingredient> repairIngredient;
    private final SoundEvent sound;
    private final float toughness;

    CrinkleArmorMaterials(String pName, int pDurabilityMultiplier,
                          EnumMap<ArmorItem.Type, Integer> pProtectionFunctionForType, int pEnchantmentValue,
                          SoundEvent pSound, float pToughness, float pKnockbackResistance,
                          Supplier<Ingredient> pRepairIngredient) {
        this.name = pName;
        this.durabilityMultiplier = pDurabilityMultiplier;
        this.protectionFunctionForType = pProtectionFunctionForType;
        this.enchantmentValue = pEnchantmentValue;
        this.sound = pSound;
        this.toughness = pToughness;
        this.knockbackResistance = pKnockbackResistance;
        //noinspection deprecation
        this.repairIngredient = new LazyLoadedValue<>(pRepairIngredient);
    }

    public int getDurabilityForType(ArmorItem.Type pType) {
        return HEALTH_FUNCTION_FOR_TYPE.get(pType) * this.durabilityMultiplier;
    }

    public int getDefenseForType(ArmorItem.Type pType) {
        return this.protectionFunctionForType.get(pType);
    }

    public int getEnchantmentValue() {
        return this.enchantmentValue;
    }

    public SoundEvent getEquipSound() {
        return this.sound;
    }

    public Ingredient getRepairIngredient() {
        return this.repairIngredient.get();
    }

    public String getName() {
        return this.name;
    }

    public float getToughness() {
        return this.toughness;
    }

    /**
     * Gets the percentage of knockback resistance provided by armor of the material.
     */
    public float getKnockbackResistance() {
        return this.knockbackResistance;
    }

    public String getSerializedName() {
        return this.name;
    }
}
