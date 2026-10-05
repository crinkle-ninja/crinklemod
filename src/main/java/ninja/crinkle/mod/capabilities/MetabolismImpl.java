package ninja.crinkle.mod.capabilities;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import ninja.crinkle.mod.capabilities.versioning.MetabolismVersion;
import ninja.crinkle.mod.metabolism.Metabolism;
import ninja.crinkle.mod.metabolism.MetabolismSettings;
import ninja.crinkle.mod.metabolism.Pang;
import ninja.crinkle.mod.settings.Setting;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.BiConsumer;

/**
 * Metabolism capability implementation.
 * This class is used to store the metabolism of a player in a compound NBT tag.
 *
 * @author Galen
 * @see IMetabolism
 */
public class MetabolismImpl implements IMetabolism, WetCapability, MessCapability {
    private final Metabolism.Type type;
    private Pang pang;
    private double currentFrequency;
    private double currentTraining;
    private boolean enabled;
    private int pangDuration;
    private int tickCount;
    private double training;
    private int ticks;
    private double slopeDegradation;
    private double frequencyCompression;
    private double intensity;
    private MetabolismVersion version;

    public MetabolismImpl(Metabolism.Type metabolismType) {
        version = MetabolismVersion.getLatest();
        type = metabolismType;
        reset();
    }

    public void reset() {
        // Settings
        enabled = MetabolismSettings.of(type).enabled().getDefault();
        training = MetabolismSettings.of(type).training().getDefault();
        ticks = MetabolismSettings.of(type).ticks().getDefault();
        slopeDegradation = MetabolismSettings.of(type).slopeDegradation().getDefault();
        frequencyCompression = MetabolismSettings.of(type).frequencyCompression().getDefault();
        intensity = MetabolismSettings.of(type).intensity().getDefault();

        // State machine values
        pang = Pang.None;
        currentFrequency = ticks;
        currentTraining = training;
        pangDuration = 0;
        tickCount = 0;
    }

    @Override
    public Metabolism.Type type() {
        return type;
    }

    @Override
    public String getAsString(Setting<?> setting) {
        // Handle bool, int, and double as strings
        if (setting.isInt()) {
            return setting.formattedString(getAsInt(setting));
        }
        if (setting.isDouble()) {
            return setting.formattedString(getAsDouble(setting));
        }
        if (setting.isBoolean()) {
            return setting.formattedString(getAsBool(setting));
        }
        return Optional.of(serializeNBT().getString(setting.key()))
                .orElse(setting.formattedString(setting.getDefault()));
    }

    @Override
    public double getAsDouble(Setting<?> setting) {
        if (!setting.isDouble()) {
            throw new IllegalArgumentException("getAsDouble expects a double setting");
        }
        Setting.DoubleValue dv = (Setting.DoubleValue) setting;
        return Optional.of(serializeNBT().getDouble(dv.key()))
                .orElse(dv.getDefault());
    }

    @Override
    public int getAsInt(Setting<?> setting) {
        if (!setting.isInt()) {
            throw new IllegalArgumentException("getAsInt expects an integer setting");
        }
        Setting.IntValue iv = (Setting.IntValue) setting;
        return Optional.of(serializeNBT().getInt(iv.key()))
                .orElse(iv.getDefault());
    }

    @Override
    public boolean getAsBool(Setting<?> setting) {
        if (!setting.isBoolean()) {
            throw new IllegalArgumentException("getAsBool expects a boolean setting");
        }
        Setting.BooleanValue bv = (Setting.BooleanValue) setting;
        return Optional.of(serializeNBT().getBoolean(bv.key()))
                .orElse(bv.getDefault());
    }

    @Override
    public void updateValue(Setting<?> setting, Object value) {
        CompoundTag current = serializeNBT();
        if (setting.isBoolean()) {
            current.putBoolean(setting.key(), (Boolean) value);
        } else if (setting.isInt()) {
            current.putInt(setting.key(), (Integer) value);
        } else if (setting.isDouble()) {
            current.putDouble(setting.key(), (Double) value);
        } else {
            current.putString(setting.key(), value.toString());
        }
        deserializeNBT(current);
    }

    /**
     * Serialize the metabolism to NBT.
     *
     * @return The serialized metabolism
     */
    @Override
    public CompoundTag serializeNBT() {
        MetabolismSettings settings = MetabolismSettings.of(type());
        CompoundTag tag = new CompoundTag();
        tag.putString(MetabolismVersion.TAG_VERSION, version.name());
        tag.putBoolean(settings.enabled().key(), enabled);
        tag.putInt(settings.ticks().key(), ticks);
        tag.putDouble(settings.training().key(), training);
        tag.putDouble(settings.slopeDegradation().key(), slopeDegradation);
        tag.putDouble(settings.frequencyCompression().key(), frequencyCompression);
        tag.putDouble(settings.intensity().key(), intensity);
        tag.putString(settings.pang().key(), pang.name());
        tag.putDouble(settings.currentTraining().key(), currentTraining);
        tag.putDouble(settings.currentFrequency().key(), currentFrequency);
        tag.putInt(settings.pangDuration().key(), pangDuration);
        tag.putInt(settings.tickCount().key(), tickCount);
        return tag;
    }

    /**
     * Deserialize the metabolism from NBT.
     *
     * @param nbt The NBT to deserialize
     */
    @Override
    public void deserializeNBT(@NotNull CompoundTag nbt) {
        MetabolismSettings settings = MetabolismSettings.of(type());
        version = MetabolismVersion.fromNBT(nbt);
        safeSet(nbt, settings.enabled().key(), (tag, key) -> enabled = tag.getBoolean(key));
        safeSet(nbt, settings.training().key(), (tag, key) -> training = tag.getDouble(key));
        safeSet(nbt, settings.ticks().key(), (tag, key) -> ticks = tag.getInt(key));
        safeSet(nbt, settings.slopeDegradation().key(), (tag, key) -> slopeDegradation = tag.getDouble(key));
        safeSet(nbt, settings.frequencyCompression().key(), (tag, key) -> frequencyCompression = tag.getDouble(key));
        safeSet(nbt, settings.intensity().key(), (tag, key) -> intensity = tag.getDouble(key));
        safeSet(nbt, settings.pang().key(), (tag, key) -> pang = Pang.from(tag.getString(key)));
        safeSet(nbt, settings.currentTraining().key(), (tag, key) -> currentTraining = tag.getDouble(key));
        safeSet(nbt, settings.currentFrequency().key(), (tag, key) -> currentFrequency = tag.getDouble(key));
        safeSet(nbt, settings.pangDuration().key(), (tag, key) -> pangDuration = tag.getInt(key));
        safeSet(nbt, settings.tickCount().key(), (tag, key) -> tickCount = tag.getInt(key));
    }

    private void safeSet(CompoundTag nbt, String key, BiConsumer<CompoundTag, String> setter) {
        if (nbt.contains(key)) {
            setter.accept(nbt, key);
        }
    }

    /**
     * To string method for debugging
     */
    @Override
    public String toString() {
        return "MetabolismImpl{" +
                "type=" + type().name() +
                ", enabled=" + enabled +
                ", training=" + training +
                ", ticks=" + ticks +
                ", slopeDegradation=" + slopeDegradation +
                ", frequencyCompression=" + frequencyCompression +
                ", intensity=" + intensity +
                ", currentDesperation=" + pang +
                ", currentFrequency=" + currentFrequency +
                ", currentTraining=" + currentTraining +
                ", pangDuration=" + pangDuration +
                ", tickCount=" + tickCount +
                '}';
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buffer) {
        buffer.writeBoolean(enabled);
        buffer.writeDouble(training);
        buffer.writeInt(ticks);
        buffer.writeDouble(slopeDegradation);
        buffer.writeDouble(frequencyCompression);
        buffer.writeDouble(intensity);
        buffer.writeUtf(pang.name());
        buffer.writeDouble(currentFrequency);
        buffer.writeDouble(currentTraining);
        buffer.writeInt(pangDuration);
        buffer.writeInt(tickCount);
    }

    @Override
    public void readSpawnData(FriendlyByteBuf additionalData) {
        enabled = additionalData.readBoolean();
        training = additionalData.readDouble();
        ticks = additionalData.readInt();
        slopeDegradation = additionalData.readDouble();
        frequencyCompression = additionalData.readDouble();
        intensity = additionalData.readDouble();
        pang = Pang.from(additionalData.readUtf());
        currentFrequency = additionalData.readDouble();
        currentTraining = additionalData.readDouble();
        pangDuration = additionalData.readInt();
        tickCount = additionalData.readInt();
    }
}
