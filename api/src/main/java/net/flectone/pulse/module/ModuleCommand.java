package net.flectone.pulse.module;

import net.flectone.pulse.config.setting.CommandSetting;
import net.flectone.pulse.model.entity.FPlayer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.execution.CommandExecutionHandler;

/**
 * A module that registers a chat command.
 * @author TheFaser
 */
public interface ModuleCommand extends ModuleLocalization, CommandExecutionHandler<FPlayer> {

    /**
     * The config section for this command, which also holds its aliases.
     *
     * @return the command settings
     */
    CommandSetting config();

    /**
     * Runs the command.
     *
     * @param fPlayer who ran it
     * @param commandContext the parsed arguments
     */
    void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext);

    @Override
    default void execute(@NonNull CommandContext<FPlayer> commandContext) {
        execute(commandContext.sender(), commandContext);
    }

}
