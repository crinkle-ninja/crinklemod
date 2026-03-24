package ninja.crinkle.mod.undergarment;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import ninja.crinkle.mod.CrinkleMod;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.*;

public class DiaperDesignRegistry extends SimpleJsonResourceReloadListener {
    public static final String DIRECTORY = CrinkleMod.MODID + "/diaper_designs";
    public static final ResourceLocation PLAIN_ID = CrinkleMod.loc("plain");
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().create();

    private static final Map<ResourceLocation, DiaperDesign> SERVER_DESIGNS = new LinkedHashMap<>();
    private static final Map<ResourceLocation, DiaperDesign> CLIENT_DESIGNS = new LinkedHashMap<>();

    public DiaperDesignRegistry() {
        super(GSON, DIRECTORY);
    }

    @Override
    protected void apply(@NotNull Map<ResourceLocation, JsonElement> entries,
                         @NotNull ResourceManager resourceManager,
                         @NotNull ProfilerFiller profiler) {
        SERVER_DESIGNS.clear();
        for (Map.Entry<ResourceLocation, JsonElement> entry : entries.entrySet()) {
            try {
                DiaperDesign design = DiaperDesign.fromJson(entry.getKey(), entry.getValue().getAsJsonObject());
                SERVER_DESIGNS.put(entry.getKey(), design);
                LOGGER.info("Loaded diaper design: {}", entry.getKey());
            } catch (Exception e) {
                LOGGER.error("Failed to load diaper design {}: {}", entry.getKey(), e.getMessage());
            }
        }
        LOGGER.info("Loaded {} diaper design(s)", SERVER_DESIGNS.size());
    }

    public static void setClientDesigns(Map<ResourceLocation, DiaperDesign> designs) {
        CLIENT_DESIGNS.clear();
        CLIENT_DESIGNS.putAll(designs);
        LOGGER.info("Received {} diaper design(s) from server", CLIENT_DESIGNS.size());
    }

    public static Map<ResourceLocation, DiaperDesign> getServerDesigns() {
        return Collections.unmodifiableMap(SERVER_DESIGNS);
    }

    public static Map<ResourceLocation, DiaperDesign> getClientDesigns() {
        return Collections.unmodifiableMap(CLIENT_DESIGNS);
    }

    public static Optional<DiaperDesign> getDesign(ResourceLocation id, boolean clientSide) {
        return Optional.ofNullable(clientSide ? CLIENT_DESIGNS.get(id) : SERVER_DESIGNS.get(id));
    }

    public static Collection<DiaperDesign> getAllDesigns(boolean clientSide) {
        return clientSide ? CLIENT_DESIGNS.values() : SERVER_DESIGNS.values();
    }
}