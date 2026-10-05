package ninja.crinkle.mod.capabilities;

import net.minecraftforge.common.capabilities.AutoRegisterCapability;
import net.minecraftforge.common.util.INBTSerializable;

/**
 * Metabolism capability interface.
 * This interface is used to store the metabolism of a player in a compound NBT tag.
 *
 * @author Galen
 * @see MetabolismImpl
 * @see INBTSerializable
 */
@AutoRegisterCapability
public interface WetCapability extends IMetabolism {}
