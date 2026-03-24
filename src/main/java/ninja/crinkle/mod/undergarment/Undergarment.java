package ninja.crinkle.mod.undergarment;

import com.mojang.logging.LogUtils;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import ninja.crinkle.mod.CrinkleMod;
import ninja.crinkle.mod.capabilities.IUndergarment;
import ninja.crinkle.mod.capabilities.UndergarmentImpl;
import ninja.crinkle.mod.client.color.Color;
import ninja.crinkle.mod.items.custom.DiaperArmorItem;
import ninja.crinkle.mod.tooltips.GradientBarTooltip;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

public class Undergarment {
    public static final int LIQUIDS_COLOR = 0xffffef00;
    public static final String NBT_KEY = CrinkleMod.MODID + ".undergarment";
    public static final int SOLIDS_COLOR = 0xFF836953;
    private static final Logger LOGGER = LogUtils.getLogger();
    private final IUndergarment capability;
    private final ItemStack itemStack;

    private Undergarment(@NotNull ItemStack itemStack) {
        this.itemStack = itemStack;
        IUndergarment u = new UndergarmentImpl(itemStack);
        u.deserializeNBT(itemStack.getOrCreateTagElement(NBT_KEY));
        this.capability = u;
    }

    public static ItemStack getWornUndergarment(@NotNull Player player) {
        for (final ItemStack i : player.getArmorSlots()) {
            if (!LivingEntity.getEquipmentSlotForItem(i).equals(EquipmentSlot.LEGS)) {
                continue;
            }
            if (!hasUndergarmentData(i)) {
                continue;
            }
            return i;
        }
        return ItemStack.EMPTY;
    }

    public static boolean hasUndergarmentData(@NotNull ItemStack itemStack) {
        return itemStack.getItem() instanceof DiaperArmorItem;
    }

    @Contract(value = "_ -> new", pure = true)
    public static @NotNull Undergarment of(ItemStack item) {
        return new Undergarment(item);
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public double getLiquidsPercent() {
        return (double) getLiquids() / (double) getMaxLiquids();
    }

    public int getLiquids() {
        return capability.getLiquids();
    }

    public int getMaxLiquids() {
        return capability.getMaxLiquids();
    }

    public void setMaxLiquids(int value) {
        capability.setMaxLiquids(value);
        capability.save(itemStack);
    }

    public void setLiquids(int value) {
        capability.setLiquids(value);
        capability.save(itemStack);
    }

    public GradientBarTooltip getLiquidsTooltip() {
        return new GradientBarTooltip(UndergarmentSettings.LIQUIDS.label(), getLiquids(), getMaxLiquids(),
                LIQUIDS_COLOR, Color.brightness(LIQUIDS_COLOR, 0.5f),
                Color.brightness(LIQUIDS_COLOR, 0.25f), 9, 60, 40);
    }

    public double getSolidsPercent() {
        return (double) getSolids() / (double) getMaxSolids();
    }

    public int getSolids() {
        return capability.getSolids();
    }

    public int getMaxSolids() {
        return capability.getMaxSolids();
    }

    public void setMaxSolids(int value) {
        capability.setMaxSolids(value);
        capability.save(itemStack);
    }

    public void setSolids(int value) {
        capability.setSolids(value);
        capability.save(itemStack);
    }

    public GradientBarTooltip getSolidsTooltip() {
        return new GradientBarTooltip(UndergarmentSettings.SOLIDS.label(), getSolids(), getMaxSolids(),
                SOLIDS_COLOR, Color.brightness(SOLIDS_COLOR, 0.5f),
                Color.brightness(SOLIDS_COLOR, 0.25f), 9, 60, 40);
    }

    public boolean isLeaking() {
        return getLiquids() > getMaxLiquids() || getSolids() > getMaxSolids();
    }

    public void modifyLiquids(int amount) {
        capability.setLiquids(capability.getLiquids() + amount);
        capability.save(itemStack);
    }

    public void modifySolids(int amount) {
        capability.setSolids(capability.getSolids() + amount);
        capability.save(itemStack);
    }
}
