package net.flectone.pulse.module.command.clearchat;

import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.constant.ModuleName;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.ModuleCommand;
import org.incendo.cloud.context.CommandContext;

/**
 * The /clearchat command, which blanks the chat window.
 * @author TheFaser
 */
public interface ClearchatModule extends ModuleCommand {

    @Override
    void onEnable();

    @Override
    void onDisable();

    @Override
    void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext);

    @Override
    ModuleName name();

    @Override
    Command.Clearchat config();

    @Override
    Permission.Command.Clearchat permission();

    @Override
    Localization.Command.Clearchat localization(FPlayer fPlayer);

    /**
     * Blanks the chat window, telling the other servers to do the same.
     *
     * @param fPlayer who ran the command
     */
    void clearChat(FPlayer fPlayer);

    /**
     * Blanks the chat window.
     *
     * @param fPlayer who ran the command
     * @param checkProxy whether to forward the request to the other servers
     */
    void clearChat(FPlayer fPlayer, boolean checkProxy);

}
