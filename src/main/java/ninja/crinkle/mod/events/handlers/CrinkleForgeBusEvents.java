package ninja.crinkle.mod.events.handlers;

import com.mojang.logging.LogUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.MissingMappingsEvent;
import ninja.crinkle.mod.CrinkleMod;
import ninja.crinkle.mod.client.gui.overlays.CrinkleOverlay;
import ninja.crinkle.mod.items.CrinkleItems;
import ninja.crinkle.mod.metabolism.Metabolism;
import ninja.crinkle.mod.network.CrinkleChannel;
import ninja.crinkle.mod.network.messages.DesignsUpdateMessage;
import ninja.crinkle.mod.recipes.DesignRecipe;
import ninja.crinkle.mod.undergarment.Design;
import ninja.crinkle.mod.undergarment.DesignRegistry;
import ninja.crinkle.mod.undergarment.Undergarment;
import ninja.crinkle.mod.util.ClientUtil;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.List;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, modid = CrinkleMod.MODID)
public class CrinkleForgeBusEvents {
    private static final Logger LOGGER = LogUtils.getLogger();
    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof Player p && (ClientUtil.isDedicatedServer()
                || ClientUtil.getMinecraft().isLocalServer())) {
            Metabolism.wetOf(p).syncClient();
            Metabolism.messOf(p).syncClient();
        }
    }

    @SubscribeEvent
    public static void onDatapackSync(OnDatapackSyncEvent event) {
        DesignsUpdateMessage packet = new DesignsUpdateMessage(DesignRegistry.getAllDesigns());
        if (event.getPlayer() != null) {
            LOGGER.debug("Syncing designs to {}", event.getPlayer().getName());
            CrinkleChannel.INSTANCE.send(PacketDistributor.PLAYER.with(event::getPlayer), packet);
        } else {
            LOGGER.debug("Syncing designs to everyone");
            CrinkleChannel.INSTANCE.send(PacketDistributor.ALL.noArg(), packet);
        }
    }

    /**
     * Hook on when a player ticks. This is used to tick the metabolism of a player.
     * It only ticks the metabolism every 100 ticks, or 5 seconds.
     *
     * @param event The event
     * @see TickEvent.PlayerTickEvent
     */
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.@NotNull PlayerTickEvent event) {
        if (event.side.isServer()) return;
        if (event.player.isDeadOrDying()) return;
        if (event.phase == TickEvent.Phase.END && CrinkleOverlay.HUD.isReady()) {
            Metabolism.wetOf(event.player).tick(event.player.tickCount);
            Metabolism.messOf(event.player).tick(event.player.tickCount);
        }
    }

    @SubscribeEvent
    public static void onMissingItemMappings(@NotNull MissingMappingsEvent event) {
        for (MissingMappingsEvent.Mapping<Item> mapping : event.getAllMappings(ForgeRegistries.Keys.ITEMS)) {
            if (mapping.getKey().equals(CrinkleMod.loc("diaper_little_pawz"))) {
                mapping.remap(CrinkleItems.DIAPER.get());
            }
        }
    }

    @SubscribeEvent
    public static void onItemPickupEvent(PlayerEvent.ItemPickupEvent event) {
        List<Design> designs = DesignRegistry.getDesignsForItem(event.getStack());
        Player player = event.getEntity();
        if (player instanceof ServerPlayer serverPlayer) {
            List<Recipe<?>> designRecipes = serverPlayer.server.getRecipeManager().getRecipes().stream()
                    .filter(r -> r instanceof DesignRecipe recipe && designs.contains(recipe.design()))
                    .toList();
            serverPlayer.awardRecipes(designRecipes);
        }
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        BlockState state = event.getLevel().getBlockState(event.getPos());
        Block block = state.getBlock();
        if (block.equals(Blocks.CAULDRON) && Undergarment.hasUndergarmentData(event.getItemStack()) && event.getSide() == LogicalSide.CLIENT) {
            event.getEntity().sendSystemMessage(
                    Component.translatable("event.crinklemod.undergarment.use.cauldron.no_water.text",
                            event.getItemStack().getHoverName()));
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
        }
        if (block.equals(Blocks.WATER_CAULDRON)) {
            Undergarment undergarment = Undergarment.of(event.getItemStack());
            if (Undergarment.hasUndergarmentData(event.getItemStack())) {
                if (undergarment.getLiquids() > 0 || undergarment.getSolids() > 0) {
                    int amount = (undergarment.getLiquids() + undergarment.getSolids()) / 2;
                    undergarment.setLiquids(0);
                    undergarment.setSolids(0);
                    int numberToSpawn = Math.max(amount / 250, 1);
                    for (int i = 0; i < numberToSpawn; i++) {
                        event.getEntity().spawnAtLocation(new ItemStack(Items.CLAY_BALL));
                    }
                    if (event.getSide() == LogicalSide.CLIENT) {
                        event.getEntity().sendSystemMessage(
                                Component.translatable("event.crinklemod.undergarment.use.cauldron.text",
                                        event.getItemStack().getHoverName()));
                    }
                    LayeredCauldronBlock.lowerFillLevel(state, event.getLevel(), event.getPos());
                } else {
                    if (event.getSide() == LogicalSide.CLIENT) {
                        event.getEntity().sendSystemMessage(
                                Component.translatable("event.crinklemod.undergarment.use.cauldron.nothing.text",
                                        event.getItemStack().getHoverName()));
                    }
                }
                event.setCanceled(true);
                event.setCancellationResult(InteractionResult.SUCCESS);
            }
        }
    }
}
