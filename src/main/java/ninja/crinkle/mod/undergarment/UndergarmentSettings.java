package ninja.crinkle.mod.undergarment;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import ninja.crinkle.mod.settings.Setting;

public class UndergarmentSettings {
    public static final Setting<Integer> LIQUIDS = Setting.intBuilder("liquids")
            .range(p -> 0, p -> Undergarment.of((ItemStack) p).getMaxLiquids())
            .label(Component.translatable("setting.crinklemod.undergarment.liquids.label"))
            .tooltip(Component.translatable("setting.crinklemod.undergarment.liquids.tooltip"))
            .defaultValue(0)
            .getter(p -> Undergarment.of((ItemStack) p).getLiquids())
            .setter((p, v) -> Undergarment.of((ItemStack) p).setLiquids(v))
            .build();

    public static final Setting<Integer> SOLIDS = Setting.intBuilder("solids")
            .range(p -> 0, p -> Undergarment.of((ItemStack) p).getMaxSolids())
            .label(Component.translatable("setting.crinklemod.undergarment.solids.label"))
            .tooltip(Component.translatable("setting.crinklemod.undergarment.solids.tooltip"))
            .defaultValue(0)
            .getter(p -> Undergarment.of((ItemStack) p).getSolids())
            .setter((p, v) -> Undergarment.of((ItemStack) p).setSolids(v))
            .build();

    public static final Setting<Integer> MAX_LIQUIDS = Setting.intBuilder("maxLiquids")
            .range(p -> 0, p -> Integer.MAX_VALUE)
            .label(Component.translatable("setting.crinklemod.undergarment.max_liquids.label"))
            .tooltip(Component.translatable("setting.crinklemod.undergarment.max_liquids.tooltip"))
            .defaultValue(0)
            .getter(p -> Undergarment.of((ItemStack) p).getMaxLiquids())
            .setter((p, v) -> Undergarment.of((ItemStack) p).setMaxLiquids(v))
            .build();

    public static final Setting<Integer> MAX_SOLIDS = Setting.intBuilder("maxSolids")
            .range(p -> 0, p -> Integer.MAX_VALUE)
            .label(Component.translatable("setting.crinklemod.undergarment.max_solids.label"))
            .tooltip(Component.translatable("setting.crinklemod.undergarment.max_solids.tooltip"))
            .defaultValue(0)
            .getter(p -> Undergarment.of((ItemStack) p).getMaxSolids())
            .setter((p, v) -> Undergarment.of((ItemStack) p).setMaxSolids(v))
            .build();
}
