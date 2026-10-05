package ninja.crinkle.mod.metabolism;

import com.mojang.logging.LogUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.network.PacketDistributor;
import ninja.crinkle.mod.capabilities.IMetabolism;
import ninja.crinkle.mod.capabilities.MetabolismCapabilities;
import ninja.crinkle.mod.events.CrinkleEvent;
import ninja.crinkle.mod.network.CrinkleChannel;
import ninja.crinkle.mod.network.messages.MetabolismUpdateMessage;
import ninja.crinkle.mod.settings.Setting;
import ninja.crinkle.mod.util.LogMarkers;
import ninja.crinkle.mod.util.MathUtil;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.Random;

public class Metabolism {
    private static final int ACCIDENT_DURATION = 5;
    private static final int SYNC_SERVER_TICK_FREQUENCY = 20 * 60; // ticks per second * seconds
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int TICK_FREQ = 20;
    private static final int MIN_PANG = 5;
    private static final int MAX_PANG = 15;
    private static final int DELAY_SECONDS = 60;
    private final Player player;
    private final Type metabolismType;
    private boolean dirty = false;

    public double getAsDouble(Setting<?> setting) {
        return capability().getAsDouble(setting);
    }

    public int getAsInt(Setting<?> setting) {
        return capability().getAsInt(setting);
    }

    public String getAsString(Setting<?> setting) {
        return capability().getAsString(setting);
    }

    public void intensity(double intensity) {
        setValue(MetabolismSettings.of(metabolismType).intensity(), intensity);
    }

    public <T extends Comparable<? super T>> void setValue(Setting<T> setting, T value) {
        capability().updateValue(setting, value);
        dirty = true;
    }

    public void slopeDegradation(double slopeDegradation) {
        setValue(MetabolismSettings.of(metabolismType).slopeDegradation(), slopeDegradation);
    }

    public void ticks(int ticks) {
        setValue(MetabolismSettings.of(metabolismType).ticks(), ticks);
    }

    public void training(double training) {
        setValue(MetabolismSettings.of(metabolismType).training(), training);
    }

    public Type type() {
        return metabolismType;
    }

    public enum Type {
        Wet, Mess;

        @Override
        public String toString() {
            return name().toLowerCase();
        }
    }

    private Metabolism(Player player, Type metabolismType) {
        this.player = player;
        this.metabolismType = metabolismType;
    }

    public static @NotNull Metabolism wetOf(ICapabilityProvider provider) {
        return of(provider, Type.Wet);
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

    /**
     * Get the player's metabolism capability.
     */
    protected IMetabolism capability() {
        return switch(metabolismType) {
            case Mess -> player.getCapability(MetabolismCapabilities.MESS).resolve().orElseThrow();
            case Wet -> player.getCapability(MetabolismCapabilities.WET).resolve().orElseThrow();
        };
    }

    public boolean enabled() {
        boolean enabled = capability().getAsBool(MetabolismSettings.of(metabolismType).enabled());
        return !player.isDeadOrDying() && enabled;
    }

    public void enabled(boolean enabled) {
        setValue(MetabolismSettings.of(metabolismType).enabled(), enabled);
    }

    public double training() {
        return capability().getAsDouble(MetabolismSettings.of(metabolismType).training());
    }

    public int ticks() {
        return capability().getAsInt(MetabolismSettings.of(metabolismType).ticks());
    }

    public double slopeDegradation() {
        return capability().getAsDouble(MetabolismSettings.of(metabolismType).slopeDegradation());
    }

    public double frequencyCompression() {
        return capability().getAsDouble(MetabolismSettings.of(metabolismType).frequencyCompression());
    }

    public void frequencyCompression(double compression) {
        setValue(MetabolismSettings.of(metabolismType).frequencyCompression(), compression);
    }

    public double intensity() {
        return capability().getAsDouble(MetabolismSettings.of(metabolismType).intensity());
    }

    public void syncClient() {
        if (player instanceof ServerPlayer serverPlayer) {
            LOGGER.trace(LogMarkers.MET, "({}) [{}] Sending metabolism sync to client", CrinkleEvent.side(), metabolismType);
            CrinkleChannel.INSTANCE.send(PacketDistributor.PLAYER.with(() -> serverPlayer),
                    new MetabolismUpdateMessage(this));
        }
    }

    public void syncServer() {
        if (player instanceof ServerPlayer) return;
        LOGGER.trace(LogMarkers.MET, "({}) [{}] Sending metabolism sync to server", CrinkleEvent.side(), metabolismType);
        CrinkleChannel.INSTANCE.sendToServer(new MetabolismUpdateMessage(this));
    }

    public void resetDefaults() {
        capability().reset();
        syncServer();
    }

    public void reset() {
        LOGGER.trace(LogMarkers.MET, "({}) [{}] reset metabolism vars", CrinkleEvent.side(), metabolismType);
        currentTraining(training());
        pang(Pang.None);
        currentFrequency(ticks());
        pangDuration(0);
        tickCount(0);
    }

    public void currentTraining(double training) {
        setValue(MetabolismSettings.of(metabolismType).currentTraining(), training);
    }

    public void triggerAccident() {
        currentFrequency(ticks());
        currentTraining(training());
        pang(Pang.Accident);
        pangDuration(ACCIDENT_DURATION);
        LOGGER.trace(LogMarkers.MET, "({}) [{}] triggering accident: {}", CrinkleEvent.side(), metabolismType, capability());
    }

    private void handleFrequency() {
        if (isDelayed() || pang() == Pang.Accident) return;
        double newFrequency = currentFrequency() - frequencyCompression();
        if (newFrequency <= 0) {
            triggerAccident();
        } else {
            currentFrequency(newFrequency);
        }
    }

    private void handlePangs() {
        if (isDelayed()) return;
        Random random = new Random();
        switch(pang()) {
            case Accident -> {
                if (pangDuration() <= 0) {
                    reset();
                    LOGGER.trace(LogMarkers.MET, "({}) [{}] accident stop: pang={} pangDuration={}, currentFrequency={}",
                            CrinkleEvent.side(), metabolismType, pang(), pangDuration(), currentFrequency());
                } else {
                    pangDuration(pangDuration() - 1);
                    LOGGER.trace(LogMarkers.MET, "({}) [{}] accident tick: pang={} pangDuration={}, currentFrequency={}",
                            CrinkleEvent.side(), metabolismType, pang(), pangDuration(), currentFrequency());
                }
            }
            case Major, Minor -> {
                if (pangDuration() <= 0) {
                    // Check for accident
                    boolean accident = false;
                    double roll = 0.0;
                    if (pang() == Pang.Major) {
                        roll = random.nextDouble();
                        accident = roll > currentTraining();
                        if (accident) {
                            triggerAccident();
                        }
                    }
                    if (!accident) {
                        pang(Pang.None);
                        pangDuration(0);
                    }
                    LOGGER.trace(LogMarkers.MET, "({}) [{}] pang stop: pang={} pangDuration={}, currentFrequency={}, accident={}, roll={}, currentTraining={}",
                            CrinkleEvent.side(), metabolismType, pang(), pangDuration(), currentFrequency(), accident, roll, currentTraining());
                } else {
                    pangDuration(pangDuration() - 1);
                    LOGGER.trace(LogMarkers.MET, "({}) [{}] pang tick: pang={} pangDuration={}, currentFrequency={}",
                            CrinkleEvent.side(), metabolismType, pang(), pangDuration(), currentFrequency());
                }
            }
            case None -> {
                // We only handle pangs on the frequency tick
                if (tickCount() % Math.max(MIN_PANG, Math.ceil(currentFrequency())) != 0) {
                    LOGGER.trace(LogMarkers.MET, "({}) [{}] pang skip: tickCount={}, currentFrequency={} ceil(currentFrequency)={}",
                            CrinkleEvent.side(), metabolismType, tickCount(), currentFrequency(), Math.ceil(currentFrequency()));
                    return;
                }
                Pang pang = intensity() >= currentTraining() ? Pang.Major : Pang.Minor;
                pang(pang);
                int maxDuration = MathUtil.clamp((int) (currentFrequency() * 0.666), MIN_PANG + 1, MAX_PANG);
                pangDuration(random.nextInt(MIN_PANG, maxDuration));
                LOGGER.trace(LogMarkers.MET, "({}) [{}] pang start: pang={} pangDuration={}, maxDuration={}, currentFrequency={}",
                        CrinkleEvent.side(), metabolismType, pang(), pangDuration(), maxDuration, currentFrequency());
            }
        }
    }

    private boolean isDelayed() {
        // We are delayed if our tickCount is less than DELAY_SECONDS and currentTraining equals default training,
        // since both values reset after an accident.
        return tickCount() < DELAY_SECONDS && currentTraining() == training();
    }

    private void handleTraining() {
        if (isDelayed()) return;
        // Update currentTraining based on our slope
        double newTraining = Math.max(0, (slopeDegradation() * tickCount()) + training());
        LOGGER.trace(LogMarkers.MET, "({}) [{}] training tick: currentTraining={}, newTraining={}, slopeDegradation={}", CrinkleEvent.side(), metabolismType, currentTraining(), newTraining, slopeDegradation());
        if (newTraining <= 0) {
            triggerAccident();
        } else {
            currentTraining(newTraining);
        }
    }

    public void tick(int mcTickCount) {
        if (!enabled()) return;

        // Only process every second (TICK_FREQ = 20)
        if (mcTickCount % TICK_FREQ != 0) return;

        // Record our tick count
        tickCount(tickCount() + 1);

        // Process
        // Each checks isDelayed to see if we've entered the delay period
        // this ensures an accident from one handler will prevent other handlers during the delay
        handleTraining();
        handlePangs();
        handleFrequency();
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

    public Pang pang() {
        return Pang.from(capability().getAsString(MetabolismSettings.of(metabolismType).pang()));
    }

    public void pang(Pang pang) {
        setValue(MetabolismSettings.of(metabolismType).pang(), pang.name());
    }

    public void pangDuration(int duration) {
        setValue(MetabolismSettings.of(metabolismType).pangDuration(), duration);
    }

    public void currentFrequency(double frequency) {
        setValue(MetabolismSettings.of(metabolismType).currentFrequency(), frequency);
    }

    public int pangDuration() {
        return capability().getAsInt(MetabolismSettings.of(metabolismType).pangDuration());
    }

    public double currentFrequency() {
        return capability().getAsDouble(MetabolismSettings.of(metabolismType).currentFrequency());
    }

    public double currentTraining() {
        return capability().getAsDouble(MetabolismSettings.of(metabolismType).currentTraining());
    }
}
