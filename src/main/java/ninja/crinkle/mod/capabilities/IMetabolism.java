package ninja.crinkle.mod.capabilities;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import ninja.crinkle.mod.metabolism.Metabolism;
import ninja.crinkle.mod.settings.Setting;

public interface IMetabolism extends INBTSerializable<CompoundTag>, IEntityAdditionalSpawnData {
    String getAsString(Setting<?> setting);
    double getAsDouble(Setting<?> setting);
    int getAsInt(Setting<?> setting);
    boolean getAsBool(Setting<?> setting);

    void reset();

    Metabolism.Type type();
    void updateValue(Setting<?> setting, Object value);
}
