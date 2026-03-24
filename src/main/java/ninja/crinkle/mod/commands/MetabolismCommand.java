package ninja.crinkle.mod.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import ninja.crinkle.mod.metabolism.Metabolism;
import ninja.crinkle.mod.metabolism.MetabolismSettings;
import ninja.crinkle.mod.settings.Setting;
import ninja.crinkle.mod.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static net.minecraft.commands.Commands.*;
import static net.minecraft.commands.arguments.EntityArgument.getPlayer;
import static net.minecraft.commands.arguments.EntityArgument.player;

public class MetabolismCommand {

    public MetabolismCommand(@NotNull CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralCommandNode<CommandSourceStack> node = dispatcher.register(literal("metabolism")
                .then(literal("status")
                        .executes(c -> status(c.getSource(), c.getSource().getPlayerOrException(),
                                c.getSource().getPlayerOrException()))
                        .then(argument("target", player())
                                .executes(c -> status(c.getSource(), c.getSource().getPlayerOrException(),
                                        getPlayer(c, "target")))))
                .then(literal("reset")
                        .requires(source -> source.hasPermission(LEVEL_GAMEMASTERS))
                        .executes(c -> resetData(c.getSource(), c.getSource().getPlayerOrException(),
                                c.getSource().getPlayerOrException()))
                        .then(argument("target", player())
                                .executes(c -> resetData(c.getSource(), c.getSource().getPlayerOrException(),
                                        getPlayer(c, "target")))))
                .then(literal("set")
                        .requires(source -> source.hasPermission(LEVEL_GAMEMASTERS))
                        .then(command(MetabolismSettings.TIMER))
                        .then(command(MetabolismSettings.NUMBER_ONE_ENABLED))
                        .then(command(MetabolismSettings.NUMBER_ONE_ROLLS))
                        .then(command(MetabolismSettings.NUMBER_ONE_SAFE_ROLLS))
                        .then(command(MetabolismSettings.NUMBER_ONE_CHANCE))
                        .then(command(MetabolismSettings.NUMBER_TWO_ENABLED))
                        .then(command(MetabolismSettings.NUMBER_TWO_ROLLS))
                        .then(command(MetabolismSettings.NUMBER_TWO_SAFE_ROLLS))
                        .then(command(MetabolismSettings.NUMBER_TWO_CHANCE))
                ));
        dispatcher.register(literal("met").redirect(node));
    }

    private int status(CommandSourceStack source, ServerPlayer instigator, @NotNull Player target) {
        Metabolism m = Metabolism.of(target);
        List<Component> components = new ArrayList<>();
        components.add(Component.literal(String.format("%s: %d seconds",
                Component.translatable("setting.crinklemod.metabolism.timer.label").getString(),
                m.getTimer())));
        components.add(Component.literal(String.format("%s: %s",
                Component.translatable("setting.crinklemod.metabolism.numberOneEnabled.label").getString(),
                m.isNumberOneEnabled())));
        components.add(Component.literal(String.format("%s: %d",
                Component.translatable("setting.crinklemod.metabolism.numberOneRolls.label").getString(),
                m.getNumberOneRolls())));
        components.add(Component.literal(String.format("%s: %d",
                Component.translatable("setting.crinklemod.metabolism.numberOneSafeRolls.label").getString(),
                m.getNumberOneSafeRolls())));
        components.add(Component.literal(String.format("%s: %.2f",
                Component.translatable("setting.crinklemod.metabolism.numberOneChance.label").getString(),
                m.getNumberOneChance())));
        components.add(Component.literal(String.format("%s: %s",
                Component.translatable("setting.crinklemod.metabolism.numberOneDesperationLevel.label").getString(),
                m.getNumberOneDesperationLevel())));
        components.add(Component.literal(String.format("%s: %s",
                Component.translatable("setting.crinklemod.metabolism.numberTwoEnabled.label").getString(),
                m.isNumberTwoEnabled())));
        components.add(Component.literal(String.format("%s: %d",
                Component.translatable("setting.crinklemod.metabolism.numberTwoRolls.label").getString(),
                m.getNumberTwoRolls())));
        components.add(Component.literal(String.format("%s: %d",
                Component.translatable("setting.crinklemod.metabolism.numberTwoSafeRolls.label").getString(),
                m.getNumberTwoSafeRolls())));
        components.add(Component.literal(String.format("%s: %.2f",
                Component.translatable("setting.crinklemod.metabolism.numberTwoChance.label").getString(),
                m.getNumberTwoChance())));
        components.add(Component.literal(String.format("%s: %s",
                Component.translatable("setting.crinklemod.metabolism.numberTwoDesperationLevel.label").getString(),
                m.getNumberTwoDesperationLevel())));
        source.sendSuccess(() -> Component.literal(target.getDisplayName().getString()), false);
        components.forEach(instigator::sendSystemMessage);
        return Command.SINGLE_SUCCESS;
    }

    private int resetData(@NotNull CommandSourceStack source, ServerPlayer instigator, @NotNull ServerPlayer target) {
        Metabolism m = Metabolism.of(target);
        m.setTimer(MetabolismSettings.TIMER.getDefault(target));
        m.setNumberOneEnabled(MetabolismSettings.NUMBER_ONE_ENABLED.getDefault(target));
        m.setNumberOneRolls(MetabolismSettings.NUMBER_ONE_ROLLS.getDefault(target));
        m.setNumberOneSafeRolls(MetabolismSettings.NUMBER_ONE_SAFE_ROLLS.getDefault(target));
        m.setNumberOneChance(MetabolismSettings.NUMBER_ONE_CHANCE.getDefault(target));
        m.setNumberTwoEnabled(MetabolismSettings.NUMBER_TWO_ENABLED.getDefault(target));
        m.setNumberTwoRolls(MetabolismSettings.NUMBER_TWO_ROLLS.getDefault(target));
        m.setNumberTwoSafeRolls(MetabolismSettings.NUMBER_TWO_SAFE_ROLLS.getDefault(target));
        m.setNumberTwoChance(MetabolismSettings.NUMBER_TWO_CHANCE.getDefault(target));
        m.syncClient();
        source.sendSuccess(() -> instigator.equals(target) ?
                        Component.translatable("command.crinklemod.metabolism.reset.self.success") :
                        Component.translatable("command.crinklemod.metabolism.reset.instigator.success",
                                target.getDisplayName()),
                true);
        return Command.SINGLE_SUCCESS;
    }

    private <T extends Comparable<? super T>> ArgumentBuilder<CommandSourceStack, ?> command(Setting<T> setting) {
        return literal(StringUtil.snake(setting.key()))
                .then(argument("target", player())
                        .then(argument("value", StringArgumentType.greedyString())
                                .executes(c -> setValue(c.getSource(), setting, c.getSource().getPlayerOrException(),
                                        getPlayer(c, "target"), StringArgumentType.getString(c, "value")))));
    }

    private <T extends Comparable<? super T>> int setValue(CommandSourceStack source, Setting<T> setting,
                                                           ServerPlayer instigator, @NotNull ServerPlayer target,
                                                           String value) {
        List<Component> errors = setting.errors(target, setting.valueOf(value));
        if (!errors.isEmpty()) {
            errors.forEach(source::sendFailure);
            return 0;
        }
        setting.set(target, value);
        Metabolism.of(target).syncClient();
        String command = StringUtil.snake(setting.key());
        source.sendSuccess(() -> instigator.equals(target) ?
                        Component.translatable("command.crinklemod.metabolism.set.self.success",
                                command, value) :
                        Component.translatable("command.crinklemod.metabolism.set.instigator.success",
                                target.getDisplayName(), command, value),
                true);

        return Command.SINGLE_SUCCESS;
    }
}
