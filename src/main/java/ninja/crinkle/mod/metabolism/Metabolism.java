package ninja.crinkle.mod.metabolism;

import com.mojang.logging.LogUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.network.PacketDistributor;
import ninja.crinkle.mod.CrinkleMod;
import ninja.crinkle.mod.capabilities.IMetabolism;
import ninja.crinkle.mod.capabilities.MetabolismCapabilities;
import ninja.crinkle.mod.events.CrinkleEvent;
import ninja.crinkle.mod.events.PangEvent;
import ninja.crinkle.mod.network.CrinkleChannel;
import ninja.crinkle.mod.network.messages.MetabolismUpdateMessage;
import ninja.crinkle.mod.settings.Setting;
import ninja.crinkle.mod.util.LogMarkers;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.Random;

public class Metabolism {
    private static final int DEFAULT_PANG_DURATION = 10;
    private static final int DELAY_SECONDS = 60;
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int MIN_INTERVAL = 10; // seconds
    private static final String OUTPUT_CSV_FILE = "crinklemod-metabolism-%s.csv";
    private static final String OUTPUT_CSV_KEY = "CRINKLEMOD_CSV";
    private static final int SYNC_SERVER_TICK_FREQUENCY = 20 * 60; // ticks per second * seconds
    private final Type metabolismType;
    private final Player player;
    private boolean dirty = false;

    private Metabolism(Player player, Type metabolismType) {
        this.player = player;
        this.metabolismType = metabolismType;
    }

    public static @NotNull Metabolism messOf(ICapabilityProvider provider) {
        return of(provider, Type.Mess);
    }

    public static @NotNull Metabolism of(ICapabilityProvider provider, Type metabolismType) {
        if (provider instanceof Player player) {
            return new Metabolism(player, metabolismType);
        }
        throw new IllegalArgumentException("Cannot create metabolism for non-player entity");
    }

    public static @NotNull Metabolism wetOf(ICapabilityProvider provider) {
        return of(provider, Type.Wet);
    }

    public void control(double control) {
        setValue(MetabolismSettings.of(metabolismType).control(), control);
    }

    public <T extends Comparable<? super T>> void setValue(Setting<T> setting, T value) {
        capability().updateValue(setting, value);
        dirty = true;
    }

    protected IMetabolism capability() {
        return switch (metabolismType) {
            case Mess -> player.getCapability(MetabolismCapabilities.MESS).resolve().orElseThrow();
            case Wet -> player.getCapability(MetabolismCapabilities.WET).resolve().orElseThrow();
        };
    }

    public double control() {
        return capability().getAsDouble(MetabolismSettings.of(metabolismType).control());
    }

    public void controlDecay(double controlDecay) {
        setValue(MetabolismSettings.of(metabolismType).controlDecay(), controlDecay);
    }

    public double controlDecay() {
        return capability().getAsDouble(MetabolismSettings.of(metabolismType).controlDecay());
    }

    public void currentControl(double control) {
        setValue(MetabolismSettings.of(metabolismType).currentControl(), control);
    }

    public double currentControl() {
        return capability().getAsDouble(MetabolismSettings.of(metabolismType).currentControl());
    }

    public void currentInterval(double currentInterval) {
        setValue(MetabolismSettings.of(metabolismType).currentInterval(), currentInterval);
    }

    public double currentInterval() {
        return capability().getAsDouble(MetabolismSettings.of(metabolismType).currentInterval());
    }

    public boolean enabled() {
        boolean enabled = capability().getAsBool(MetabolismSettings.of(metabolismType).enabled());
        return !player.isDeadOrDying() && enabled;
    }

    public void enabled(boolean enabled) {
        setValue(MetabolismSettings.of(metabolismType).enabled(), enabled);
    }

    public double getAsDouble(Setting<?> setting) {
        return capability().getAsDouble(setting);
    }

    public int getAsInt(Setting<?> setting) {
        return capability().getAsInt(setting);
    }

    public String getAsString(Setting<?> setting) {
        return capability().getAsString(setting);
    }

    private File getNewOrExistingCSV() {
        Path path = Path.of(OUTPUT_CSV_FILE.formatted(CrinkleMod.getStartTime()
                .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))));
        File csvFile = new File(path.toUri());
        try {
            if (csvFile.createNewFile()) {
                try (FileWriter writer = new FileWriter(csvFile)) {
                    writer.write(capability().getCSVHeaders() + "\n");
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return csvFile;
    }

    private void handleAccident() {
        if (pangDuration() <= 0) {
            pangStop();
            pangStart(Pang.Relief, DEFAULT_PANG_DURATION / 2);
        } else {
            pangTick();
        }
    }

    private void handleControlDecay() {
        if (isDelayed()) return;
        // Update currentControl based on our slope
        double newControl = Math.max(0, (controlDecay() * tickCount()) + control());
        LOGGER.trace(LogMarkers.MET, "({}) [{}] Control tick: {}", CrinkleEvent.side(), metabolismType,
                this.capability().toString());
        if (newControl <= 0) {
            triggerAccident();
        } else {
            currentControl(newControl);
        }
    }

    private void handleDelay() {
        if (pangDuration() <= 0) {
            pangStop();
            reset();
        } else {
            pangTick();
        }
    }

    private void handleIntervalDecay() {
        if (isDelayed() || tickCount() % Math.max(DEFAULT_PANG_DURATION, Math.ceil(currentInterval())) != 0) return;
        double newInterval = currentInterval() * (1 - intervalDecay());
        if (newInterval <= MIN_INTERVAL) {
            triggerAccident();
        } else {
            currentInterval(newInterval);
        }
    }

    private void handlePangNone() {
        if (tickCount() % Math.max(DEFAULT_PANG_DURATION, Math.ceil(currentInterval())) != 0) return;
        Pang pang = intensity() >= currentControl() ? Pang.Major : Pang.Minor;
        pangStart(pang, DEFAULT_PANG_DURATION);
    }

    private void handlePangState() {
        switch (pang()) {
            case Delay -> handleDelay();
            case Relief -> handleRelief();
            case Accident -> handleAccident();
            case Major, Minor -> handlePangs();
            case None -> handlePangNone();
        }
    }

    private void handlePangs() {
        if (pangDuration() <= 0) {
            // Check for accident
            boolean accident = false;
            double roll;
            if (pang() == Pang.Major) {
                Random random = new Random();
                roll = random.nextDouble();
                accident = roll > currentControl();
            }
            pangStop();
            if (accident) triggerAccident();
        } else {
            pangTick();
        }
    }

    private void handleRelief() {
        if (pangDuration() <= 0) {
            pangStop();
            pangStart(Pang.Delay, DELAY_SECONDS);
        } else {
            pangTick();
        }
    }

    public void intensity(double intensity) {
        setValue(MetabolismSettings.of(metabolismType).intensity(), intensity);
    }

    public double intensity() {
        return capability().getAsDouble(MetabolismSettings.of(metabolismType).intensity());
    }

    public void interval(int interval) {
        setValue(MetabolismSettings.of(metabolismType).interval(), interval);
    }

    public int interval() {
        return capability().getAsInt(MetabolismSettings.of(metabolismType).interval());
    }

    public double intervalDecay() {
        return capability().getAsDouble(MetabolismSettings.of(metabolismType).intervalDecay());
    }

    public void intervalDecay(double intervalDecay) {
        setValue(MetabolismSettings.of(metabolismType).intervalDecay(), intervalDecay);
    }

    private boolean isDelayed() {
        // We are delayed if our tickCount is less than DELAY_SECONDS and current control equals default control,
        // since both values reset after an accident.
        return pang() == Pang.Delay && pangDuration() > 0;
    }

    public boolean isNormal() {
        return pang() == Pang.None;
    }

    public Pang pang() {
        return Pang.from(capability().getAsString(MetabolismSettings.of(metabolismType).pang()));
    }

    public boolean isPang() {
        return pang().compareTo(Pang.Relief) > 0;
    }

    private boolean outputCSV() {
        if (System.getenv().containsKey(OUTPUT_CSV_KEY)) {
            String v = System.getenv(OUTPUT_CSV_KEY);
            return Boolean.parseBoolean(v) || Integer.parseInt(v) == 1;
        }
        return false;
    }

    public void pang(Pang pang) {
        setValue(MetabolismSettings.of(metabolismType).pang(), pang.name());
    }

    public void pangDuration(int duration) {
        setValue(MetabolismSettings.of(metabolismType).pangDuration(), duration);
    }

    public int pangDuration() {
        return capability().getAsInt(MetabolismSettings.of(metabolismType).pangDuration());
    }

    public void pangStart(Pang pang, int duration) {
        pang(pang);
        pangDuration(duration);
        CrinkleMod.EVENT_BUS.post(new PangEvent.Start(player, metabolismType, pang(), pangDuration()));
        LOGGER.trace(LogMarkers.MET, "({}) [{}] {} start: {}",
                CrinkleEvent.side(), metabolismType, pang(), this.capability().toString());
    }

    public void pangStop() {
        CrinkleMod.EVENT_BUS.post(new PangEvent.Stop(player, metabolismType, pang(), pangDuration()));
        LOGGER.trace(LogMarkers.MET, "({}) [{}] {} stop: {}",
                CrinkleEvent.side(), metabolismType, pang(), this.capability().toString());
        pang(Pang.None);
        pangDuration(0);
    }

    public void pangTick() {
        pangDuration(pangDuration() - 1);
        CrinkleMod.EVENT_BUS.post(new PangEvent.Tick(player, metabolismType, pang(), pangDuration()));
        LOGGER.trace(LogMarkers.MET, "({}) [{}] {} tick: {}",
                CrinkleEvent.side(), metabolismType, pang(), this.capability().toString());
    }

    public void reset() {
        LOGGER.trace(LogMarkers.MET, "({}) [{}] reset metabolism vars", CrinkleEvent.side(), metabolismType);
        currentControl(control());
        pang(Pang.None);
        currentInterval(interval());
        pangDuration(0);
        tickCount(0);
    }

    public void resetDefaults() {
        capability().reset();
        syncServer();
    }

    public void syncServer() {
        if (player instanceof ServerPlayer) return;
        LOGGER.trace(LogMarkers.MET, "({}) [{}] Sending metabolism sync to server", CrinkleEvent.side(),
                metabolismType);
        CrinkleChannel.INSTANCE.sendToServer(new MetabolismUpdateMessage(this));
    }

    public void syncClient() {
        if (player instanceof ServerPlayer serverPlayer) {
            LOGGER.trace(LogMarkers.MET, "({}) [{}] Sending metabolism sync to client", CrinkleEvent.side(),
                    metabolismType);
            CrinkleChannel.INSTANCE.send(PacketDistributor.PLAYER.with(() -> serverPlayer),
                    new MetabolismUpdateMessage(this));
        }
    }

    public void tick(int mcTickCount) {
        if (!enabled()) return;

        // Only process every second
        // Minecraft ticks are 1/20th of a second
        if (mcTickCount % 20 != 0) return;

        // Record our tick count
        tickCount(tickCount() + 1);

        // Process
        handlePangState();
        handleControlDecay();
        handleIntervalDecay();

        // CSV Output for debugging
        if (outputCSV()) {
            writeCSV();
        }

        // Sync
        if (dirty && mcTickCount % SYNC_SERVER_TICK_FREQUENCY == 0) {
            syncServer();
            dirty = false;
        }
    }

    public void tickCount(int tickCount) {
        setValue(MetabolismSettings.of(metabolismType).tickCount(), tickCount);
    }

    public int tickCount() {
        return capability().getAsInt(MetabolismSettings.of(metabolismType).tickCount());
    }

    public void triggerAccident() {
        currentInterval(interval());
        currentControl(control());
        pangStart(Pang.Accident, DEFAULT_PANG_DURATION / 2);
    }

    public Type type() {
        return metabolismType;
    }

    private void writeCSV() {
        File csvFile = getNewOrExistingCSV();
        try (FileWriter writer = new FileWriter(getNewOrExistingCSV(), true)) {
            writer.write(capability().getCSVRow() + "\n");
            LOGGER.trace(LogMarkers.MET, "wrote to csv: {}", csvFile.toPath().toUri());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public enum Type {
        Wet, Mess;

        @Override
        public String toString() {
            return name().toLowerCase();
        }
    }
}
