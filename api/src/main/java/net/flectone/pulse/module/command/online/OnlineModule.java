package net.flectone.pulse.module.command.online;

import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.constant.ModuleName;
import net.flectone.pulse.constant.TimeType;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.module.ModuleCommand;
import org.incendo.cloud.context.CommandContext;
import org.jspecify.annotations.NonNull;

/**
 * The /online command, which reports how long a player has been playing.
 * @author TheFaser
 */
public interface OnlineModule extends ModuleCommand {

    @Override
    void onEnable();

    @Override
    void onDisable();

    @Override
    void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext);

    @Override
    ModuleName name();

    @Override
    Command.Online config();

    @Override
    Permission.Command.Online permission();

    @Override
    Localization.Command.Online localization(FPlayer fPlayer);

    /**
     * Registers the tags that render play-time figures.
     *
     * @param messageContext the message being formatted
     * @return the context with the tags added
     */
    MessageContext addTag(MessageContext messageContext);

    @NonNull String parseTimeValue(FPlayer fPlayer, FPlayer fReceiver, String time);

    /**
     * One play-time figure for a player.
     *
     * @param fPlayer the player
     * @param type which figure
     * @return the value in milliseconds
     */
    int getTime(FPlayer fPlayer, TimeType type);

    @NonNull String getTimeFormatted(FPlayer fPlayer, FPlayer fReceiver, TimeType type);

}
