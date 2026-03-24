package ninja.crinkle.mod.capabilities;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.INBTSerializable;

public interface IUndergarment extends INBTSerializable<CompoundTag> {
    int getLiquids();

    void setLiquids(int value);

    int getMaxLiquids();

    void setMaxLiquids(int value);

    int getMaxSolids();

    void setMaxSolids(int value);

    int getSolids();

    void setSolids(int value);

    void save(ItemStack stack);
}
