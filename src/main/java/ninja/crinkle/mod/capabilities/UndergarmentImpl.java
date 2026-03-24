package ninja.crinkle.mod.capabilities;

import com.mojang.logging.LogUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import ninja.crinkle.mod.config.UndergarmentConfig;
import ninja.crinkle.mod.items.custom.DiaperArmorItem;
import ninja.crinkle.mod.undergarment.DiaperDesign;
import ninja.crinkle.mod.undergarment.Undergarment;
import ninja.crinkle.mod.util.MathUtil;
import org.slf4j.Logger;

public class UndergarmentImpl implements IUndergarment {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String NBT_KEY_LIQUIDS = "liquids";
    private static final String NBT_KEY_MAX_LIQUIDS = "maxLiquids";
    private static final String NBT_KEY_MAX_SOLIDS = "maxSolids";
    private static final String NBT_KEY_SOLIDS = "solids";
    private int liquids;
    private int maxLiquids;
    private int maxSolids;
    private int solids;

    public UndergarmentImpl(ItemStack stack) {
        // Try design-specific stats first, fall back to global config defaults
        DiaperDesign design = DiaperArmorItem.getDesign(stack, false);
        if (design != null && design.maxLiquids() != null) {
            maxLiquids = design.maxLiquids();
        } else {
            maxLiquids = UndergarmentConfig.getDefaultMaxLiquids();
        }
        if (design != null && design.maxSolids() != null) {
            maxSolids = design.maxSolids();
        } else {
            maxSolids = UndergarmentConfig.getDefaultMaxSolids();
        }
    }

    @Override
    public int getLiquids() {
        return liquids;
    }

    @Override
    public void setLiquids(int value) {
        liquids = MathUtil.clamp(value, 0, maxLiquids);
    }

    @Override
    public int getMaxLiquids() {
        return maxLiquids;
    }

    @Override
    public void setMaxLiquids(int value) {
        maxLiquids = MathUtil.clamp(value, 0, Integer.MAX_VALUE);
    }

    @Override
    public int getMaxSolids() {
        return maxSolids;
    }

    @Override
    public void setMaxSolids(int value) {
        maxSolids = MathUtil.clamp(value, 0, Integer.MAX_VALUE);
    }

    @Override
    public int getSolids() {
        return solids;
    }

    @Override
    public void setSolids(int value) {
        solids = MathUtil.clamp(value, 0, maxSolids);
    }

    public void save(ItemStack itemStack) {
        CompoundTag nbt = itemStack.getOrCreateTag();
        nbt.put(Undergarment.NBT_KEY, serializeNBT());
        itemStack.setTag(nbt);
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        // LOGGER.debug("Serializing undergarment data: {}", this);
        nbt.putInt(NBT_KEY_LIQUIDS, liquids);
        nbt.putInt(NBT_KEY_SOLIDS, solids);
        nbt.putInt(NBT_KEY_MAX_LIQUIDS, maxLiquids);
        nbt.putInt(NBT_KEY_MAX_SOLIDS, maxSolids);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        if (nbt.contains(NBT_KEY_MAX_LIQUIDS))
            maxLiquids = nbt.getInt(NBT_KEY_MAX_LIQUIDS);
        if (nbt.contains(NBT_KEY_MAX_SOLIDS))
            maxSolids = nbt.getInt(NBT_KEY_MAX_SOLIDS);
        setLiquids(nbt.getInt(NBT_KEY_LIQUIDS));
        setSolids(nbt.getInt(NBT_KEY_SOLIDS));
    }

    @Override
    public int hashCode() {
        int result = liquids;
        result = 31 * result + solids;
        result = 31 * result + maxLiquids;
        result = 31 * result + maxSolids;
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UndergarmentImpl that = (UndergarmentImpl) o;

        if (liquids != that.liquids) return false;
        if (solids != that.solids) return false;
        if (maxLiquids != that.maxLiquids) return false;
        return maxSolids == that.maxSolids;
    }

    @Override
    public String toString() {
        return String.format("UndergarmentImpl{liquids=%d, solids=%d, maxLiquids=%d, maxSolids=%d}", liquids, solids,
                maxLiquids, maxSolids);
    }
}
