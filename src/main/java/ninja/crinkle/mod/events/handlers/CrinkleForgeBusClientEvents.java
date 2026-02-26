package ninja.crinkle.mod.events.handlers;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import ninja.crinkle.mod.sounds.CrinkleSounds;
import ninja.crinkle.mod.undergarment.Undergarment;
import ninja.crinkle.mod.util.ClientUtil;
import org.slf4j.Logger;

import java.util.Random;

public class CrinkleForgeBusClientEvents {
    private static final Logger LOGGER = LogUtils.getLogger();

    @SubscribeEvent
    public void handleCrinkles(PlaySoundEvent event) {
        Minecraft minecraft = ClientUtil.getMinecraft();
        if (minecraft == null || minecraft.level == null || minecraft.player == null || event.getSound() == null)
            return;
        BlockState blockState = minecraft.level.getBlockState(minecraft.player.blockPosition().below());
        if (blockState.getSoundType().getStepSound().getLocation().equals(event.getSound().getLocation())
                && Undergarment.getWornUndergarment(minecraft.player) != ItemStack.EMPTY) {
            Random random = new Random();
            minecraft.player.playSound(CrinkleSounds.CRINKLE_SOUND.get(),
                    0.5f + random.nextFloat(0.5f),
                    0.75f + random.nextFloat(0.5f));
        }
    }
}
