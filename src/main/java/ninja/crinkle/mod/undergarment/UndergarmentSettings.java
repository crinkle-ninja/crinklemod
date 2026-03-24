package ninja.crinkle.mod.undergarment;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import ninja.crinkle.mod.config.UndergarmentConfig;
import ninja.crinkle.mod.items.custom.DiaperArmorItem;
import ninja.crinkle.mod.network.messages.UndergarmentUpdateMessage;
import ninja.crinkle.mod.settings.Setting;

import java.util.Optional;

public class UndergarmentSettings {
    public static final Setting<Integer> LIQUIDS = Setting.intBuilder("liquids")
            .range(p -> 0, p -> Undergarment.of((ItemStack) p).getMaxLiquids())
            .label(Component.translatable("setting.crinklemod.undergarment.liquids.label"))
            .tooltip(Component.translatable("setting.crinklemod.undergarment.liquids.tooltip"))
            .defaultValue(0)
            .getter(p -> Undergarment.of((ItemStack) p).getLiquids())
            .setter((p, v) -> Undergarment.of((ItemStack) p).setLiquids(v))
            .synchronizer(p -> () -> UndergarmentUpdateMessage.builder()
                    .liquids(Undergarment.of((ItemStack) p).getLiquids())
                    .build()
                    .sendToServer())
            .build();
    public static final Setting<Integer> MAX_LIQUIDS = Setting.intBuilder("maxLiquids")
            .range(p -> 0, p -> Integer.MAX_VALUE)
            .label(Component.translatable("setting.crinklemod.undergarment.max_liquids.label"))
            .tooltip(Component.translatable("setting.crinklemod.undergarment.max_liquids.tooltip"))
            .defaultValue(p -> Optional.ofNullable(DiaperArmorItem.getDesign((ItemStack) p, false))
                    .map(d -> d.maxLiquids())
                    .orElse(UndergarmentConfig.getDefaultMaxLiquids()), 0)
            .getter(p -> Undergarment.of((ItemStack) p).getMaxLiquids())
            .setter((p, v) -> Undergarment.of((ItemStack) p).setMaxLiquids(v))
            .synchronizer(p -> () -> UndergarmentUpdateMessage.builder()
                    .maxLiquids(Undergarment.of((ItemStack) p).getMaxLiquids())
                    .build()
                    .sendToServer())
            .build();
    public static final Setting<Integer> MAX_SOLIDS = Setting.intBuilder("maxSolids")
            .range(p -> 0, p -> Integer.MAX_VALUE)
            .label(Component.translatable("setting.crinklemod.undergarment.max_solids.label"))
            .tooltip(Component.translatable("setting.crinklemod.undergarment.max_solids.tooltip"))
            .defaultValue(p -> Optional.ofNullable(DiaperArmorItem.getDesign((ItemStack) p, false))
                    .map(d -> d.maxSolids())
                    .orElse(UndergarmentConfig.getDefaultMaxSolids()), 0)
            .getter(p -> Undergarment.of((ItemStack) p).getMaxSolids())
            .setter((p, v) -> Undergarment.of((ItemStack) p).setMaxSolids(v))
            .synchronizer(p -> () -> UndergarmentUpdateMessage.builder()
                    .maxSolids(Undergarment.of((ItemStack) p).getMaxSolids())
                    .build()
                    .sendToServer())
            .build();
    public static final Setting<Integer> SOLIDS = Setting.intBuilder("solids")
            .range(p -> 0, p -> Undergarment.of((ItemStack) p).getMaxSolids())
            .label(Component.translatable("setting.crinklemod.undergarment.solids.label"))
            .tooltip(Component.translatable("setting.crinklemod.undergarment.solids.tooltip"))
            .defaultValue(0)
            .getter(p -> Undergarment.of((ItemStack) p).getSolids())
            .setter((p, v) -> Undergarment.of((ItemStack) p).setSolids(v))
            .synchronizer(p -> () -> UndergarmentUpdateMessage.builder()
                    .solids(Undergarment.of((ItemStack) p).getSolids())
                    .build()
                    .sendToServer())
            .build();
}
