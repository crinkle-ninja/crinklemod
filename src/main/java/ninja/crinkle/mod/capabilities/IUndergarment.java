package ninja.crinkle.mod.capabilities;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.INBTSerializable;
import ninja.crinkle.mod.undergarment.DiaperDesign;

import java.util.Optional;

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

    Optional<DiaperDesign> getDesign();

    void setDesign(DiaperDesign design);

}
