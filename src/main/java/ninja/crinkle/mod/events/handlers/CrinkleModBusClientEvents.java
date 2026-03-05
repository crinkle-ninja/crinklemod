package ninja.crinkle.mod.events.handlers;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.*;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import ninja.crinkle.mod.CrinkleMod;
import ninja.crinkle.mod.client.gui.textures.ThemeAtlas;
import ninja.crinkle.mod.client.gui.screens.loader.ScreenReloadListener;
import ninja.crinkle.mod.client.gui.themes.loader.ThemeReloadListener;
import ninja.crinkle.mod.client.renderers.DunnyRenderer;
import ninja.crinkle.mod.client.textures.SpriteLoaderType;
import ninja.crinkle.mod.client.textures.Textures;

import java.util.Arrays;

import static ninja.crinkle.mod.blocks.CrinkleBlocks.DUNNY_BLOCK_ENTITY;


@Mod.EventBusSubscriber(modid = CrinkleMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class CrinkleModBusClientEvents {
    @SubscribeEvent
    public static void onRegisterReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(ThemeAtlas.getAtlas());
        event.registerReloadListener(new ThemeReloadListener());
        event.registerReloadListener(new ScreenReloadListener());
        Arrays.stream(SpriteLoaderType.values())
                .map(type -> Textures.getInstance().getSpriteLoader(type)).forEach(event::registerReloadListener);

    }

    @SubscribeEvent
    public static void registerRenderers(final EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(DUNNY_BLOCK_ENTITY.get(), DunnyRenderer::new);
    }

}
