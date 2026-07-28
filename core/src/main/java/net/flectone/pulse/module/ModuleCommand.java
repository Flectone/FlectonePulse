package net.flectone.pulse.module;

import net.flectone.pulse.config.setting.CommandSetting;
import net.flectone.pulse.model.entity.FPlayer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.execution.CommandExecutionHandler;

public interface ModuleCommand extends ModuleLocalization, CommandExecutionHandler<FPlayer> {

    CommandSetting config();

    void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext);

    @Override
    default void execute(@NonNull CommandContext<FPlayer> commandContext) {
        execute(commandContext.sender(), commandContext);
    }

}
