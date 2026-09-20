package ninja.crinkle.mod.capabilities;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import ninja.crinkle.mod.config.UndergarmentConfig;
import ninja.crinkle.mod.items.custom.DiaperArmorItem;
import ninja.crinkle.mod.undergarment.DiaperDesign;
import ninja.crinkle.mod.undergarment.DiaperDesignRegistry;
import ninja.crinkle.mod.undergarment.Undergarment;
import ninja.crinkle.mod.util.MathUtil;

import java.util.Optional;

public class UndergarmentImpl implements IUndergarment {
    private static final String NBT_KEY_LIQUIDS = "liquids";
    private static final String NBT_KEY_MAX_LIQUIDS = "maxLiquids";
    private static final String NBT_KEY_MAX_SOLIDS = "maxSolids";
    private static final String NBT_KEY_SOLIDS = "solids";
    private static final String NBT_KEY_DESIGN = "design";
    private int liquids;
    private int maxLiquids;
    private int maxSolids;
    private int solids;
    private DiaperDesign design;

    public UndergarmentImpl(ItemStack stack) {
        Optional.ofNullable(DiaperArmorItem.getDesign(stack, false)).ifPresent(this::setDesign);
        getDesign().ifPresentOrElse(d -> {
            setMaxLiquids(Optional.ofNullable(d.maxLiquids()).orElse(UndergarmentConfig.getDefaultMaxLiquids()));
            setMaxSolids(Optional.ofNullable(d.maxSolids()).orElse(UndergarmentConfig.getDefaultMaxSolids()));
        }, () -> {
            setMaxSolids(UndergarmentConfig.getDefaultMaxSolids());
            setMaxLiquids(UndergarmentConfig.getDefaultMaxLiquids());
        });
    }

    public Optional<DiaperDesign> getDesign() {
        return Optional.ofNullable(design);
    }

    public void setDesign(DiaperDesign design) {
        this.design = design;
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
        nbt.putInt(NBT_KEY_LIQUIDS, liquids);
        nbt.putInt(NBT_KEY_SOLIDS, solids);
        nbt.putInt(NBT_KEY_MAX_LIQUIDS, maxLiquids);
        nbt.putInt(NBT_KEY_MAX_SOLIDS, maxSolids);
        getDesign().ifPresent(d -> nbt.putString(NBT_KEY_DESIGN, d.id().toString()));
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        if (nbt.contains(NBT_KEY_MAX_LIQUIDS))
            maxLiquids = nbt.getInt(NBT_KEY_MAX_LIQUIDS);
        if (nbt.contains(NBT_KEY_MAX_SOLIDS))
            maxSolids = nbt.getInt(NBT_KEY_MAX_SOLIDS);
        if (nbt.contains(NBT_KEY_DESIGN)) {
            ResourceLocation designId = new ResourceLocation(nbt.getString(NBT_KEY_DESIGN));
            DiaperDesignRegistry.getDesign(designId, true).ifPresent(this::setDesign);
        }
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
        return String.format("UndergarmentImpl{liquids=%d, solids=%d, maxLiquids=%d, maxSolids=%d, design=%s}",
                liquids, solids, maxLiquids, maxSolids, getDesign().toString());
    }
}
