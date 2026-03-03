package net.flectone.pulse.module;

import net.flectone.pulse.config.setting.CommandSetting;
import net.flectone.pulse.config.setting.LocalizationSetting;
import net.flectone.pulse.model.entity.FPlayer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.execution.CommandExecutionHandler;

public abstract class AbstractModuleCommand<M extends LocalizationSetting> extends AbstractModuleLocalization<M> implements CommandExecutionHandler<FPlayer> {

    @Override
    public void execute(@NonNull CommandContext<FPlayer> commandContext) {
        execute(commandContext.sender(), commandContext);
    }

    public abstract void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext);

    public abstract CommandSetting config();

}
