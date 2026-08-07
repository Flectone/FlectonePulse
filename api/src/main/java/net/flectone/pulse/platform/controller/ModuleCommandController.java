package net.flectone.pulse.platform.controller;

import net.flectone.pulse.config.Localization;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.ModuleCommand;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.Command;
import org.incendo.cloud.context.CommandContext;

import java.util.List;
import java.util.function.Function;
import java.util.function.UnaryOperator;

/**
 * Registers module commands with the command framework and keeps track of their argument
 * names, which are translatable and therefore differ per language.
 * @author TheFaser
 */
public interface ModuleCommandController {

    /**
     * Registers a command under every alias in its config.
     *
     * @param command the module that handles it
     * @param builder describes the arguments
     */
    void registerCommand(ModuleCommand command, UnaryOperator<Command.Builder<FPlayer>> builder);

    /**
     * Registers an extra literal form of an already registered command.
     *
     * @param command the module that handles it
     * @param subName the literal that follows the command name
     * @param builder describes the remaining arguments
     */
    void registerSubCommand(ModuleCommand command, String subName, UnaryOperator<Command.Builder<FPlayer>> builder);

    /**
     * Forgets a command's argument names, so they are rebuilt after a language change.
     *
     * @param abstractModuleCommand the command to clear
     */
    void clearPrompts(ModuleCommand abstractModuleCommand);

    /**
     * Registers the translated name of one argument and returns it.
     *
     * @param command the command the argument belongs to
     * @param index the argument position
     * @param promptLocalization picks the name out of the localization file
     * @return the registered name
     */
    String addPrompt(ModuleCommand command, int index, Function<Localization.Command.Prompt, String> promptLocalization);

    /**
     * The registered name of one argument.
     *
     * @param command the command the argument belongs to
     * @param index the argument position
     * @return the argument name
     */
    String getPrompt(ModuleCommand command, int index);

    /**
     * Every registered argument name for a command, in order.
     *
     * @param command the command
     * @return the argument names
     */
    List<String> getPrompts(ModuleCommand command);

    /**
     * Reads a parsed argument by position rather than by its translated name.
     *
     * @param command the command the argument belongs to
     * @param context the parsed arguments
     * @param promptIndex the argument position
     * @param <V> the argument type
     * @return the parsed value
     */
    <V extends @NonNull Object> V getArgument(ModuleCommand command, CommandContext<FPlayer> context, int promptIndex);

    /**
     * The primary alias a command is registered under.
     *
     * @param command the command
     * @return the command name
     */
    String getCommandName(ModuleCommand command);

}
