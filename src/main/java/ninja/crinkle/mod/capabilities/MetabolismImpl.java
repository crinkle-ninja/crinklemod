package ninja.crinkle.mod.capabilities;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import ninja.crinkle.mod.capabilities.versioning.MetabolismVersion;
import ninja.crinkle.mod.metabolism.Metabolism;
import ninja.crinkle.mod.metabolism.MetabolismSettings;
import ninja.crinkle.mod.metabolism.Pang;
import ninja.crinkle.mod.settings.Setting;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

/**
 * Metabolism capability implementation.
 * This class is used to store the metabolism of a player in a compound NBT tag.
 *
 * @author Galen
 * @see IMetabolism
 */
public class MetabolismImpl implements IMetabolism, WetCapability, MessCapability {
    private static final List<String> omitCSVFields = List.of("enabled", "version");
    private final Metabolism.Type type;
    private Pang pang;
    private double currentInterval;
    private double currentControl;
    private boolean enabled;
    private int pangDuration;
    private int tickCount;
    private double control;
    private int interval;
    private double controlDecay;
    private double intervalDecay;
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
        control = MetabolismSettings.of(type).control().getDefault();
        interval = MetabolismSettings.of(type).interval().getDefault();
        controlDecay = MetabolismSettings.of(type).controlDecay().getDefault();
        intervalDecay = MetabolismSettings.of(type).intervalDecay().getDefault();
        intensity = MetabolismSettings.of(type).intensity().getDefault();

        // State machine values
        pang = Pang.None;
        currentInterval = interval;
        currentControl = control;
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

    private String getHeaderName(Setting<?> setting) {
        return setting.key().substring(setting.key().lastIndexOf(".") + 1);
    }

    @Override
    public String getCSVRow() {
        CompoundTag data = serializeNBT();
        List<String> sortedKeys = data.getAllKeys().stream().sorted().toList();
        String row = sortedKeys.stream()
                .map((k) -> MetabolismSettings.of(type()).byKey(k))
                .filter((s) -> s != null && !omitCSVFields.contains(getHeaderName(s)))
                .map(this::getAsString)
                .collect(Collectors.joining(","));
        return (int) (System.currentTimeMillis() / 1000) + "," + type().name().toLowerCase() + "," + row;
    }

    @Override
    public String getCSVHeaders() {
        return "timestamp,type," + serializeNBT().getAllKeys().stream()
                .map((k) -> MetabolismSettings.of(type()).byKey(k))
                .filter(Objects::nonNull)
                .map(this::getHeaderName)
                .filter((k) -> !omitCSVFields.contains(k))
                .sorted()
                .collect(Collectors.joining(","));
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
        tag.putInt(settings.interval().key(), interval);
        tag.putDouble(settings.control().key(), control);
        tag.putDouble(settings.controlDecay().key(), controlDecay);
        tag.putDouble(settings.intervalDecay().key(), intervalDecay);
        tag.putDouble(settings.intensity().key(), intensity);
        tag.putString(settings.pang().key(), pang.name());
        tag.putDouble(settings.currentControl().key(), currentControl);
        tag.putDouble(settings.currentInterval().key(), currentInterval);
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
        safeSet(nbt, settings.control().key(), (tag, key) -> control = tag.getDouble(key));
        safeSet(nbt, settings.interval().key(), (tag, key) -> interval = tag.getInt(key));
        safeSet(nbt, settings.controlDecay().key(), (tag, key) -> controlDecay = tag.getDouble(key));
        safeSet(nbt, settings.intervalDecay().key(), (tag, key) -> intervalDecay = tag.getDouble(key));
        safeSet(nbt, settings.intensity().key(), (tag, key) -> intensity = tag.getDouble(key));
        safeSet(nbt, settings.pang().key(), (tag, key) -> pang = Pang.from(tag.getString(key)));
        safeSet(nbt, settings.currentControl().key(), (tag, key) -> currentControl = tag.getDouble(key));
        safeSet(nbt, settings.currentInterval().key(), (tag, key) -> currentInterval = tag.getDouble(key));
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
                ", control=" + control +
                ", interval=" + interval +
                ", controlDecay=" + controlDecay +
                ", intervalDecay=" + intervalDecay +
                ", intensity=" + intensity +
                ", pang=" + pang +
                ", currentInterval=" + currentInterval +
                ", currentControl=" + currentControl +
                ", pangDuration=" + pangDuration +
                ", tickCount=" + tickCount +
                '}';
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buffer) {
        buffer.writeBoolean(enabled);
        buffer.writeDouble(control);
        buffer.writeInt(interval);
        buffer.writeDouble(controlDecay);
        buffer.writeDouble(intervalDecay);
        buffer.writeDouble(intensity);
        buffer.writeUtf(pang.name());
        buffer.writeDouble(currentInterval);
        buffer.writeDouble(currentControl);
        buffer.writeInt(pangDuration);
        buffer.writeInt(tickCount);
    }

    @Override
    public void readSpawnData(FriendlyByteBuf additionalData) {
        enabled = additionalData.readBoolean();
        control = additionalData.readDouble();
        interval = additionalData.readInt();
        controlDecay = additionalData.readDouble();
        intervalDecay = additionalData.readDouble();
        intensity = additionalData.readDouble();
        pang = Pang.from(additionalData.readUtf());
        currentInterval = additionalData.readDouble();
        currentControl = additionalData.readDouble();
        pangDuration = additionalData.readInt();
        tickCount = additionalData.readInt();
    }
}
