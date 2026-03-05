package ninja.crinkle.mod.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import ninja.crinkle.mod.client.gui.overlays.CrinkleOverlay;
import ninja.crinkle.mod.client.gui.screens.binding.SettingRegistry;
import ninja.crinkle.mod.metabolism.MetabolismSettings;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

import static ninja.crinkle.mod.CrinkleMod.MODID;

@Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {
    public static KeyMapping LAYOUT_EDITOR_KEY;

    @SubscribeEvent
    public static void init(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> ClientHooks::registerEvents);
            registerSettings();
        });
    }

    private static void registerSettings() {
        SettingRegistry.register("metabolism.timer", MetabolismSettings.TIMER);
        SettingRegistry.register("metabolism.numberOneEnabled", MetabolismSettings.NUMBER_ONE_ENABLED);
        SettingRegistry.register("metabolism.numberOneChance", MetabolismSettings.NUMBER_ONE_CHANCE);
        SettingRegistry.register("metabolism.numberOneSafeRolls", MetabolismSettings.NUMBER_ONE_SAFE_ROLLS);
        SettingRegistry.register("metabolism.numberTwoEnabled", MetabolismSettings.NUMBER_TWO_ENABLED);
        SettingRegistry.register("metabolism.numberTwoChance", MetabolismSettings.NUMBER_TWO_CHANCE);
        SettingRegistry.register("metabolism.numberTwoSafeRolls", MetabolismSettings.NUMBER_TWO_SAFE_ROLLS);
    }

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        LAYOUT_EDITOR_KEY = new KeyMapping(
                "key.crinklemod.layout_editor",
                KeyConflictContext.IN_GAME,
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_F7,
                "key.categories.crinklemod"
        );
        event.register(LAYOUT_EDITOR_KEY);
    }

    @SubscribeEvent
    public static void registerGuiOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAboveAll("crinkle_overlay", CrinkleOverlay.HUD);
    }
}
