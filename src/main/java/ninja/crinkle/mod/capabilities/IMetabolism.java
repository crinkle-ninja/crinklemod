package ninja.crinkle.mod.capabilities;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;
import ninja.crinkle.mod.metabolism.Metabolism;
import ninja.crinkle.mod.settings.Setting;

public interface IMetabolism extends INBTSerializable<CompoundTag> {
    void copyFrom(IMetabolism other);
    String getAsString(Setting<?> setting);
    double getAsDouble(Setting<?> setting);
    int getAsInt(Setting<?> setting);
    boolean getAsBool(Setting<?> setting);
    String getCSVRow();
    String getCSVHeaders();
    void reset();

    Metabolism.Type type();
    void updateValue(Setting<?> setting, Object value);
}
