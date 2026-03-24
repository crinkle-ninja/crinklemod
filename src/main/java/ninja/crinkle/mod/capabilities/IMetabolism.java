package ninja.crinkle.mod.capabilities;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.AutoRegisterCapability;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;

/**
 * Metabolism capability interface.
 * This interface is used to store the metabolism of a player in a compound NBT tag.
 *
 * @author Galen
 * @see MetabolismImpl
 * @see net.minecraftforge.common.util.INBTSerializable
 */
@AutoRegisterCapability
public interface IMetabolism extends INBTSerializable<CompoundTag>, IEntityAdditionalSpawnData {
    int getIndicatorPositionX();

    void setIndicatorPositionX(int x);

    int getIndicatorPositionY();

    void setIndicatorPositionY(int y);

    double getNumberOneChance();

    void setNumberOneChance(double numberOneChance);

    int getNumberOneRolls();

    void setNumberOneRolls(int rolls);

    int getNumberOneSafeRolls();

    void setNumberOneSafeRolls(int safeRolls);

    double getNumberTwoChance();

    void setNumberTwoChance(double numberTwoChance);

    int getNumberTwoRolls();

    void setNumberTwoRolls(int rolls);

    int getNumberTwoSafeRolls();

    void setNumberTwoSafeRolls(int safeRolls);

    int getTimer();

    void setTimer(int timer);

    boolean isNumberOneEnabled();

    void setNumberOneEnabled(boolean enabled);

    boolean isNumberTwoEnabled();

    void setNumberTwoEnabled(boolean enabled);
}
