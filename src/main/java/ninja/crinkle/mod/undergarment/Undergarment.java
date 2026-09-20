package ninja.crinkle.mod.undergarment;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import ninja.crinkle.mod.CrinkleMod;
import ninja.crinkle.mod.capabilities.IUndergarment;
import ninja.crinkle.mod.capabilities.UndergarmentImpl;
import ninja.crinkle.mod.config.UndergarmentConfig;
import ninja.crinkle.mod.items.custom.DiaperArmorItem;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class Undergarment {
    public static final Undergarment EMPTY = new Undergarment(ItemStack.EMPTY);
    public static final String NBT_KEY = CrinkleMod.MODID + ".undergarment";
    private final IUndergarment capability;
    private final ItemStack itemStack;

    private Undergarment(@NotNull ItemStack itemStack) {
        this.itemStack = itemStack;
        if (itemStack.equals(ItemStack.EMPTY)) {
            this.capability = null;
        } else {
            IUndergarment u = new UndergarmentImpl(itemStack);
            u.deserializeNBT(itemStack.getOrCreateTagElement(NBT_KEY));
            this.capability = u;
        }
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

    public static boolean hasUndergarmentData(ItemStack itemStack) {
        return itemStack != null && itemStack.getItem() instanceof DiaperArmorItem;
    }

    public static @NotNull Undergarment of(ICapabilityProvider provider) {
        if (provider instanceof ItemStack itemStack) {
            return of(itemStack);
        }
        return Undergarment.EMPTY;
    }

    @Contract(value = "_ -> new", pure = true)
    public static @NotNull Undergarment of(ItemStack item) {
        return item.equals(ItemStack.EMPTY) ? Undergarment.EMPTY : new Undergarment(item);
    }

    private <T> Optional<T> getCapability(Function<IUndergarment, T> getter) {
        if (capability != null)
            return Optional.of(getter.apply(capability));
        return Optional.empty();
    }

    private void saveCapability(Consumer<IUndergarment> setter) {
        if (capability != null) {
            setter.accept(capability);
            capability.save(itemStack);
        }
    }

    public Optional<DiaperDesign> getDesign() {
        return getCapability(IUndergarment::getDesign).orElse(Optional.empty());
    }

    public void setDesign(DiaperDesign design) {
        saveCapability(c -> c.setDesign(design));
    }


    public ItemStack getItemStack() {
        return itemStack;
    }

    public double getLiquidsPercent() {
        return (double) getLiquids() / (double) getMaxLiquids();
    }

    public int getLiquids() {
        return getCapability(IUndergarment::getLiquids).orElse(0);
    }

    public int getMaxLiquids() {
        return getCapability(IUndergarment::getMaxLiquids).orElse(UndergarmentConfig.getDefaultMaxLiquids());
    }

    public void setMaxLiquids(int value) {
        saveCapability(c -> c.setMaxLiquids(value));
    }

    public void setLiquids(int value) {
        saveCapability(c -> c.setLiquids(value));
    }

    public double getSolidsPercent() {
        return (double) getSolids() / (double) getMaxSolids();
    }

    public int getSolids() {
        return getCapability(IUndergarment::getSolids).orElse(0);
    }

    public int getMaxSolids() {
        return getCapability(IUndergarment::getMaxSolids).orElse(UndergarmentConfig.getDefaultMaxSolids());
    }

    public void setMaxSolids(int value) {
        saveCapability(c -> c.setMaxSolids(value));
    }

    public void setSolids(int value) {
        saveCapability(c -> c.setSolids(value));
    }

    public boolean isLeaking() {
        return getLiquids() > getMaxLiquids() || getSolids() > getMaxSolids();
    }

    public void modifyLiquids(int amount) {
        int newAmount = getLiquids() + amount;
        saveCapability(c -> setLiquids(newAmount));
    }

    public void modifySolids(int amount) {
        int newAmount = getSolids() + amount;
        saveCapability(c -> c.setSolids(newAmount));
    }
}
